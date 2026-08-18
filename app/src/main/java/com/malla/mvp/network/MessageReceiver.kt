package com.malla.mvp.network

import android.content.Context
import android.util.Log
import com.malla.mvp.core.engine.LogBuffer
import com.malla.mvp.data.AppDatabase
import com.malla.mvp.data.entity.ConversationEntity
import com.malla.mvp.data.entity.MessageEntity
import com.malla.mvp.data.entity.PollEntity
import com.malla.mvp.data.entity.PollOptionEntity
import com.malla.mvp.di.Injector
import com.malla.mvp.events.MallaEventBus
import kotlinx.coroutines.*
import java.util.UUID

object MessageReceiver {
    private const val TAG = "MessageReceiver"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val bloomFilter = BloomFilter()
    private var started = false

    fun start(context: Context) {
        if (started) return
        started = true
        bloomFilter.startAutoRotation()
        LogBuffer.add(TAG, "MessageReceiver iniciado")

        // Mensajes de la red mesh (TCP/WebRTC/Internet)
        scope.launch {
            NetworkService.messages.collect { meshMsg ->
                process(context, meshMsg)
            }
        }

        // Mensajes globales WebRTC
        scope.launch {
            WebRtcDataManager.incomingMessages.collect { meshMsg ->
                process(context, meshMsg)
            }
        }

        // Mensajes SMS entrantes
        scope.launch {
            Injector.smsTransport.incomingMessages.collect { raw ->
                val parts = raw.split("|", limit = 2)
                val sender = parts.getOrElse(0) { "unknown" }
                val body = parts.getOrElse(1) { raw }
                val meshMsg = MeshMessage(
                    content = body,
                    senderId = sender,
                    timestamp = System.currentTimeMillis(),
                    type = "sms"
                )
                process(context, meshMsg)
            }
        }
    }

    // Método público para que MessageBridge delegue
    fun process(context: Context, meshMsg: MeshMessage) {
        scope.launch {
            processInternal(context, meshMsg)
        }
    }

    private suspend fun processInternal(context: Context, meshMsg: MeshMessage) {
        try {
            val db = AppDatabase.getInstance(context) ?: return
            val messageId = "${meshMsg.senderId}_${meshMsg.timestamp}_${meshMsg.content.hashCode()}"

            if (!ReplayProtection.validate(messageId, meshMsg.timestamp)) {
                Log.w(TAG, "Duplicado: $messageId")
                return
            }
            if (bloomFilter.mightContain(messageId)) {
                Log.w(TAG, "Bloom: posible duplicado $messageId")
                return
            }
            bloomFilter.add(messageId)

            if (meshMsg.type == "poll_create") {
                try {
                    val json = org.json.JSONObject(meshMsg.content)
                    val pollId = json.getString("pollId")
                    val question = json.getString("question")
                    val optionsArray = json.getJSONArray("options")
                    db.pollDao().insertPoll(PollEntity(id = pollId, groupId = meshMsg.senderId, question = question, creatorId = meshMsg.senderId))
                    for (i in 0 until optionsArray.length()) {
                        val text = optionsArray.getString(i)
                        if (text.isNotBlank()) {
                            db.pollDao().insertOption(PollOptionEntity(id = java.util.UUID.randomUUID().toString(), pollId = pollId, text = text))
                        }
                    }
                    val localMsg = MessageEntity(
                        id = java.util.UUID.randomUUID().toString(),
                        conversationId = meshMsg.senderId,
                        content = "📊 $question",
                        timestamp = meshMsg.timestamp,
                        isOwn = false,
                        status = 1,
                        pollId = pollId
                    )
                    db.messageDao().insertMessage(localMsg)
                    MallaEventBus.messageReceived.emit(meshMsg)
                } catch (e: Exception) {
                    Log.e(TAG, "Error procesando poll_create: ${e.message}", e)
                }
                return
            }

            if (meshMsg.type == "poll_vote") {
                try {
                    val json = org.json.JSONObject(meshMsg.content)
                    val pollId = json.getString("pollId")
                    val optionId = json.getString("optionId")
                    db.pollDao().incrementVoteCount(optionId, 1)
                    MallaEventBus.messageReceived.emit(meshMsg)
                } catch (e: Exception) {
                    Log.e(TAG, "Error procesando poll_vote: ${e.message}", e)
                }
                return
            }


            if (meshMsg.type == "edit" && meshMsg.quotedMessageId != null) {
                val originalId = meshMsg.quotedMessageId!!
                db?.messageDao()?.updateContent(originalId, meshMsg.content)
                MallaEventBus.messageReceived.emit(meshMsg)
                return
            }

            if (meshMsg.type == "delete_for_all" && meshMsg.quotedMessageId != null) {
                val originalId = meshMsg.quotedMessageId!!
                db?.messageDao()?.markAsDeleted(originalId)
                MallaEventBus.messageReceived.emit(meshMsg)
                return
            }

            val conversationId = meshMsg.senderId

            val conversationDao = db.conversationDao()
            val messageDao = db.messageDao()

            var conv = conversationDao.getConversationById(conversationId)
            if (conv == null) {
                conv = ConversationEntity(
                    id = conversationId,
                    title = "Peer ${conversationId.take(8)}",
                    lastMessage = meshMsg.content.take(30),
                    timestamp = meshMsg.timestamp,
                    unreadCount = 1
                )
                conversationDao.insertConversation(conv)
            } else {
                conversationDao.updateLastMessage(
                    conversationId,
                    meshMsg.content.take(30),
                    meshMsg.timestamp
                )
            }

            val msgEntity = MessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = conversationId,
                content = meshMsg.content,
                timestamp = meshMsg.timestamp,
                isOwn = false,
                status = 1,
                quotedMessageId = meshMsg.quotedMessageId,
                quotedMessageContent = meshMsg.quotedMessageContent
            )
            messageDao.insertMessage(msgEntity)

            MallaEventBus.messageReceived.emit(meshMsg)
            if (meshMsg.type == "zumbido") {
                MallaEventBus.zumbidoReceived.emit(meshMsg)
            }
            LogBuffer.add(TAG, "Mensaje guardado de ${meshMsg.senderId}")
        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}", e)
        }
    }

    fun stop() {
        bloomFilter.stopAutoRotation()
        started = false
    }
}
