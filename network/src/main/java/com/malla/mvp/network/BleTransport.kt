package com.malla.mvp.network

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import com.malla.mvp.network.BleManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import com.malla.mvp.core.engine.LogBuffer
import com.malla.mvp.core.engine.DiagnosticsLogger
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private data class BleFragmentBuffer(
    val totalFrags: Int,
    val chunks: Array<ByteArray?>,
    var receivedCount: Int = 0
)

object BleTransport {
    private const val TAG = "BleTransport"
    /** Cap de fragmentos aceptados. 4095 × 508B (MTU 517) ≈ 2 MB; 4095 × 16B (MTU 23) ≈ 64 KB. */
    private const val MAX_TOTAL_FRAGS = 4095
    val SERVICE_UUID = UUID.fromString("0000abcd-0000-1000-8000-00805f9b34fb")
    val MESSAGE_CHAR_UUID = UUID.fromString("0000abcd-0003-1000-8000-00805f9b34fb")
    val INVITE_CHAR_UUID = UUID.fromString("0000abcd-0002-1000-8000-00805f9b34fb")
    private var appContext: Context? = null
    private var gattServer: BluetoothGattServer? = null
    private val connectedGatts = ConcurrentHashMap<String, BluetoothGatt>()
    private val writeConfirmations = ConcurrentHashMap<String, kotlinx.coroutines.CancellableContinuation<Boolean>>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val incomingMessages = MutableSharedFlow<ByteArray>(extraBufferCapacity = 64)
    val messages: SharedFlow<ByteArray> = incomingMessages.asSharedFlow()
    private val incomingInvitationPayloads = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val invitationPayloads: SharedFlow<String> = incomingInvitationPayloads.asSharedFlow()
    private val incomingAcceptancePayloads = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val acceptancePayloads: SharedFlow<String> = incomingAcceptancePayloads.asSharedFlow()

    private val fragmentBuffers = ConcurrentHashMap<String, BleFragmentBuffer>()

    /**
     * Reensambla fragmentos BLE.
     * Formato del payload: fragIdx (1 byte) + totalFrags (1 byte) + chunk de datos.
     * Si totalFrags <= 1, emite directo. Si no, acumula y emite al completar.
     */
    private fun reassembleFragments(device: BluetoothDevice, value: ByteArray): ByteArray? {
        if (value.size < 4) return null
        // Header de 4 bytes: [idxHi][idxLo][totalHi][totalLo]
        val fragIdx = ((value[0].toInt() and 0xFF) shl 8) or (value[1].toInt() and 0xFF)
        val totalFrags = ((value[2].toInt() and 0xFF) shl 8) or (value[3].toInt() and 0xFF)
        val payload = value.copyOfRange(4, value.size)

        if (totalFrags <= 1) return payload

        if (totalFrags > MAX_TOTAL_FRAGS) {
            DiagnosticsLogger.log(TAG, "Rechazado de ${device.address}: totalFrags=$totalFrags (>$MAX_TOTAL_FRAGS)")
            return null
        }

        val key = device.address
        return synchronized(fragmentBuffers) {
            var buffer = fragmentBuffers[key]
            // Si el buffer contiene fragmentos huérfanos de un mensaje anterior
            // (invitación previa incompleta, mensaje perdido), y llega un fragmento
            // de un mensaje distinto, reseteamos para evitar corrupción cruzada.
            // Evidencia: Iteración 13 mostró JSON de typing fusionado con timestamp
            // corrupto por mezcla de fragmentos de dos payloads consecutivos.
            if (buffer != null && buffer.totalFrags != totalFrags) {
                DiagnosticsLogger.log(TAG, "Buffer reseteado para $key: totalFrags ${buffer.totalFrags}→$totalFrags (recibidos ${buffer.receivedCount}/${buffer.totalFrags})")
                fragmentBuffers.remove(key)
                buffer = null
            }
            if (buffer == null) {
                buffer = BleFragmentBuffer(totalFrags, arrayOfNulls(totalFrags))
                fragmentBuffers[key] = buffer
            }
            val b = buffer
            if (fragIdx < b.totalFrags && b.chunks[fragIdx] == null) {
                b.chunks[fragIdx] = payload
                b.receivedCount++
            }
            if (b.receivedCount == b.totalFrags) {
                fragmentBuffers.remove(key)
                var totalSize = 0
                for (c in b.chunks) totalSize += c?.size ?: 0
                val out = ByteArray(totalSize)
                var offset = 0
                for (c in b.chunks) {
                    if (c != null) {
                        System.arraycopy(c, 0, out, offset, c.size)
                        offset += c.size
                    }
                }
                out
            } else {
                null
            }
        }
    }

