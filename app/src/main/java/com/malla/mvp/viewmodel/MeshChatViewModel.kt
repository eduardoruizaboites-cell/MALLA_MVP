package com.malla.mvp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.malla.mvp.App
import com.malla.mvp.data.AppDatabase
import com.malla.mvp.data.entity.MessageEntity
import javax.crypto.SecretKey
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
                NetworkService.sendMessage(
                    MeshMessage(content = "📳 Zumbido", senderId = "self", type = "zumbido")
                )
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
                NetworkService.sendMessage(
                    MeshMessage(
                        content = msg.content,
                        senderId = "self",
                        timestamp = System.currentTimeMillis(),
                        quotedMessageId = quotedMessageId,
                        quotedMessageContent = quotedMessageContent
                    )
                )
            }
            _inputText.value = ""
            refreshMessages(convId)
        }
    }

    fun votePoll(optionId: String, pollId: String) {
        viewModelScope.launch {
            db?.pollDao()?.incrementVoteCount(optionId, 1)
            val currentOptions = _optionsMap.value[pollId] ?: return@launch
            val updated = currentOptions.map { opt ->
                if (opt.id == optionId) opt.copy(voteCount = opt.voteCount + 1) else opt
            }
            _optionsMap.value = _optionsMap.value + (pollId to updated)
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
            loadPolls(convId)
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
