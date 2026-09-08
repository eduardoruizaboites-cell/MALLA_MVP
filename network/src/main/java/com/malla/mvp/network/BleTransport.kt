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

object BleTransport {
    private const val TAG = "BleTransport"
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

    private fun hasBlePermissions(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED
    }

    private var started = false
    private var discoveryJob: Job? = null

    fun start(context: Context) {
        if (started) return
        started = true
        appContext = context.applicationContext
        startServer(context)
        // Observar dispositivos BLE detectados para conectar GATT automáticamente
        discoveryJob = scope.launch {
            BleManager.foundBluetoothDevices.collect { devices ->
                devices.forEach { device ->
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
                        LogBuffer.add("BleTransport", "GATT conectado a ${device.address}")
                        DiagnosticsLogger.log("BleTransport", "GATT conectado a ${device.address}")
                    } else {
                        gatt.disconnect()
                    }
                }
            })
            // Mantener referencia para evitar GC
        } catch (e: SecurityException) {
            LogBuffer.add("BleTransport", "Permiso denegado para conectar GATT")
            DiagnosticsLogger.log("BleTransport", "Permiso denegado para conectar GATT")
        }
    }

    private fun startServer(context: Context) {
        if (!hasBlePermissions(context)) return
        val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = btManager.adapter ?: return
        gattServer = btManager.openGattServer(context, gattServerCallback).apply {
            val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
            val char = BluetoothGattCharacteristic(
                MESSAGE_CHAR_UUID,
                BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE
            )
            val inviteChar = BluetoothGattCharacteristic(
                INVITE_CHAR_UUID,
                BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE
            )
            service.addCharacteristic(char)
            service.addCharacteristic(inviteChar)
            addService(service)
        }
    }

    fun broadcast(data: ByteArray): Boolean {
        var sent = false
        connectedGatts.keys.forEach { address ->
            connectedGatts[address]?.let { gatt ->
                writeCharacteristic(gatt, data)
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
            val result = writeCharacteristicWithConfirmation(gatt, data)
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
                        if (!continuation.isCompleted) continuation.resume(gatt)
                    } else {
                        gatt.disconnect()
                        if (!continuation.isCompleted) continuation.resume(null)
                    }
                }
            })
        }

    private suspend fun writeCharacteristicWithConfirmation(gatt: BluetoothGatt, data: ByteArray): Boolean =
        suspendCancellableCoroutine { continuation ->
            try {
                val service = gatt.getService(SERVICE_UUID) ?: run {
                    continuation.resume(false); return@suspendCancellableCoroutine
                }
                val char = service.getCharacteristic(MESSAGE_CHAR_UUID) ?: run {
                    continuation.resume(false); return@suspendCancellableCoroutine
                }
                char.value = data
                char.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                val key = gatt.device.address
                writeConfirmations[key] = continuation
                val success = gatt.writeCharacteristic(char)
                if (!success) {
                    writeConfirmations.remove(key)
                    continuation.resume(false)
                }
            } catch (e: Exception) {
                continuation.resume(false)
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
        var gatt = connectedGatts[device.address]
        if (gatt != null) {
            writeInvitationCharacteristic(gatt, payload)
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
                    writeInvitationCharacteristic(gatt, payload)
                } else {
                    gatt.disconnect()
                }
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
        char.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
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
        char.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
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
            when (characteristic.uuid) {
                MESSAGE_CHAR_UUID -> {
                    incomingMessages.tryEmit(value)
                    if (responseNeeded) {
                        gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null)
                    }
                }
                INVITE_CHAR_UUID -> {
                    val payload = String(value, Charsets.UTF_8)
                    LogBuffer.add(TAG, "Invitación BLE recibida: $payload")
                    DiagnosticsLogger.log(TAG, "Invitación BLE recibida: $payload")
                    // Emitir para que InvitationManager lo procese desde el módulo :app
                    incomingInvitationPayloads.tryEmit(payload)
                    if (responseNeeded) {
                        gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null)
                    }
                }
            }
        }
    }
}