    private fun handleFragment(device: BluetoothDevice, value: ByteArray) {
        val assembled = reassembleFragments(device, value) ?: return
        DiagnosticsLogger.log(TAG, "Reensamblado ${assembled.size}B de ${device.address}")
        incomingMessages.tryEmit(assembled)
    }

    private fun hasBlePermissions(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED
    }

    private var started = false
    private var serverStarted = false
    private var startingServer = false
    private var discoveryJob: Job? = null

    fun start(context: Context) {
        appContext = context.applicationContext
        // Bug F (iter 43): el server debe poder re-intentarse si la primera llamada
        // llegó antes de que los permisos BLE estén concedidos.
        if (!serverStarted) {
            startServer(context)
        }
        if (started) return
        started = true
        // Observar dispositivos BLE detectados para conectar GATT automáticamente
        discoveryJob = scope.launch {
            BleManager.foundBluetoothDevices.collect { devices ->
                // En Android 11+ adapter.address devuelve 02:00:00:00:00:00 (placeholder).
                // El filtro por MAC no sirve. Filtramos por el userId del advertising:
                // si el device está en nearbyUsers con nuestro propio userId, es un eco.
                val myId = try { com.malla.mvp.identity.IdentityManager.getIdentityId() } catch (_: Exception) { null }
                val selfMac = try {
                    val adapter = BleManager.getAdapter()
                    adapter?.address?.takeIf { it != "02:00:00:00:00:00" }
                } catch (_: SecurityException) { null }
                devices.forEach { device ->
                    if (selfMac != null && device.address == selfMac) {
                        DiagnosticsLogger.log(TAG, "Ignorando auto-conexión (MAC coincide): ${device.address}")
                        return@forEach
                    }
                    if (myId != null) {
                        val nearby = com.malla.mvp.network.ProximityEngine.nearbyUsers.value
                            .firstOrNull { it.bluetoothDevice?.address == device.address }
                        if (nearby?.userId != null && nearby.userId == myId) {
                            DiagnosticsLogger.log(TAG, "Ignorando auto-conexión (userId propio): ${device.address}")
                            return@forEach
                        }
                    }
                    if (!connectedGatts.containsKey(device.address)) {
                        connectGatt(device)
                    }
                }
            }
        }
        LogBuffer.add(TAG, "BleTransport iniciado (GATT server + auto-conexión)")
        DiagnosticsLogger.log(TAG, "BleTransport iniciado (GATT server + auto-conexión)")
    }

