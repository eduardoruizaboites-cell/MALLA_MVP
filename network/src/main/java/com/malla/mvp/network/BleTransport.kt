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
import com.malla.mvp.network.BleManager
import android.content.pm.PackageManager
import android.os.ParcelUuid
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import com.malla.mvp.core.engine.LogBuffer
import com.malla.mvp.core.engine.DiagnosticsLogger
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object BleTransport {
    private const val TAG = "BleTransport"
    val SERVICE_UUID = UUID.fromString("0000abcd-0000-1000-8000-00805f9b34fb")
    val MESSAGE_CHAR_UUID = UUID.fromString("0000abcd-0003-1000-8000-00805f9b34fb")
    private var appContext: Context? = null
    private var gattServer: BluetoothGattServer? = null
    private val connectedGatts = ConcurrentHashMap<String, BluetoothGatt>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val incomingMessages = MutableSharedFlow<ByteArray>(extraBufferCapacity = 64)
    val messages: SharedFlow<ByteArray> = incomingMessages.asSharedFlow()
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
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT)
            != PackageManager.PERMISSION_GRANTED) return
        try {
            val gatt = device.connectGatt(context, true, object : BluetoothGattCallback() {
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
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT)
            != PackageManager.PERMISSION_GRANTED) return
        val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = btManager.adapter ?: return
        gattServer = btManager.openGattServer(context, gattServerCallback).apply {
            val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
            val char = BluetoothGattCharacteristic(
                MESSAGE_CHAR_UUID,
                BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE
            )
            service.addCharacteristic(char)
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

    fun connectAndSend(device: BluetoothDevice, data: ByteArray) {
        val context = appContext ?: return
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT)
            != PackageManager.PERMISSION_GRANTED) return
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
            if (characteristic.uuid == MESSAGE_CHAR_UUID) {
                incomingMessages.tryEmit(value)
                if (responseNeeded) {
                    gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null)
                }
            }
        }
    }
}
