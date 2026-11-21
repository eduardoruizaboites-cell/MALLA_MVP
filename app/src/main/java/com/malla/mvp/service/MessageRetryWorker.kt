package com.malla.mvp.service

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.malla.mvp.data.AppDatabase
import com.malla.mvp.data.entity.MessageEntity
import com.malla.mvp.data.entity.MeshMessage
import com.malla.mvp.identity.IdentityManager
import com.malla.mvp.network.TransportManager

/**
 * Iter 73 (1b): worker que reintenta mensajes PENDING de la cola persistente Room.
 *
 * Diseño:
 *  - Un solo worker periódico (cada 2 min) que procesa en lote.
 *  - Además, enqueueUniqueWork one-shot tras cada send() fallido para el primer
 *    retry sin esperar 2 min.
 *  - Backoff exponencial por mensaje: 15s, 30s, 1m, 2m, 4m, 8m, 16m, 32m (max).
 *  - MAX_RETRIES = 8 (~1h de intentos). Al agotarse, marca FAILED (status=3).
 */
class MessageRetryWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(applicationContext) ?: return Result.success()
        val now = System.currentTimeMillis()

        return try {
            val pendings = db.messageDao().getPendingMessagesForRetry(MAX_RETRIES)
            if (pendings.isEmpty()) return Result.success()

            var deliveredCount = 0
            var failedCount = 0
            var skippedBackoff = 0

            for (msg in pendings) {
                if (msg.retryCount > 0) {
                    val backoffMs = backoffFor(msg.retryCount)
                    if (now - msg.lastRetryAt < backoffMs) {
                        skippedBackoff++
                        continue
                    }
                }

                val meshMsg = MeshMessage(
                    content = msg.content,
                    senderId = IdentityManager.getIdentityId(),
                    timestamp = msg.timestamp,
                    type = msg.type,
                    messageId = msg.id,
                    quotedMessageId = msg.quotedMessageId,
                    quotedMessageContent = msg.quotedMessageContent,
                    expireAt = msg.expireAt,
                    viewOnce = msg.viewOnce
                )

                val ok = try {
                    TransportManager.send(msg.conversationId, meshMsg)
                } catch (e: Exception) {
                    Log.w(TAG, "retry.send exception msg=${msg.id.take(8)}", e)
                    false
                }

                if (ok) {
                    db.messageDao().updateStatus(msg.id, MessageEntity.STATUS_SENT)
                    deliveredCount++
                } else {
                    db.messageDao().incrementRetry(msg.id, now)
                    if (msg.retryCount + 1 >= MAX_RETRIES) {
                        db.messageDao().markFailed(msg.id, "max_retries_exceeded")
                        failedCount++
                    }
                }
            }

            Log.d(TAG, "retry batch: ${'$'}{pendings.size} pending, ${'$'}deliveredCount sent, ${'$'}failedCount failed, ${'$'}skippedBackoff backoff")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "retry worker error", e)
            Result.retry()
        }
    }

    private fun backoffFor(retryCount: Int): Long {
        if (retryCount <= 0) return 0L
        val base = 15_000L
        val cap = 32L * 60L * 1000L
        return (base shl (retryCount - 1)).coerceAtMost(cap)
    }

    companion object {
        const val TAG = "MsgRetryWorker"
        const val MAX_RETRIES = 8
    }
}
