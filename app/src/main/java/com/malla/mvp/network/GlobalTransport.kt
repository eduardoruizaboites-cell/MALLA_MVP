package com.malla.mvp.network

import com.malla.mvp.core.network.INetworkService
import com.malla.mvp.core.network.MeshMessage as CoreMeshMessage
import com.malla.mvp.identity.IdentityManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.*

/**
 * Transporte global basado en WebRTC DataChannel.
 * Implementa INetworkService para ser usado por CascadeRouter.
 */
object GlobalTransport : INetworkService {
    private val _connectionState = MutableStateFlow(false)
    override val connectionState: Flow<Boolean> = _connectionState

    private val listeners = mutableListOf<(CoreMeshMessage) -> Unit>()

    override suspend fun sendMeshMessage(message: CoreMeshMessage): Result<Unit> {
        return try {
            val contactId = message.senderId
            val meshMsg = MeshMessage(
                content = message.content,
                senderId = IdentityManager.getIdentityId(),
                timestamp = message.timestamp,
                type = "chat"
            )
            WebRtcDataManager.sendToContact(contactId, meshMsg)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun addMessageListener(listener: (CoreMeshMessage) -> Unit) {
        listeners.add(listener)
    }

    override fun removeMessageListener(listener: (CoreMeshMessage) -> Unit) {
        listeners.remove(listener)
    }

    fun start() {
        WebRtcDataManager.start()
        // Escuchar mensajes entrantes y reenviar a listeners
        CoroutineScope(Dispatchers.IO).launch {
            WebRtcDataManager.incomingMessages.collect { msg ->
                val coreMsg = CoreMeshMessage(
                    senderId = msg.senderId,
                    content = msg.content,
                    timestamp = msg.timestamp,
                    type = 0
                )
                listeners.forEach { it(coreMsg) }
                _connectionState.value = true
            }
        }
    }
}
