package com.malla.mvp.network
import com.malla.mvp.data.entity.MeshMessage

import com.malla.mvp.core.engine.DiagnosticsLogger
import com.malla.mvp.identity.IdentityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.malla.mvp.network.BleManager
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

    suspend fun send(contactId: String, message: MeshMessage) = withContext(Dispatchers.IO) {
        DiagnosticsLogger.log(TAG, "Enviando mensaje a $contactId; TCP conectado=${NetworkService.isContactConnected(contactId)}")
        // 1. Intentar BLE broadcast primero (más confiable en mesh sin infraestructura)
        try {
            val payload = org.json.JSONObject().apply {
                put("senderId", message.senderId)
                put("content", message.content)
                put("messageId", message.messageId ?: "")
                put("type", message.type ?: "chat")
                put("timestamp", message.timestamp)
            }.toString().toByteArray(Charsets.UTF_8)
            DiagnosticsLogger.log(TAG, "Intentando enviar por BLE: ${message.content.take(30)}")
            var sentBle = false
            val nearby = ProximityEngine.nearbyUsers.value.firstOrNull { it.userId == contactId }
            val allNearby = ProximityEngine.nearbyUsers.value
            DiagnosticsLogger.log(
                TAG,
                "Contacto=$contactId | nearbyUsers=${allNearby.map { "${it.userId}:${it.displayName}:${it.bluetoothDevice?.address}" }}"
            )
            if (nearby == null) {
                DiagnosticsLogger.log(
                    TAG,
                    "AVISO: contacto $contactId NO está en nearbyUsers. Fallback a firstOrNull (posible envío a device incorrecto)."
                )
            }
            val device = nearby?.bluetoothDevice ?: BleManager.foundBluetoothDevices.value.firstOrNull()
            DiagnosticsLogger.log(TAG, "Dispositivo BLE seleccionado: ${device?.address ?: "null"} para contacto $contactId")
            if (device != null) {
                // Método directo más confiable: conectar y escribir característica
                DiagnosticsLogger.log(TAG, "Intentando connectAndWriteData a ${device.address}")
                sentBle = BleManager.connectAndWriteData(device, BleManager.MESSAGE_CHAR_UUID, payload)
                DiagnosticsLogger.log(TAG, "BLE connectAndWriteData resultado=$sentBle")
            }
            if (!sentBle) {
                DiagnosticsLogger.log(TAG, "Fallback a broadcast/sendWithRetry")
                sentBle = BleTransport.broadcast(payload)
                DiagnosticsLogger.log(TAG, "BLE broadcast resultado=$sentBle")
                if (!sentBle && device != null) {
                    DiagnosticsLogger.log(TAG, "Reintentando sendWithRetry a ${device.address}")
                    sentBle = BleTransport.sendWithRetry(device, payload)
                    DiagnosticsLogger.log(TAG, "BLE sendWithRetry resultado=$sentBle")
                }
            }
            if (sentBle) {
                updateStatus(TransportStatus.BLE_CONNECTED, "BLE")
                return@withContext
            }
        } catch (e: Exception) {
            DiagnosticsLogger.log(TAG, "Error BLE: ${e.message}")
        }

        // 2. Intentar TCP/IP si hay handler conectado
        if (NetworkService.isContactConnected(contactId)) {
            DiagnosticsLogger.log(TAG, "Enviando por TCP a $contactId")
            NetworkService.sendMessageToContact(contactId, message)
            updateStatus(TransportStatus.TCP_CONNECTED, "TCP")
            return@withContext
        }

        // 3. Intentar Wi-Fi Direct broadcast
        try {
            val wfdPayload = org.json.JSONObject().apply {
                put("senderId", message.senderId)
                put("content", message.content)
                put("messageId", message.messageId ?: "")
                put("type", message.type ?: "chat")
                put("timestamp", message.timestamp)
            }.toString()
            val sentWfd = WifiDirectManager.broadcast(wfdPayload)
            if (sentWfd) {
                updateStatus(TransportStatus.WIFI_DIRECT_CONNECTED, "Wi-Fi Direct")
                return@withContext
            }
        } catch (e: Exception) {
            DiagnosticsLogger.log(TAG, "Error Wi-Fi Direct: ${e.message}")
        }

        // 4. Si no hay canal activo, encolar en NetworkService para entrega cuando TCP esté disponible
        try {
            NetworkService.sendMessageToContact(contactId, message)
            updateStatus(TransportStatus.ERROR, "NONE")
            DiagnosticsLogger.log(TAG, "Sin canal activo: mensaje encolado en TCP")
        } catch (e: Exception) {
            DiagnosticsLogger.log(TAG, "Error encolando mensaje: ${e.message}")
            updateStatus(TransportStatus.ERROR, "NONE")
        }
    }
    suspend fun sendTyping(contactId: String, isTyping: Boolean) = withContext(Dispatchers.IO) {
        try {
            val payload = org.json.JSONObject().apply {
                put("senderId", IdentityManager.getIdentityId())
                put("type", "typing")
                put("content", if (isTyping) "1" else "0")
                put("timestamp", System.currentTimeMillis())
            }.toString().toByteArray(Charsets.UTF_8)
            val nearby = ProximityEngine.nearbyUsers.value.firstOrNull { it.userId == contactId }
            val device = nearby?.bluetoothDevice ?: BleManager.foundBluetoothDevices.value.firstOrNull()
            DiagnosticsLogger.log(TAG, "Dispositivo BLE seleccionado: ${device?.address ?: "null"} para contacto $contactId")
            if (device != null) {
                BleManager.connectAndWriteData(device, BleManager.MESSAGE_CHAR_UUID, payload)
            } else {
                BleTransport.broadcast(payload)
            }
        } catch (e: Exception) {
            DiagnosticsLogger.log(TAG, "Error enviando typing: ${e.message}")
        }
    }

}