    private fun connectGatt(device: BluetoothDevice) {
        val context = appContext ?: return
        if (!hasBlePermissions(context)) return
        try {
            Log.i("BleTransport", "Intentando conectar GATT a ${device.address}")
            val gatt = device.connectGatt(context, true, object : BluetoothGattCallback() {
                override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                    Log.i("BleTransport", "Estado GATT ${device.address}: $newState")
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        gatt.discoverServices()
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        connectedGatts.remove(device.address)
                        gatt.close()
                    }
                }
                override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        connectedGatts[device.address] = gatt
                        gatt.requestMtu(517)
                        LogBuffer.add("BleTransport", "GATT conectado a ${device.address}")
                        DiagnosticsLogger.log("BleTransport", "GATT conectado a ${device.address}")
                    } else {
                        gatt.disconnect()
                    }
                }
                override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
                    val effective = if (status == BluetoothGatt.GATT_SUCCESS) mtu else 23
                    LogBuffer.add("BleTransport", "MTU con ${device.address}: $effective")
                    DiagnosticsLogger.log("BleTransport", "MTU con ${device.address}: $effective")
                }
            })
            // Mantener referencia para evitar GC
        } catch (e: SecurityException) {
            LogBuffer.add("BleTransport", "Permiso denegado para conectar GATT")
            DiagnosticsLogger.log("BleTransport", "Permiso denegado para conectar GATT")
        }
    }

    private fun startServer(context: Context) {
        if (serverStarted || startingServer) return
        if (!BleManager.hasConnectPermission(context)) {
            LogBuffer.add("BleTransport", "GATT server NO iniciado: falta BLUETOOTH_CONNECT")
            DiagnosticsLogger.log("BleTransport", "GATT server NO iniciado: falta BLUETOOTH_CONNECT")
            return
        }
        val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = btManager.adapter ?: return
        startingServer = true
        // Bug F capa 2 (iter 44): en Android 14+ (y confirmado en Cubot API 36), llamar
        // addService() inmediatamente despues de openGattServer() devuelve false porque
        // el server interno aun no esta listo. Resultado: server "abierto" pero sin servicios
        // registrados; los clientes solo ven los genericos del sistema (GAP + GATT).
        // Fix: delay de 500ms entre openGattServer y addService, verificar el retorno,
        // retry unico si falla, y logs honestos.
        scope.launch {
            try {
                val server = btManager.openGattServer(context, gattServerCallback)
                if (server == null) {
                    DiagnosticsLogger.log("BleTransport", "openGattServer devolvio null")
                    return@launch
                }
                gattServer = server
                delay(500L)
                val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
                val char = BluetoothGattCharacteristic(
                    MESSAGE_CHAR_UUID,
                    BluetoothGattCharacteristic.PROPERTY_READ or
                        BluetoothGattCharacteristic.PROPERTY_WRITE or
                        BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE or
                        BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                    BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE
                )
                val inviteChar = BluetoothGattCharacteristic(
                    INVITE_CHAR_UUID,
                    BluetoothGattCharacteristic.PROPERTY_WRITE or
                        BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE or
                        BluetoothGattCharacteristic.PROPERTY_READ,
                    BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE
                )
                service.addCharacteristic(char)
                service.addCharacteristic(inviteChar)
                val ok = server.addService(service)
                DiagnosticsLogger.log("BleTransport", "addService(service) retorno=$ok")
                if (!ok) {
                    delay(500L)
                    val ok2 = server.addService(service)
                    DiagnosticsLogger.log("BleTransport", "addService(service) retry retorno=$ok2")
                    if (!ok2) {
                        DiagnosticsLogger.log("BleTransport", "FALLO CRITICO: MALLA service no registrado tras 2 intentos")
                        return@launch
                    }
                }
                serverStarted = true
                LogBuffer.add("BleTransport", "GATT server abierto con SERVICE_UUID=$SERVICE_UUID (addService OK)")
                DiagnosticsLogger.log("BleTransport", "GATT server abierto con SERVICE_UUID=$SERVICE_UUID (addService OK)")
            } catch (e: Exception) {
                DiagnosticsLogger.log("BleTransport", "startServer excepcion: ${e.javaClass.simpleName}: ${e.message}")
            } finally {
                startingServer = false
            }
        }
    }

    fun broadcast(data: ByteArray): Boolean {
        // Header de 4B requerido por reassembleFragments del receptor.
        // Sin esto, el receptor lee los primeros 4B del payload como header
        // y rechaza con totalFrags absurdo (bug confirmado 2026-09-19:
        // "se perdió este mensaje" -> totalFrags=29541 = 0x7365 = "se").
        if (data.size + 4 > 512) {
            DiagnosticsLogger.log("BleTransport",
                "broadcast rechaza payload ${data.size}B + 4B header > 512 (limite ATT)")
            return false
        }
        val framed = ByteArray(data.size + 4)
        framed[0] = 0
        framed[1] = 0
        framed[2] = 0
        framed[3] = 1
        System.arraycopy(data, 0, framed, 4, data.size)

        var sent = false
        connectedGatts.keys.forEach { address ->
            connectedGatts[address]?.let { gatt ->
                writeCharacteristic(gatt, framed)
                sent = true
            }
        }
        return sent
    }

    /**
     * Envía datos con confirmación de escritura y reintentos.
     * Espera el callback onCharacteristicWrite con un timeout.
     */
    suspend fun sendWithRetry(device: BluetoothDevice, data: ByteArray, maxRetries: Int = 3): Boolean {
        val context = appContext ?: return false
        if (!hasBlePermissions(context)) return false

        var gatt = connectedGatts[device.address]
        if (gatt == null) {
            gatt = connectGattAndWait(device) ?: return false
        }

        repeat(maxRetries) { attempt ->
            val result = writeCharacteristicWithoutConfirmation(gatt, data)
            if (result) return true
            delay(1000L * (attempt + 1))
        }
        return false
    }

    private suspend fun connectGattAndWait(device: BluetoothDevice): BluetoothGatt? =
        suspendCancellableCoroutine { continuation ->
            val context = appContext ?: run { continuation.resume(null); return@suspendCancellableCoroutine }
            val gatt = device.connectGatt(context, true, object : BluetoothGattCallback() {
                override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        gatt.discoverServices()
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        if (!continuation.isCompleted) continuation.resume(null)
                        connectedGatts.remove(device.address)
                        gatt.close()
                    }
                }
                override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        connectedGatts[device.address] = gatt
                        gatt.requestMtu(517)
                        if (!continuation.isCompleted) continuation.resume(gatt)
                    } else {
                        gatt.disconnect()
                        if (!continuation.isCompleted) continuation.resume(null)
                    }
                }
                override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
                    val effective = if (status == BluetoothGatt.GATT_SUCCESS) mtu else 23
                    LogBuffer.add("BleTransport", "MTU con ${device.address}: $effective")
                    DiagnosticsLogger.log("BleTransport", "MTU con ${device.address}: $effective")
                }
            })
        }

    private suspend fun writeCharacteristicWithoutConfirmation(gatt: BluetoothGatt, data: ByteArray): Boolean {
        return try {
            val service = gatt.getService(SERVICE_UUID) ?: return false
            val char = service.getCharacteristic(MESSAGE_CHAR_UUID) ?: return false
            char.value = data
            char.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            gatt.writeCharacteristic(char)
        } catch (e: Exception) {
            false
        }
    }

    private fun handleWriteConfirmation(address: String, success: Boolean) {
        writeConfirmations.remove(address)?.let { cont ->
            if (cont.isActive) {
                cont.resume(success)
            }
        }
    }

    fun stop() {
        discoveryJob?.cancel()
        discoveryJob = null
        started = false
        connectedGatts.values.forEach { gatt ->
            try { gatt.disconnect() } catch (_: Exception) {}
            try { gatt.close() } catch (_: Exception) {}
        }
        connectedGatts.clear()
        gattServer?.close()
        gattServer = null
        LogBuffer.add(TAG, "BleTransport detenido")
    }

    fun sendInvitation(device: BluetoothDevice, payload: ByteArray) {
        val context = appContext ?: return
        if (!hasBlePermissions(context)) return
        val existing = connectedGatts[device.address]
        if (existing != null) {
            writeInvitationCharacteristic(existing, payload)
            return
        }
        val gattRef = device.connectGatt(context, true, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    gatt.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    connectedGatts.remove(device.address)
                    try { gatt.close() } catch (_: Exception) {}
                }
            }
            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    connectedGatts[device.address] = gatt
                    // Negociar MTU antes de escribir la invitación (mismo fix que en Iteración 1
                    // para mensajes; este camino se quedó sin él).
                    val ok = gatt.requestMtu(517)
                    if (!ok) {
                        // Si requestMtu no está soportado, escribir igual con MTU por defecto.
                        writeInvitationCharacteristic(gatt, payload)
                    }
                } else {
                    gatt.disconnect()
                }
            }
            override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
                val effective = if (status == BluetoothGatt.GATT_SUCCESS) mtu else 23
                LogBuffer.add("BleTransport", "MTU invite con ${device.address}: $effective")
                DiagnosticsLogger.log("BleTransport", "MTU invite con ${device.address}: $effective")
                writeInvitationCharacteristic(gatt, payload)
            }
        })
    }

    private fun writeInvitationCharacteristic(gatt: BluetoothGatt, data: ByteArray) {
        val service = gatt.getService(SERVICE_UUID) ?: run {
            Log.e("BleTransport", "Servicio no encontrado")
            return
        }
        val char = service.getCharacteristic(INVITE_CHAR_UUID) ?: run {
            Log.e("BleTransport", "Característica de invitación no encontrada")
            return
        }
        char.value = data
        char.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        val success = gatt.writeCharacteristic(char)
        Log.i("BleTransport", "Escritura de invitación iniciada: $success")
        DiagnosticsLogger.log("BleTransport", "Escritura de invitación iniciada: $success")
    }

    fun connectAndSend(device: BluetoothDevice, data: ByteArray) {
        val context = appContext ?: return
        if (!hasBlePermissions(context)) return
        var gatt = connectedGatts[device.address]
        if (gatt != null) {
            writeCharacteristic(gatt, data)
            return
        }
        gatt = device.connectGatt(context, true, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    gatt.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    connectedGatts.remove(device.address)
                    gatt.close()
                }
            }
            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    connectedGatts[device.address] = gatt
                    writeCharacteristic(gatt, data)
                } else {
                    gatt.disconnect()
                }
            }
        })
    }

    private fun writeCharacteristic(gatt: BluetoothGatt, data: ByteArray) {
        val service = gatt.getService(SERVICE_UUID) ?: return
        val char = service.getCharacteristic(MESSAGE_CHAR_UUID) ?: return
        char.value = data
        char.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        gatt.writeCharacteristic(char)
    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {
        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray
        ) {
            DiagnosticsLogger.log(TAG, "Write Request recibido de ${device.address}: char=${characteristic.uuid}, responseNeeded=$responseNeeded, size=${value.size}")
            when (characteristic.uuid) {
                MESSAGE_CHAR_UUID -> {
                    handleFragment(device, value)
                    if (responseNeeded) {
                        gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null)
                    }
                }
                INVITE_CHAR_UUID -> {
                    val reassembled = reassembleFragments(device, value)
                    if (reassembled != null) {
                        val payload = String(reassembled, Charsets.UTF_8)
                        // Iter 47: diferenciar invitacion vs aceptacion por prefijo.
                        // La aceptacion viaja por el mismo canal GATT que la invitacion
                        // (antes usaba advertising serviceData y el scanner solo lee
                        // manufacturerData — nunca llegaba).
                        if (payload.startsWith("ACCEPT|")) {
                            LogBuffer.add(TAG, "Aceptacion BLE recibida: $payload")
                            DiagnosticsLogger.log(TAG, "Aceptacion BLE recibida: $payload")
                            incomingAcceptancePayloads.tryEmit(payload)
                        } else {
                            LogBuffer.add(TAG, "Invitacion BLE recibida: $payload")
                            DiagnosticsLogger.log(TAG, "Invitacion BLE recibida: $payload")
                            incomingInvitationPayloads.tryEmit(payload)
                        }
                    } else {
                        DiagnosticsLogger.log(TAG, "Invitacion fragmentada incompleta de ${device.address}")
                    }
                    if (responseNeeded) {
                        gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null)
                    }
                }
            }
        }
    }
}
