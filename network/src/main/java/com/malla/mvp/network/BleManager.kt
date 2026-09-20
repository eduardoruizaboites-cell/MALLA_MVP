package com.malla.mvp.network
import com.malla.mvp.core.engine.LogBuffer
import com.malla.mvp.core.engine.DiagnosticsLogger

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.delay
import java.util.UUID
import kotlin.coroutines.resume
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object BleManager {
    private const val TAG = "BleManager"
    /** Cap de fragmentos aceptados. 4095 × 508B (MTU 517) ≈ 2 MB; 4095 × 16B (MTU 23) ≈ 64 KB. */
    private const val MAX_TOTAL_FRAGS = 4095
    private val serviceUuid = UUID.fromString("0000abcd-0000-1000-8000-00805f9b34fb")
    val MESSAGE_CHAR_UUID = UUID.fromString("0000abcd-0003-1000-8000-00805f9b34fb")
    private val ipCharacteristicUuid = UUID.fromString("0000abcd-0001-1000-8000-00805f9b34fb")
    private val invitationCharacteristicUuid = UUID.fromString("0000abcd-0002-1000-8000-00805f9b34fb")
    private var adapter: BluetoothAdapter? = null
    private var scanner: BluetoothLeScanner? = null
    private var advertiser: BluetoothLeAdvertiser? = null
    fun getAdapter(): BluetoothAdapter? = adapter
    private var isAdvertising = false
    private var isScanningActive = false
    private var appContext: Context? = null
    private var gattServer: BluetoothGattServer? = null
    private val gattCache = java.util.concurrent.ConcurrentHashMap<String, BluetoothGatt>()
    private val mtuCache = java.util.concurrent.ConcurrentHashMap<String, Int>()
    private val writeMutexes = java.util.concurrent.ConcurrentHashMap<String, Mutex>()
    private val writeAckDeferreds = java.util.concurrent.ConcurrentHashMap<String, kotlinx.coroutines.CompletableDeferred<Boolean>>()

    private val _foundDevices = MutableStateFlow<List<String>>(emptyList())
    val foundDevices: StateFlow<List<String>> = _foundDevices
    private val _foundBluetoothDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val foundBluetoothDevices: StateFlow<List<BluetoothDevice>> = _foundBluetoothDevices

    // ---------- Nuevo: callbacks para ProximityEngine ----------
    private var proximityScanCallback: ((token: String, userId: String?, name: String, seed: Int, strength: Int, device: BluetoothDevice) -> Unit)? = null
    private var acceptanceCallback: ((acceptorName: String, acceptorAvatarSeed: Int) -> Unit)? = null
    private var isProximityScanning = false
    private var isProximityAdvertising = false


    fun hasBlePermissions(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED
    }

    fun start(context: Context) {
        appContext = context.applicationContext
        val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        adapter = btManager.adapter
        if (adapter == null) {
            LogBuffer.add("BLE", "Bluetooth no soportado en este dispositivo")
            return
        }
        if (!adapter!!.isEnabled) {
            LogBuffer.add("BLE", "Bluetooth desactivado - no se puede iniciar escaneo")
            return
        }
        scanner = adapter!!.bluetoothLeScanner
        advertiser = adapter!!.bluetoothLeAdvertiser
        LogBuffer.add("BLE", "BleManager inicializado")
    }





    fun startAdvertisingWithPayload(payload: String) {
        if (adapter == null || !adapter!!.isEnabled) {
            LogBuffer.add("BLE", "No se puede iniciar advertising: Bluetooth no disponible")
            return
        }
        if (advertiser == null) advertiser = adapter!!.bluetoothLeAdvertiser
        if (isProximityAdvertising) {
            LogBuffer.add("BLE", "Advertising de proximidad ya activo")
            return
        }
        val context = appContext
        if (context != null && !hasBlePermissions(context)) {
            LogBuffer.add("BLE", "Permiso BLUETOOTH_ADVERTISE denegado")
            DiagnosticsLogger.log("BLE", "Permiso BLUETOOTH_ADVERTISE denegado")
            return
        }
        try {
            val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(false)
                .build()
            val data = AdvertiseData.Builder()
                .addServiceData(ParcelUuid(serviceUuid), payload.toByteArray(Charsets.UTF_8))
                .build()
            advertiser?.startAdvertising(settings, data, proximityAdvertiseCallback)
            isProximityAdvertising = true
            DiagnosticsLogger.log("BLE", "Advertising con payload iniciado")
        } catch (e: SecurityException) {
            LogBuffer.add("BLE", "Error de seguridad al iniciar advertising")
        } catch (e: Exception) {
            LogBuffer.add("BLE", "Error al iniciar advertising con payload: ${e.message}")
        }
    }

    fun startAdvertisingWithUserData(userId: String, token: String, displayName: String) {
        if (adapter == null || !adapter!!.isEnabled) {
            LogBuffer.add("BLE", "No se puede iniciar advertising: Bluetooth no disponible")
            return
        }
        if (advertiser == null) advertiser = adapter!!.bluetoothLeAdvertiser
        if (isProximityAdvertising) {
            LogBuffer.add("BLE", "Advertising de proximidad ya activo")
            return
        }
        val context = appContext
        if (context != null && !hasBlePermissions(context)) {
            LogBuffer.add("BLE", "Permiso BLUETOOTH_ADVERTISE denegado")
            DiagnosticsLogger.log("BLE", "Permiso BLUETOOTH_ADVERTISE denegado")
            return
        }
        try {
            val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(true)
                .build()
            // Payload: userId(16) + "|"(1) + nombre(6) = 23 bytes.
            // Con 6 chars cabemos siempre en el advertising de 31 bytes, incluso con
            // AdvertiseData flags y el header de manufacturerData. Con 10 chars fallaba
            // en Xiaomi cuando el nombre era el default "Usuario Malla".
            val shortUserId = userId.take(16)
            val shortName = displayName.split(" ").firstOrNull()?.take(6).orEmpty()
            val payload = if (shortName.isBlank()) shortUserId else "$shortUserId|$shortName"
            val data = AdvertiseData.Builder()
                .addManufacturerData(0xABCD, payload.toByteArray(Charsets.UTF_8))
                .build()
            advertiser?.startAdvertising(settings, data, proximityAdvertiseCallback)
            isProximityAdvertising = true
            LogBuffer.add("BLE", "Advertising con userId iniciado: $userId")
            DiagnosticsLogger.log("BLE", "Advertising con userId iniciado: $userId")
        } catch (e: SecurityException) {
            LogBuffer.add("BLE", "Error de seguridad al iniciar advertising")
        } catch (e: Exception) {
            LogBuffer.add("BLE", "Error al iniciar advertising con userId: ${e.message}")
        }
    }

    fun startAdvertisingWithData(token: String, displayName: String, avatarSeed: Int) {
        if (adapter == null || !adapter!!.isEnabled) {
            LogBuffer.add("BLE", "No se puede iniciar advertising: Bluetooth no disponible")
            return
        }
        if (advertiser == null) {
            advertiser = adapter!!.bluetoothLeAdvertiser
        }
        if (isProximityAdvertising) {
            LogBuffer.add("BLE", "Advertising de proximidad ya activo")
            return
        }

        val context = appContext
        if (context != null && !hasBlePermissions(context)) {
            LogBuffer.add("BLE", "Permiso BLUETOOTH_ADVERTISE denegado")
            DiagnosticsLogger.log("BLE", "Permiso BLUETOOTH_ADVERTISE denegado")
            return
        }

        try {
            val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(true)
                .build()

            // Payload compacto: token(8)|nombre(10) = 19 bytes
            val shortToken = token.take(8)
            val shortName = displayName.take(10)
            val data = AdvertiseData.Builder()
                .addServiceData(ParcelUuid(serviceUuid), "$shortToken|$shortName".toByteArray(Charsets.UTF_8))
                .build()

            advertiser?.startAdvertising(settings, data, proximityAdvertiseCallback)
            isProximityAdvertising = true
            LogBuffer.add("BLE", "Advertising de proximidad iniciado con token $token")
        } catch (e: SecurityException) {
            LogBuffer.add("BLE", "Error de seguridad al iniciar advertising de proximidad")
        } catch (e: Exception) {
            LogBuffer.add("BLE", "Error al iniciar advertising de proximidad: ${e.message}")
        }
    }

    fun stopProximityAdvertising() {
        if (!isProximityAdvertising) return
        try {
            advertiser?.stopAdvertising(proximityAdvertiseCallback)
            isProximityAdvertising = false
        } catch (e: Exception) {}
    }



    fun stop() {
        scanner?.stopScan(scanCallback)
        isScanningActive = false
        stopProximityAdvertising()
        stopProximityScanning()
        gattCache.values.forEach { try { it.disconnect() } catch (_: Exception) {} }
        gattCache.values.forEach { try { it.close() } catch (_: Exception) {} }
        gattCache.clear()
        mtuCache.clear()
    }

    // ---------- Nuevo: escaneo con callback ----------
    fun startScanningWithCallback(callback: (token: String, userId: String?, name: String, seed: Int, strength: Int, device: BluetoothDevice) -> Unit) {
        if (adapter == null || !adapter!!.isEnabled) return
        val ctx = appContext ?: return
        if (!hasBlePermissions(ctx)) {
            LogBuffer.add("BLE", "Sin permisos BLUETOOTH_SCAN para iniciar escaneo")
            DiagnosticsLogger.log("BLE", "Sin permisos BLUETOOTH_SCAN para iniciar escaneo")
            return
        }
        if (scanner == null) {
            scanner = adapter?.bluetoothLeScanner
        }
        if (isProximityScanning) return
        proximityScanCallback = callback
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        try {
            scanner?.startScan(null, scanSettings, proximityScanCallbackWrapper)
            isProximityScanning = true
            LogBuffer.add("BLE", "Escaneo de proximidad iniciado")
            DiagnosticsLogger.log("BLE", "Escaneo de proximidad BLE iniciado sin filtro estricto")
        } catch (e: SecurityException) {
            LogBuffer.add("BLE", "Permiso BLUETOOTH_SCAN denegado")
        }
    }

    fun stopProximityScanning() {
        if (!isProximityScanning) return
        scanner?.stopScan(proximityScanCallbackWrapper)
        isProximityScanning = false
        proximityScanCallback = null
    }

    /**
     * Re-registra el escaneo de proximidad con el callback almacenado.
     * Android apaga el scan en background/doze sin avisar (a veces sin onScanFailed).
     * Llamar periodicamente desde un watchdog garantiza continuidad del descubrimiento.
     */
    fun restartProximityScanning() {
        val cb = proximityScanCallback ?: return
        try { scanner?.stopScan(proximityScanCallbackWrapper) } catch (_: Exception) {}
        isProximityScanning = false
        startScanningWithCallback(cb)
    }

    fun startScanning(context: Context, callback: (token: String, userId: String?, name: String, seed: Int, strength: Int, device: BluetoothDevice) -> Unit) {
        appContext = context.applicationContext
        val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        adapter = btManager.adapter
        startScanningWithCallback(callback)
    }

    // ---------- Callbacks internos ----------
    private val proximityScanCallbackWrapper = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val record = result.scanRecord ?: return
            val manufacturerData = record.getManufacturerSpecificData(0xABCD) ?: return
            DiagnosticsLogger.logThrottled("adv_${result.device.address}", "BLE", "Anuncio MALLA detectado: ${result.device.address} RSSI=${result.rssi}")
            if (!_foundBluetoothDevices.value.contains(result.device)) {
                _foundBluetoothDevices.value = _foundBluetoothDevices.value + result.device
                DiagnosticsLogger.log("BleManager", "Dispositivo BLE añadido: ${result.device.address}, total=${_foundBluetoothDevices.value.size}")
            }
            val payload = String(manufacturerData, Charsets.UTF_8)
            val parts = payload.split("|")
            val userIdFromAd = parts.getOrNull(0)?.takeIf { it.isNotBlank() }
            val deviceName = parts.getOrNull(1)?.takeIf { it.isNotBlank() } ?: (result.device.name ?: "MALLA_${userIdFromAd ?: "unknown"}")
            val token = userIdFromAd?.hashCode()?.toUInt()?.toString(16)?.take(8) ?: "00000000"
            val seed = 0
            Log.i(TAG, "Datos BLE parseados: token=$token, name=$deviceName")
            val strength = result.rssi?.let { rssi ->
                when {
                    rssi > -50 -> 3
                    rssi > -70 -> 2
                    rssi > -90 -> 1
                    else -> 0
                }
            } ?: 0
            proximityScanCallback?.invoke(token, userIdFromAd, deviceName, seed, strength, result.device)
            // Comprobar si es un anuncio de aceptación
            if (parts.size >= 4 && parts[0] == "ACCEPT") {
                val acceptorName = parts[2]
                val acceptorAvatarSeed = parts[3].toIntOrNull() ?: 0
                acceptanceCallback?.invoke(acceptorName, acceptorAvatarSeed)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            val readable = when (errorCode) {
                ScanCallback.SCAN_FAILED_ALREADY_STARTED -> "ALREADY_STARTED"
                ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> "APP_REGISTRATION_FAILED"
                ScanCallback.SCAN_FAILED_INTERNAL_ERROR -> "INTERNAL_ERROR"
                ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED -> "FEATURE_UNSUPPORTED"
                ScanCallback.SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES -> "OUT_OF_HARDWARE_RESOURCES"
                else -> "UNKNOWN($errorCode)"
            }
            LogBuffer.add("BLE", "Escaneo de proximidad fallido: $readable")
            DiagnosticsLogger.log("BLE", "onScanFailed: $readable")
        }
    }

    private val proximityAdvertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            LogBuffer.add("BLE", "Advertising de proximidad iniciado correctamente")
            DiagnosticsLogger.log("BLE", "Advertising de proximidad BLE activo")
        }

        override fun onStartFailure(errorCode: Int) {
            isProximityAdvertising = false
            val errorMsg = when (errorCode) {
                AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE -> "Datos demasiado grandes"
                AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> "Demasiados advertisers"
                AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED -> "Ya estaba iniciado"
                AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR -> "Error interno"
                AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED -> "No soportado"
                else -> "Error $errorCode"
            }
            LogBuffer.add("BLE", "Fallo advertising de proximidad: $errorMsg")
            DiagnosticsLogger.log("BLE", "Fallo advertising de proximidad: $errorMsg")
        }
    }

    // ---------- Métodos existentes sin cambios ----------
    suspend fun connectAndReadIp(device: BluetoothDevice): String? =
        suspendCancellableCoroutine { continuation ->
            val context = appContext ?: run {
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }

            val wasScanning = isScanningActive
            if (wasScanning) {
                scanner?.stopScan(scanCallback)
                isScanningActive = false
            }

            var gatt: BluetoothGatt? = null
            var ipResult: String? = null

            val callback = object : BluetoothGattCallback() {
                override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        gatt?.discoverServices()
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        if (ipResult == null && !continuation.isCompleted) {
                            LogBuffer.add("BLE", "Desconectado sin IP: ${device.address}")
                            continuation.resume(null)
                        }
                        gatt?.close()
                        if (wasScanning && appContext != null) {
                            start(appContext!!)
                        }
                    }
                }

                override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        val service: BluetoothGattService? = gatt?.getService(serviceUuid)
                        val characteristic: BluetoothGattCharacteristic? = service?.getCharacteristic(ipCharacteristicUuid)
                        if (characteristic != null) {
                            gatt?.readCharacteristic(characteristic)
                        } else {
                            LogBuffer.add("BLE", "Característica IP no encontrada")
                            gatt?.disconnect()
                        }
                    } else {
                        gatt?.disconnect()
                    }
                }

                override fun onCharacteristicRead(
                    gatt: BluetoothGatt?,
                    characteristic: BluetoothGattCharacteristic?,
                    status: Int
                ) {
                    if (status == BluetoothGatt.GATT_SUCCESS && characteristic?.uuid == ipCharacteristicUuid) {
                        val bytes = characteristic?.value
                        ipResult = bytes?.toString(Charsets.UTF_8)
                        LogBuffer.add("BLE", "IP leída: $ipResult")
                    }
                    gatt?.disconnect()
                    continuation.resume(ipResult)
                }
            }

            try {
                gatt = device.connectGatt(context, false, callback)
            } catch (e: SecurityException) {
                LogBuffer.add("BLE", "Permiso BLUETOOTH_CONNECT denegado")
                continuation.resume(null)
            }
        }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val name = device.name ?: device.address
            if (!_foundDevices.value.contains(name)) {
                _foundDevices.value = _foundDevices.value + name
                LogBuffer.add("BLE", "Nodo MALLA: $name")
            }
            if (!_foundBluetoothDevices.value.contains(device)) {
                _foundBluetoothDevices.value = _foundBluetoothDevices.value + device
            }
        }

        override fun onScanFailed(errorCode: Int) {
            LogBuffer.add("BLE", "Escaneo fallido: error $errorCode")
        }
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            LogBuffer.add("BLE", "Advertising iniciado correctamente")
        }

        override fun onStartFailure(errorCode: Int) {
            isAdvertising = false
            val errorMsg = when (errorCode) {
                AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE -> "Datos demasiado grandes"
                AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> "Demasiados advertisers"
                AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED -> "Ya estaba iniciado"
                AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR -> "Error interno"
                AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED -> "No soportado"
                else -> "Error $errorCode"
            }
            LogBuffer.add("BLE", "Fallo advertising: $errorMsg")
        }
    }
    fun setAcceptanceCallback(callback: ((acceptorName: String, acceptorAvatarSeed: Int) -> Unit)?) {
        acceptanceCallback = callback
    }



    suspend fun connectAndWriteData(device: BluetoothDevice, characteristicUuid: UUID, data: ByteArray): Boolean {
        val mutex = writeMutexes.getOrPut(device.address) { Mutex() }
        return mutex.withLock {
            DiagnosticsLogger.log("BleManager", "connectAndWriteData a ${device.address} (${data.size} bytes)")
            val context = appContext ?: return@withLock false
            if (!hasBlePermissions(context)) return@withLock false
            try {
                val gatt = gattCache[device.address] ?: establishGatt(device) ?: return@withLock false
                val mtu = mtuCache[device.address] ?: 23
                // Límite duro de ATT en Android: 512 bytes por operación writeCharacteristic
                // (BluetoothGatt.GATT_MAX_ATTRIBUTE_LEN = 512). Con MTU=517, mtu-3=514 excede
                // el límite y writeCharacteristic falla silenciosamente en el fragmento 1
                // (observado en Iter 13 con imagen de 29 KB).
                val maxChunk = (mtu - 3).coerceIn(20, 512)
                // Formato fragmentado: [idxHi:1][idxLo:1][totalHi:1][totalLo:1][payload...]
                // Header de 4 bytes permite hasta 65535 en el wire, pero el cap operativo
                // es MAX_TOTAL_FRAGS=4095 (~2 MB con MTU=517, ~64 KB con MTU=23).
                val payloadPerFrag = maxChunk - 4  // 4 bytes de header
                val service = gatt.getService(serviceUuid) ?: return@withLock false
                val characteristic = service.getCharacteristic(characteristicUuid) ?: return@withLock false

                if (data.size <= payloadPerFrag) {
                    val framed = ByteArray(data.size + 4)
                    framed[0] = 0
                    framed[1] = 0
                    framed[2] = 0
                    framed[3] = 1
                    System.arraycopy(data, 0, framed, 4, data.size)
                    var ok = false
                    var attempt = 0
                    while (attempt < 3 && !ok) {
                        ok = writeFramedWithAck(gatt, characteristic, framed)
                        if (!ok) {
                            attempt++
                            if (attempt < 3) delay(50L * attempt)
                        }
                    }
                    DiagnosticsLogger.log("BleManager", "writeFramed single success=$ok (size=${framed.size}, mtu=$mtu, intentos=$attempt)")
                    return@withLock ok
                }

                val totalFrags = (data.size + payloadPerFrag - 1) / payloadPerFrag
                if (totalFrags > MAX_TOTAL_FRAGS) {
                    DiagnosticsLogger.log("BleManager", "Payload ${data.size}B requiere $totalFrags fragmentos (>$MAX_TOTAL_FRAGS); se rechaza")
                    return@withLock false
                }
                DiagnosticsLogger.log("BleManager", "Fragmentando ${data.size}B en $totalFrags trozos de $payloadPerFrag (mtu=$mtu)")
                for (i in 0 until totalFrags) {
                    val start = i * payloadPerFrag
                    val end = minOf(start + payloadPerFrag, data.size)
                    val chunkSize = end - start
                    val framed = ByteArray(chunkSize + 4)
                    framed[0] = ((i ushr 8) and 0xFF).toByte()
                    framed[1] = (i and 0xFF).toByte()
                    framed[2] = ((totalFrags ushr 8) and 0xFF).toByte()
                    framed[3] = (totalFrags and 0xFF).toByte()
                    System.arraycopy(data, start, framed, 4, chunkSize)
                    var ok = false
                    var attempt = 0
                    while (attempt < 3 && !ok) {
                        ok = writeFramedWithAck(gatt, characteristic, framed)
                        if (!ok) {
                            attempt++
                            if (attempt < 3) {
                                DiagnosticsLogger.log("BleManager", "Retry fragmento ${i + 1}/$totalFrags (intento $attempt)")
                                delay(50L * attempt)
                            }
                        }
                    }
                    if (!ok) {
                        DiagnosticsLogger.log("BleManager", "Fallo definitivo fragmento ${i + 1}/$totalFrags")
                        return@withLock false
                    }
                }
                DiagnosticsLogger.log("BleManager", "Enviados $totalFrags fragmentos OK")
                true
            } catch (e: kotlinx.coroutines.CancellationException) {
                // La coroutine fue cancelada (scope cerrado o caller canceló).
                // No confundir con contención de Mutex: withLock suspende, no cancela.
                DiagnosticsLogger.log("BleManager", "connectAndWriteData cancelado para ${device.address}")
                false
            } catch (e: Exception) {
                DiagnosticsLogger.log("BleManager", "Error connectAndWriteData: ${e.message}")
                try { gattCache.remove(device.address)?.close() } catch (_: Exception) {}
                false
            }
        }
    }

    private suspend fun writeFramedWithAck(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        framed: ByteArray,
        timeoutMs: Long = 1500L
    ): Boolean {
        if (framed.size > 512) {
            DiagnosticsLogger.log("BleManager", "writeFramed rechaza ${framed.size}B (>512 límite ATT)")
            return false
        }
        val address = gatt.device.address
        val deferred = kotlinx.coroutines.CompletableDeferred<Boolean>()
        writeAckDeferreds[address] = deferred
        return try {
            characteristic.value = framed
            characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            val queued = gatt.writeCharacteristic(characteristic)
            if (!queued) {
                DiagnosticsLogger.log("BleManager", "writeCharacteristic devolvió false (queued, framed=${framed.size}B)")
                false
            } else {
                withTimeoutOrNull(timeoutMs) { deferred.await() } ?: run {
                    DiagnosticsLogger.log("BleManager", "writeCharacteristic timeout ${timeoutMs}ms (framed=${framed.size}B)")
                    false
                }
            }
        } catch (e: Exception) {
            DiagnosticsLogger.log("BleManager", "writeFramed excepción: ${e.javaClass.simpleName}: ${e.message}")
            false
        } finally {
            writeAckDeferreds.remove(address)
        }
    }

    private suspend fun establishGatt(device: BluetoothDevice): BluetoothGatt? {
        val context = appContext ?: return null
        val mtuDeferred = CompletableDeferred<Int>()
        val callback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    gatt?.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    gattCache.remove(device.address)
                    mtuCache.remove(device.address)
                    if (!mtuDeferred.isCompleted) mtuDeferred.complete(-1)
                    try { gatt?.close() } catch (_: Exception) {}
                }
            }
            private var mtuAttempt: Int = 0  // 0 = intentó 517, 1 = intentó 247
            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS && gatt != null) {
                    val ok = gatt.requestMtu(517)
                    if (!ok) {
                        if (!gatt.requestMtu(247)) {
                            if (!mtuDeferred.isCompleted) mtuDeferred.complete(23)
                        } else {
                            mtuAttempt = 1
                        }
                    }
                } else {
                    if (!mtuDeferred.isCompleted) mtuDeferred.complete(-1)
                    try { gatt?.disconnect() } catch (_: Exception) {}
                }
            }
            override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
                // Si el peer responde con MTU muy bajo (<100) en el primer intento,
                // reintentar con 247 SIN completar el deferred — el segundo callback
                // traerá el MTU real. Antes se completaba con 23 y el retry quedaba huérfano.
                if (status == BluetoothGatt.GATT_SUCCESS && mtu < 100 && mtuAttempt == 0 && gatt != null) {
                    DiagnosticsLogger.log("BleManager", "MTU inicial $mtu < 100; reintentando con 247")
                    mtuAttempt = 1
                    if (gatt.requestMtu(247)) return
                }
                val effective = if (status == BluetoothGatt.GATT_SUCCESS && mtu >= 23) mtu else 23
                if (!mtuDeferred.isCompleted) mtuDeferred.complete(effective)
            }
            override fun onCharacteristicWrite(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?,
                status: Int
            ) {
                val address = gatt?.device?.address ?: return
                val deferred = writeAckDeferreds.remove(address) ?: return
                if (!deferred.isCompleted) {
                    deferred.complete(status == BluetoothGatt.GATT_SUCCESS)
                }
            }
        }
        val gatt = try {
            device.connectGatt(context, false, callback)
        } catch (e: SecurityException) {
            return null
        } ?: return null

        val mtu = withTimeoutOrNull(3000L) { mtuDeferred.await() } ?: run {
            DiagnosticsLogger.log("BleManager", "MTU timeout 3s para ${device.address}; asumiendo 23")
            23
        }
        if (mtu <= 0) {
            try { gatt.disconnect(); gatt.close() } catch (_: Exception) {}
            return null
        }
        mtuCache[device.address] = mtu
        gattCache[device.address] = gatt
        DiagnosticsLogger.log("BleManager", "MTU con ${device.address}: $mtu bytes")
        return gatt
    }
}
