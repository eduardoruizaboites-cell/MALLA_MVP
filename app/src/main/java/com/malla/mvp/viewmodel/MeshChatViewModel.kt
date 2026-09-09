package com.malla.mvp.viewmodel

import android.app.Application
import java.io.ByteArrayOutputStream
import android.net.Uri
import android.util.Base64
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.malla.mvp.App
import com.malla.mvp.data.AppDatabase
import com.malla.mvp.data.entity.MessageEntity
import com.malla.mvp.data.entity.ConversationEntity
import com.malla.mvp.identity.IdentityManager
import com.malla.mvp.crypto.CryptoEngine
import com.malla.mvp.crypto.SessionCipher
import com.malla.mvp.core.data.MessageMapper
import com.malla.mvp.core.data.MessageData
import com.malla.mvp.events.MallaEventBus
import com.malla.mvp.data.entity.PollEntity
import com.malla.mvp.data.entity.PollOptionEntity
import com.malla.mvp.data.entity.MeshMessage
import com.malla.mvp.network.NetworkService
import com.malla.mvp.network.TransportManager
import com.malla.mvp.network.BleTransport
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.UUID
import javax.crypto.SecretKey

class MeshChatViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)

    private val _conversationId = MutableStateFlow<String?>(null)
    val conversationId: StateFlow<String?> = _conversationId.asStateFlow()

    private val _messages = MutableStateFlow<List<MessageData>>(emptyList())
    val messages: StateFlow<List<MessageData>> = _messages.asStateFlow()

    private var messageJob: Job? = null
    private var lastMessageTimestamp = 0L
    private var sessionKey: SecretKey? = null
    private var encryptionEnabled = false

    private val _polls = MutableStateFlow<List<PollEntity>>(emptyList())
    val polls: StateFlow<List<PollEntity>> = _polls.asStateFlow()

    private val _optionsMap = MutableStateFlow<Map<String, List<PollOptionEntity>>>(emptyMap())
    val optionsMap: StateFlow<Map<String, List<PollOptionEntity>>> = _optionsMap.asStateFlow()

    private val _pinnedMessage = MutableStateFlow<MessageData?>(null)
    val pinnedMessage: StateFlow<MessageData?> = _pinnedMessage.asStateFlow()

    private val _remoteTyping = MutableStateFlow(false)
    val remoteTyping: StateFlow<Boolean> = _remoteTyping.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    init {
        // Observar mensajes entrantes del bus global
        viewModelScope.launch {
            MallaEventBus.messageReceived.collect { msg ->
                if (msg.senderId != "self") {  // Evitar procesar mensajes propios (se guardan localmente al enviar)
                    handleIncomingMessage(msg)
                }
            }
        }
        // Observar estado de escribiendo remoto
        viewModelScope.launch {
            MallaEventBus.typingReceived.collect { (sender, isTyping) ->
                if (sender != "self" && sender == _conversationId.value) {
                    _remoteTyping.value = isTyping
                }
            }
        }
    }

    fun loadConversation(convId: String) {
        if (_conversationId.value == convId) return
        _conversationId.value = convId
        _messages.value = emptyList()
        refreshMessages(convId)
        loadPolls(convId)
        initEncryption(convId)
    }

    private fun initEncryption(convId: String) {
        viewModelScope.launch {
            if (encryptionEnabled || convId == "self_chat") return@launch
            try {
                val database = db ?: return@launch
                val contactDao = database.contactDao()
                val contact = contactDao.getById(convId)
                if (contact != null) {
                    val remotePubKey = CryptoEngine.base64ToPublicKey(contact.publicKey)
                    val localPrivKey = IdentityManager.getPrivateKey()
                    sessionKey = SessionCipher.deriveSessionKey(localPrivKey, remotePubKey)
                    encryptionEnabled = true
                }
            } catch (e: Exception) {
                // No se pudo obtener clave pública
            }
        }
    }

    private fun refreshMessages(convId: String) {
        viewModelScope.launch {
            messageJob?.cancelAndJoin()
            messageJob = launch {
                try {
                    val database = db ?: return@launch
                    database.messageDao().deleteExpiredMessages(convId, System.currentTimeMillis())
                    val msgs = database.messageDao().getMessagesForConversationOnce(convId)
                    _messages.value = msgs.filter { it.conversationId == convId }.map { msg ->
                        if (msg.encrypted && sessionKey != null) {
                            try {
                                val decryptedContent = SessionCipher.decrypt(msg.content, sessionKey!!)
                                msg.copy(content = decryptedContent)
                            } catch (e: Exception) {
                                msg
                            }
                        } else msg
                    }.map { MessageMapper.toMessageData(it) }
                    _pinnedMessage.value = _messages.value.firstOrNull { it.isPinned }
                    if (msgs.isNotEmpty()) {
                        lastMessageTimestamp = msgs.maxOf { it.timestamp }
                    }
                } catch (e: Exception) {
                    Log.e("MeshChatVM", "Error cargando mensajes", e)
                }
            }
        }
    }

    suspend fun getPollData(pollId: String): Pair<PollEntity?, List<PollOptionEntity>> {
        val poll = db?.pollDao()?.getPollById(pollId)
        val options = if (poll != null) db?.pollDao()?.getOptionsForPollOnce(pollId) ?: emptyList() else emptyList()
        return poll to options
    }

    fun getOptionsForPoll(pollId: String): kotlinx.coroutines.flow.Flow<List<PollOptionEntity>> {
        return db?.pollDao()?.getOptionsForPoll(pollId) ?: kotlinx.coroutines.flow.flowOf(emptyList())
    }

    private fun loadPolls(convId: String) {
        viewModelScope.launch {
            db?.pollDao()?.getPollsForGroup(convId)?.collect { pollList ->
                _polls.value = pollList
                pollList.forEach { poll ->
                    db.pollDao().getOptionsForPoll(poll.id).collect { options ->
                        _optionsMap.value = _optionsMap.value + (poll.id to options)
                    }
                }
            }
        }
    }

    private suspend fun handleIncomingMessage(msg: MeshMessage) {
        val convId = msg.senderId
        // MessageReceiver ya inserta el mensaje en la BD;
        // solo actualizamos la UI si la conversación actual está abierta.
        if (convId == _conversationId.value) {
            refreshMessages(convId)
        }
        // Si no es la conversación actual, MessageReceiver se encargó de guardarla.
    }

    fun updateInputText(text: String) {
        _inputText.value = text
    }

    fun startRecording() { _isRecording.value = true }
    fun stopRecording() { _isRecording.value = false }

    fun isMessageNew(timestamp: Long): Boolean {
        return timestamp > lastMessageTimestamp
    }


    fun sendTyping(isTyping: Boolean) {
        val convId = _conversationId.value ?: return
        if (convId == "self_chat") return
        viewModelScope.launch {
            TransportManager.sendTyping(convId, isTyping)
        }
    }

    fun sendZumbido() {
        val convId = _conversationId.value ?: return
        viewModelScope.launch {
            MallaEventBus.zumbidoReceived.tryEmit(
                MeshMessage(content = "📳 Zumbido", senderId = "self", type = "zumbido")
            )
            if (convId != "self_chat") {
                TransportManager.send(convId, MeshMessage(content = "📳 Zumbido", senderId = "self", type = "zumbido"))
            }
        }
    }

    fun sendMessage(
        text: String,
        quotedMessageId: String? = null,
        quotedMessageContent: String? = null,
        expireAt: Long? = null,
        viewOnce: Boolean = false,
        mediaUri: String? = null
    ) {
        val convId = _conversationId.value ?: return
        viewModelScope.launch {
            val finalContent = if (encryptionEnabled && sessionKey != null) {
                try {
                    SessionCipher.encrypt(text.ifBlank { "📷 Imagen" }, sessionKey!!)
                } catch (e: Exception) {
                    text.ifBlank { "📷 Imagen" }
                }
            } else {
                text.ifBlank { "📷 Imagen" }
            }
            // Si hay imagen, comprimir y codificar en Base64 para que viaje en content
            var base64Image: String? = null
            if (mediaUri != null) {
                try {
                    val resolver = getApplication<Application>().contentResolver
                    val inputStream = resolver.openInputStream(Uri.parse(mediaUri)) ?: return@launch
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream.close()
                    if (bitmap != null) {
                        val scaled = Bitmap.createScaledBitmap(bitmap, 320, 320, false)
                        val baos = java.io.ByteArrayOutputStream()
                        scaled.compress(Bitmap.CompressFormat.JPEG, 70, baos)
                        base64Image = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
                    }
                } catch (e: Exception) {
                    Log.e("MeshChatVM", "Error convirtiendo imagen a Base64", e)
                }
            }
            val contentToSend = base64Image ?: finalContent

            val msg = MessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = convId,
                content = contentToSend,
                isOwn = true,
                expireAt = expireAt,
                mediaUri = if (base64Image != null) null else mediaUri,
                viewOnce = viewOnce,
                quotedMessageId = quotedMessageId,
                quotedMessageContent = quotedMessageContent,
                encrypted = encryptionEnabled
            )
            db?.messageDao()?.insertMessage(msg)
            if (convId != "self_chat") {
                // Enviar dirigido al contactId correcto (convId)
                try {
                    val meshMsg = MeshMessage(
                        content = contentToSend,
                        senderId = IdentityManager.getIdentityId(),
                        timestamp = System.currentTimeMillis(),
                        type = "chat",
                        messageId = msg.id,
                        quotedMessageId = quotedMessageId,
                        quotedMessageContent = quotedMessageContent,
                        expireAt = expireAt,
                        viewOnce = viewOnce
                    )
                    TransportManager.send(convId, meshMsg)
                    db?.messageDao()?.updateStatus(msg.id, 1)  // entregado
                } catch (e: Exception) {
                    // fallback: queda como enviado (0)
                }
            } else {
                db?.messageDao()?.updateStatus(msg.id, 2)  // leído
            }
            _inputText.value = ""
            refreshMessages(convId)
        }
    }

    fun votePoll(optionId: String, pollId: String) {
        val convId = _conversationId.value ?: return
        viewModelScope.launch {
            db?.pollDao()?.incrementVoteCount(optionId, 1)
            val currentOptions = _optionsMap.value[pollId] ?: return@launch
            val updated = currentOptions.map { opt ->
                if (opt.id == optionId) opt.copy(voteCount = opt.voteCount + 1) else opt
            }
            _optionsMap.value = _optionsMap.value + (pollId to updated)

            if (convId != "self_chat") {
                val json = org.json.JSONObject().apply {
                    put("pollId", pollId)
                    put("optionId", optionId)
                }.toString()
                TransportManager.send(
                    convId,
                    MeshMessage(content = json, senderId = IdentityManager.getIdentityId(), type = "poll_vote")
                )
            }
        }
    }

    fun createPoll(question: String, options: List<String>) {
        val convId = _conversationId.value ?: return
        viewModelScope.launch {
            val pollId = UUID.randomUUID().toString()
            db?.pollDao()?.insertPoll(PollEntity(id = pollId, groupId = convId, question = question, creatorId = "self"))
            options.forEach { text ->
                if (text.isNotBlank()) {
                    db?.pollDao()?.insertOption(PollOptionEntity(id = UUID.randomUUID().toString(), pollId = pollId, text = text))
                }
            }

            // Insertar mensaje local para que la encuesta aparezca en el historial
            val localMsg = MessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = convId,
                content = "📊 $question",
                timestamp = System.currentTimeMillis(),
                isOwn = true,
                status = 0,
                pollId = pollId
            )
            db?.messageDao()?.insertMessage(localMsg)
            refreshMessages(convId)
            loadPolls(convId)

            if (convId != "self_chat") {
                val json = org.json.JSONObject().apply {
                    put("pollId", pollId)
                    put("question", question)
                    put("options", org.json.JSONArray(options.filter { it.isNotBlank() }))
                }.toString()
                TransportManager.send(
                    convId,
                    MeshMessage(content = json, senderId = IdentityManager.getIdentityId(), type = "poll_create")
                )
            }
        }
    }

    fun editMessage(messageId: String, newContent: String) {
        viewModelScope.launch {
            db?.messageDao()?.updateContent(messageId, newContent)
            val convId = _conversationId.value ?: return@launch
            if (convId != "self_chat") {
                TransportManager.send(convId, MeshMessage(
                    content = newContent,
                    senderId = IdentityManager.getIdentityId(),
                    type = "edit",
                    quotedMessageId = messageId,
                    quotedMessageContent = null
                ))
            }
            refreshMessages(convId)
        }
    }

    fun deleteForAll(messageId: String) {
        viewModelScope.launch {
            db?.messageDao()?.markAsDeleted(messageId)
            val convId = _conversationId.value ?: return@launch
            if (convId != "self_chat") {
                TransportManager.send(convId, MeshMessage(
                    content = "",
                    senderId = IdentityManager.getIdentityId(),
                    type = "delete_for_all",
                    quotedMessageId = messageId,
                    quotedMessageContent = null
                ))
            }
            refreshMessages(convId)
        }
    }

    fun addReaction(messageId: String, emoji: String) {
        val convId = _conversationId.value ?: return
        viewModelScope.launch {
            db?.messageDao()?.updateReaction(messageId, emoji)
            refreshMessages(convId)
            if (convId != "self_chat") {
                TransportManager.send(convId, MeshMessage(
                    content = emoji,
                    senderId = IdentityManager.getIdentityId(),
                    type = "reaction",
                    quotedMessageId = messageId
                ))
            }
        }
    }

    fun removeReaction(messageId: String) {
        val convId = _conversationId.value ?: return
        viewModelScope.launch {
            db?.messageDao()?.updateReaction(messageId, null)
            refreshMessages(convId)
            if (convId != "self_chat") {
                TransportManager.send(convId, MeshMessage(
                    content = "",
                    senderId = IdentityManager.getIdentityId(),
                    type = "reaction",
                    quotedMessageId = messageId
                ))
            }
        }
    }

    fun togglePinMessage(messageId: String, pinned: Boolean) {
        val convId = _conversationId.value ?: return
        viewModelScope.launch {
            db?.messageDao()?.setPinned(messageId, pinned)
            refreshMessages(convId)
        }
    }

    suspend fun getConversationsOnce(): List<ConversationEntity> {
        return db?.conversationDao()?.getAllConversations()?.first() ?: emptyList()
    }

    fun forwardMessages(messageIds: List<String>, targetConversationId: String) {
        viewModelScope.launch {
            val messageDao = db?.messageDao() ?: return@launch
            messageIds.forEach { id ->
                val original = messageDao.getMessageById(id) ?: return@forEach
                val forwarded = original.copy(
                    id = UUID.randomUUID().toString(),
                    conversationId = targetConversationId,
                    timestamp = System.currentTimeMillis(),
                    isOwn = true,
                    status = 0,
                    reaction = null,
                    expireAt = null,
                    viewOnce = false,
                    quotedMessageId = null,
                    quotedMessageContent = null,
                    isEdited = false,
                    isDeleted = false,
                    isPinned = false
                )
                messageDao.insertMessage(forwarded)
            }
            val last = messageIds.lastOrNull()?.let { messageDao.getMessageById(it) }
            if (targetConversationId != "self_chat" && last != null) {
                TransportManager.send(
                    targetConversationId,
                    MeshMessage(
                        content = last.content,
                        senderId = IdentityManager.getIdentityId(),
                        timestamp = System.currentTimeMillis(),
                        type = "forward",
                        quotedMessageId = null,
                        quotedMessageContent = null
                    )
                )
            }
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            db?.messageDao()?.deleteMessage(messageId)
            val convId = _conversationId.value ?: return@launch
            refreshMessages(convId)
        }
    }
}
