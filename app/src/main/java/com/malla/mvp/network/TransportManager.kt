package com.malla.mvp.network

import com.malla.mvp.core.engine.DiagnosticsLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Orquestador de transporte de mensajería.
 * Decide el mejor canal disponible (TCP/IP, BLE, Wi-Fi Direct) y expone estado.
 */
object TransportManager {
    private const val TAG = "TransportManager"

    enum class TransportStatus {
        IDLE,
        TCP_CONNECTED,
        BLE_CONNECTED,
        WIFI_DIRECT_CONNECTED,
        ERROR
    }

    private val _status = MutableStateFlow(TransportStatus.IDLE)
    val status: StateFlow<TransportStatus> = _status

    private val _activeTransport = MutableStateFlow("TCP")
    val activeTransport: StateFlow<String> = _activeTransport

    fun start(context: android.content.Context) {
        DiagnosticsLogger.log(TAG, "TransportManager iniciado")
        // Se llama desde MeshChatService o MainActivity
    }

    fun updateStatus(newStatus: TransportStatus, transportName: String) {
        _status.value = newStatus
        _activeTransport.value = transportName
        DiagnosticsLogger.log(TAG, "Transporte activo: $transportName (${newStatus.name})")
    }

    suspend fun send(contactId: String, message: MeshMessage) {
        // 1. Intentar TCP/IP si hay clientes conectados
        try {
            NetworkService.sendMessageToContact(contactId, message)
            updateStatus(TransportStatus.TCP_CONNECTED, "TCP")
            return
        } catch (e: Exception) {
            DiagnosticsLogger.log(TAG, "Error TCP: ${e.message}")
        }

        // 2. Intentar BLE broadcast
        try {
            val payload = "${message.senderId}|${message.content}".toByteArray(Charsets.UTF_8)
            BleTransport.broadcast(payload)
            updateStatus(TransportStatus.BLE_CONNECTED, "BLE")
            return
        } catch (e: Exception) {
            DiagnosticsLogger.log(TAG, "Error BLE: ${e.message}")
        }

        // 3. Wi-Fi Direct (futuro: integración con socket)
        updateStatus(TransportStatus.ERROR, "NONE")
    }
}
