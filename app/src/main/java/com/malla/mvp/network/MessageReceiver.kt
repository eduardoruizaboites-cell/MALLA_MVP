package com.malla.mvp.network

import android.content.Context
import android.util.Log
import com.malla.mvp.core.engine.LogBuffer
import com.malla.mvp.core.engine.DiagnosticsLogger
import com.malla.mvp.data.AppDatabase
import com.malla.mvp.data.entity.ConversationEntity
import com.malla.mvp.data.entity.MessageEntity
import com.malla.mvp.data.entity.PollEntity
import com.malla.mvp.data.entity.PollOptionEntity
import com.malla.mvp.di.Injector
import com.malla.mvp.identity.IdentityManager
import com.malla.mvp.events.MallaEventBus
import com.malla.mvp.network.BleTransport
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

        // Mensajes BLE entrantes
        scope.launch {
            BleTransport.messages.collect { bytes ->
                val raw = String(bytes, Charsets.UTF_8)
                DiagnosticsLogger.log(TAG, "BLE recibido: $raw")
                var meshMsg: MeshMessage? = null
                try {
                    val json = org.json.JSONObject(raw)
                    val extractedContent = json.optString("content", "")
                    meshMsg = MeshMessage(
                        content = if (extractedContent.isNotBlank()) extractedContent else raw,
                        senderId = json.optString("senderId", json.optString("senderld", "unknown")),
                        timestamp = json.optLong("timestamp", System.currentTimeMillis()),
                        type = json.optString("type", "chat"),
                        messageId = json.optString("messageId", null)
                    )
                    DiagnosticsLogger.log(TAG, "BLE JSON recibido: senderId=${meshMsg.senderId}, content=${meshMsg.content.take(30)}")
                } catch (_: Exception) {
                    // Formato antiguo: sender|content
                    val parts = raw.split("|", limit = 2)
                    val sender = parts.getOrElse(0) { "unknown" }
                    val body = parts.getOrElse(1) { raw }
                    meshMsg = MeshMessage(
                        content = body,
                        senderId = sender,
                        timestamp = System.currentTimeMillis(),
                        type = "chat"
                    )
                }
                meshMsg?.let { process(context, it) }
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

            if (meshMsg.type == "reaction" && meshMsg.quotedMessageId != null) {
                db.messageDao().updateReaction(meshMsg.quotedMessageId!!, meshMsg.content.ifBlank { null })
                MallaEventBus.messageReceived.emit(meshMsg)
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

            if (meshMsg.type == "ack" && meshMsg.messageId != null) {
                val newStatus = when (meshMsg.content) {
                    "1" -> 1
                    "2" -> 2
                    else -> 1
                }
                db.messageDao().updateStatus(meshMsg.messageId!!, newStatus)
                MallaEventBus.messageReceived.emit(meshMsg)
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
                val peerName = NetworkService.connectedPeers[conversationId] ?: "Peer ${conversationId.take(8)}"
                conv = ConversationEntity(
                    id = conversationId,
                    title = peerName,
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
                quotedMessageContent = meshMsg.quotedMessageContent,
                expireAt = meshMsg.expireAt,
                viewOnce = meshMsg.viewOnce
            )
            messageDao.insertMessage(msgEntity)

            // Enviar ack real al emisor si el mensaje trae messageId
            if (meshMsg.type == "chat" && meshMsg.messageId != null && meshMsg.senderId != "self") {
                // Por TCP
                try {
                    NetworkService.sendMessageToContact(
                        meshMsg.senderId,
                        MeshMessage(
                            content = "2",
                            senderId = IdentityManager.getIdentityId(),
                            type = "ack",
                            messageId = meshMsg.messageId
                        )
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error enviando ack TCP: ${e.message}")
                }
                // Por BLE (broadcast a todos los conectados)
                try {
                    val ackJson = org.json.JSONObject().apply {
                        put("senderId", IdentityManager.getIdentityId())
                        put("content", "2")
                        put("messageId", meshMsg.messageId)
                        put("type", "ack")
                        put("timestamp", System.currentTimeMillis())
                    }.toString()
                    BleTransport.broadcast(ackJson.toByteArray(Charsets.UTF_8))
                } catch (e: Exception) {
                    Log.e(TAG, "Error enviando ack BLE: ${e.message}")
                }
            }

            // Marcar como leídos los mensajes propios de esta conversación (palomita leída)
            try {
                messageDao.updateStatusForConversationAndOwn(conversationId, isOwn = true, newStatus = 2)
            } catch (e: Exception) {
                Log.e(TAG, "Error marcando leídos: ${e.message}")
            }

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
