package com.malla.mvp.network
import com.malla.mvp.core.engine.LogBuffer

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.ParcelUuid
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.UUID
import kotlin.coroutines.resume

object BleManager {
    private const val TAG = "BleManager"
    private val serviceUuid = UUID.fromString("0000abcd-0000-1000-8000-00805f9b34fb")
    private val ipCharacteristicUuid = UUID.fromString("0000abcd-0001-1000-8000-00805f9b34fb")
    private var adapter: BluetoothAdapter? = null
    private var scanner: BluetoothLeScanner? = null
    private var advertiser: BluetoothLeAdvertiser? = null
    fun getAdapter(): BluetoothAdapter? = adapter
    private var isAdvertising = false
    private var isScanningActive = false
    private var appContext: Context? = null

    private val _foundDevices = MutableStateFlow<List<String>>(emptyList())
    val foundDevices: StateFlow<List<String>> = _foundDevices
    private val _foundBluetoothDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val foundBluetoothDevices: StateFlow<List<BluetoothDevice>> = _foundBluetoothDevices

    // ---------- Nuevo: callbacks para ProximityEngine ----------
    private var proximityScanCallback: ((token: String, name: String, seed: Int, strength: Int) -> Unit)? = null
    private var isProximityScanning = false
    private var isProximityAdvertising = false

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
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        val scanFilter = ScanFilter.Builder().setServiceUuid(ParcelUuid(serviceUuid)).build()
        try {
            scanner?.startScan(listOf(scanFilter), scanSettings, scanCallback)
            isScanningActive = true
            LogBuffer.add("BLE", "Escaneo BLE iniciado con filtro MALLA")
        } catch (e: SecurityException) {
            LogBuffer.add("BLE", "Permiso BLUETOOTH_SCAN denegado")
        }
    }

    fun startAdvertising() {
        if (adapter == null || !adapter!!.isEnabled) {
            LogBuffer.add("BLE", "No se puede iniciar advertising: Bluetooth no disponible")
            return
        }
        if (advertiser == null) {
            advertiser = adapter!!.bluetoothLeAdvertiser
        }
        if (isAdvertising) return

        val context = appContext
        if (context != null && ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_ADVERTISE)
            != PackageManager.PERMISSION_GRANTED) {
            LogBuffer.add("BLE", "Permiso BLUETOOTH_ADVERTISE denegado")
            return
        }

        try {
            val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(true)
                .build()

            val data = AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .addServiceUuid(ParcelUuid(serviceUuid))
                .build()

            advertiser?.startAdvertising(settings, data, advertiseCallback)
            isAdvertising = true
            LogBuffer.add("BLE", "Advertising BLE iniciado")
        } catch (e: SecurityException) {
            LogBuffer.add("BLE", "Error de seguridad al iniciar advertising")
        } catch (e: Exception) {
            LogBuffer.add("BLE", "Error al iniciar advertising: ${e.message}")
        }
    }

    // ---------- Nuevo: advertising con datos personalizados ----------
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
        if (context != null && ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_ADVERTISE)
            != PackageManager.PERMISSION_GRANTED) {
            LogBuffer.add("BLE", "Permiso BLUETOOTH_ADVERTISE denegado")
            return
        }

        try {
            val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(false)
                .build()

            // Empaquetar datos en el campo de manufacturer specific data o service data
            val payload = "$token|$displayName|$avatarSeed".toByteArray(Charsets.UTF_8)
            val data = AdvertiseData.Builder()
                .addServiceData(ParcelUuid(serviceUuid), payload)
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

    fun stopAdvertising() {
        if (!isAdvertising) return
        try {
            advertiser?.stopAdvertising(advertiseCallback)
            isAdvertising = false
        } catch (e: Exception) {}
    }

    fun stop() {
        scanner?.stopScan(scanCallback)
        isScanningActive = false
        stopAdvertising()
        stopProximityAdvertising()
        stopProximityScanning()
    }

    // ---------- Nuevo: escaneo con callback ----------
    fun startScanningWithCallback(callback: (token: String, name: String, seed: Int, strength: Int) -> Unit) {
        if (adapter == null || !adapter!!.isEnabled) return
        if (scanner == null) {
            scanner = adapter?.bluetoothLeScanner
        }
        if (isProximityScanning) return
        proximityScanCallback = callback
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        val scanFilter = ScanFilter.Builder().setServiceUuid(ParcelUuid(serviceUuid)).build()
        try {
            scanner?.startScan(listOf(scanFilter), scanSettings, proximityScanCallbackWrapper)
            isProximityScanning = true
            LogBuffer.add("BLE", "Escaneo de proximidad iniciado")
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

    fun startScanning(context: Context, callback: (token: String, name: String, seed: Int, strength: Int) -> Unit) {
        appContext = context.applicationContext
        val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        adapter = btManager.adapter
        startScanningWithCallback(callback)
    }

    // ---------- Callbacks internos ----------
    private val proximityScanCallbackWrapper = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val record = result.scanRecord ?: return
            val serviceData = record.serviceData?.get(ParcelUuid(serviceUuid)) ?: return
            val payload = String(serviceData, Charsets.UTF_8)
            val parts = payload.split("|")
            if (parts.size >= 3) {
                val token = parts[0]
                val name = parts[1]
                val seed = parts[2].toIntOrNull() ?: 0
                val strength = result.rssi?.let { rssi ->
                    when {
                        rssi > -50 -> 3
                        rssi > -70 -> 2
                        rssi > -90 -> 1
                        else -> 0
                    }
                } ?: 0
                proximityScanCallback?.invoke(token, name, seed, strength)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            LogBuffer.add("BLE", "Escaneo de proximidad fallido: error $errorCode")
        }
    }

    private val proximityAdvertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            LogBuffer.add("BLE", "Advertising de proximidad iniciado correctamente")
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
}
