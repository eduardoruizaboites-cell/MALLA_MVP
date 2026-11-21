package com.malla.mvp.service

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Iter 73 (1b): encolado de reintentos.
 *  - ensurePeriodicScheduled: registra el worker periódico (cada 2 min) una sola vez.
 *  - scheduleImmediate: one-shot inmediato tras un send() fallido.
 */
object MessageRetryScheduler {

    private const val PERIODIC_NAME = "message_retry_periodic"
    private const val ONESHOT_NAME = "message_retry_oneshot"

    fun ensurePeriodicScheduled(context: Context) {
        val request = PeriodicWorkRequestBuilder<MessageRetryWorker>(2, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    // Sin restricción de red: el retry puede usar BLE, TCP LAN, etc.
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .build()
        WorkManager.getInstance(context.applicationContext)
            .enqueueUniquePeriodicWork(PERIODIC_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    fun scheduleImmediate(context: Context) {
        val request = OneTimeWorkRequestBuilder<MessageRetryWorker>().build()
        WorkManager.getInstance(context.applicationContext)
            .enqueueUniqueWork(ONESHOT_NAME, ExistingWorkPolicy.REPLACE, request)
    }
}
