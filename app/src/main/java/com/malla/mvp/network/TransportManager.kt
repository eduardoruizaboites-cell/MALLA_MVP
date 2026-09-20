package com.malla.mvp.network
import com.malla.mvp.data.entity.MeshMessage

import com.malla.mvp.core.engine.DiagnosticsLogger
import com.malla.mvp.identity.IdentityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.NonCancellable
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

    suspend fun send(contactId: String, message: MeshMessage): Boolean = withContext(Dispatchers.IO) {
        val tcpConnected = NetworkService.isContactConnected(contactId)
        DiagnosticsLogger.log(TAG, "Enviando mensaje a $contactId; TCP conectado=$tcpConnected")

        // 1. TCP primero si el peer está conectado por red (más rápido, sin límite de MTU)
        if (tcpConnected) {
            try {
                DiagnosticsLogger.log(TAG, "Usando TCP (peer conectado por red)")
                NetworkService.sendMessageToContact(contactId, message)
                updateStatus(TransportStatus.TCP_CONNECTED, "TCP")
                return@withContext true
            } catch (e: Exception) {
                DiagnosticsLogger.log(TAG, "TCP falló, cayendo a BLE: ${e.message}")
            }
        }

        // 2. BLE broadcast como fallback (mesh sin infraestructura)
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
                sentBle = withContext(NonCancellable) {
                    BleManager.connectAndWriteData(device, BleManager.MESSAGE_CHAR_UUID, payload)
                }
                DiagnosticsLogger.log(TAG, "BLE connectAndWriteData resultado=$sentBle")
            }
            if (!sentBle) {
                DiagnosticsLogger.log(TAG, "Fallback a broadcast/sendWithRetry")
                sentBle = BleTransport.broadcast(payload)
                DiagnosticsLogger.log(TAG, "BLE broadcast resultado=$sentBle")
                if (!sentBle && device != null) {
                    DiagnosticsLogger.log(TAG, "Reintentando sendWithRetry a ${device.address}")
                    sentBle = withContext(NonCancellable) {
                        BleTransport.sendWithRetry(device, payload)
                    }
                    DiagnosticsLogger.log(TAG, "BLE sendWithRetry resultado=$sentBle")
                }
            }
            if (sentBle) {
                updateStatus(TransportStatus.BLE_CONNECTED, "BLE")
                return@withContext true
            }
        } catch (e: Exception) {
            DiagnosticsLogger.log(TAG, "Error BLE: ${e.message}")
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
                return@withContext true
            }
        } catch (e: Exception) {
            DiagnosticsLogger.log(TAG, "Error Wi-Fi Direct: ${e.message}")
        }

        // 4. Sin canal activo en el momento: NetworkService lo deja en su cola
        // pendingMessages (memoria) y lo reenviará cuando el peer abra TCP.
        // Devolvemos false para que el caller NO marque el mensaje como entregado:
        // queda en status=0 (SENT) hasta que el ACK del receptor llegue.
        try {
            NetworkService.sendMessageToContact(contactId, message)
            updateStatus(TransportStatus.ERROR, "NONE")
            DiagnosticsLogger.log(TAG, "Sin canal activo: mensaje en cola pendiente (no entregado)")
        } catch (e: Exception) {
            DiagnosticsLogger.log(TAG, "Error encolando mensaje: ${e.message}")
            updateStatus(TransportStatus.ERROR, "NONE")
        }
        return@withContext false
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
                withContext(NonCancellable) {
                    BleManager.connectAndWriteData(device, BleManager.MESSAGE_CHAR_UUID, payload)
                }
            } else {
                BleTransport.broadcast(payload)
            }
        } catch (e: Exception) {
            DiagnosticsLogger.log(TAG, "Error enviando typing: ${e.message}")
        }
    }

}