package com.malla.mvp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.malla.mvp.App
import com.malla.mvp.data.AppDatabase
import com.malla.mvp.data.entity.MessageEntity
import com.malla.mvp.identity.IdentityManager
import com.malla.mvp.crypto.CryptoEngine
import com.malla.mvp.crypto.SessionCipher
import com.malla.mvp.core.data.MessageMapper
import com.malla.mvp.core.data.MessageData
import com.malla.mvp.events.MallaEventBus
import com.malla.mvp.data.entity.PollEntity
import com.malla.mvp.data.entity.PollOptionEntity
import com.malla.mvp.network.MeshMessage
import com.malla.mvp.network.NetworkService
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
        val convId = msg.senderId  // El senderId ahora es el contactId real
        if (convId == _conversationId.value) {
            // Mensaje para la conversación actual
            val msgEntity = MessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = convId,
                content = msg.content,
                timestamp = System.currentTimeMillis(),
                isOwn = false,
                quotedMessageId = msg.quotedMessageId,
                quotedMessageContent = msg.quotedMessageContent,
                expireAt = msg.expireAt,
                viewOnce = msg.viewOnce
            )
            db?.messageDao()?.insertMessage(msgEntity)
            refreshMessages(convId)
        } else {
            // Si no es la conversación actual, asegurar que exista la conversación
            val conversationDao = db?.conversationDao()
            val existing = conversationDao?.getConversationById(convId)
            if (existing == null) {
                val newConv = com.malla.mvp.data.entity.ConversationEntity(
                    id = convId,
                    title = convId,  // Temporal; se actualizará cuando se obtenga el nombre
                    timestamp = System.currentTimeMillis()
                )
                conversationDao?.insertConversation(newConv)
            }
            val msgEntity = MessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = convId,
                content = msg.content,
                timestamp = System.currentTimeMillis(),
                isOwn = false,
                quotedMessageId = msg.quotedMessageId,
                quotedMessageContent = msg.quotedMessageContent,
                expireAt = msg.expireAt,
                viewOnce = msg.viewOnce
            )
            db?.messageDao()?.insertMessage(msgEntity)
        }
    }

    fun updateInputText(text: String) {
        _inputText.value = text
    }

    fun startRecording() { _isRecording.value = true }
    fun stopRecording() { _isRecording.value = false }

    fun isMessageNew(timestamp: Long): Boolean {
        return timestamp > lastMessageTimestamp
    }

    fun sendZumbido() {
        val convId = _conversationId.value ?: return
        viewModelScope.launch {
            MallaEventBus.zumbidoReceived.tryEmit(
                MeshMessage(content = "📳 Zumbido", senderId = "self", type = "zumbido")
            )
            if (convId != "self_chat") {
                NetworkService.sendMessageToContact(convId, MeshMessage(content = "📳 Zumbido", senderId = "self", type = "zumbido"))
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
            val msg = MessageEntity(
                id = UUID.randomUUID().toString(),
                conversationId = convId,
                content = finalContent,
                isOwn = true,
                expireAt = expireAt,
                mediaUri = mediaUri,
                viewOnce = viewOnce,
                quotedMessageId = quotedMessageId,
                quotedMessageContent = quotedMessageContent,
                encrypted = encryptionEnabled
            )
            db?.messageDao()?.insertMessage(msg)
            if (convId != "self_chat") {
                // Enviar dirigido al contactId correcto (convId)
                NetworkService.sendMessageToContact(convId, MeshMessage(
                    content = finalContent,
                    senderId = IdentityManager.getIdentityId(),
                    timestamp = System.currentTimeMillis(),
                    quotedMessageId = quotedMessageId,
                    quotedMessageContent = quotedMessageContent,
                    expireAt = expireAt,
                    viewOnce = viewOnce
                ))
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
                NetworkService.sendMessageToContact(
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
                NetworkService.sendMessageToContact(
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
                NetworkService.sendMessageToContact(convId, MeshMessage(
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
                NetworkService.sendMessageToContact(convId, MeshMessage(
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

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            db?.messageDao()?.deleteMessage(messageId)
            val convId = _conversationId.value ?: return@launch
            refreshMessages(convId)
        }
    }
}
