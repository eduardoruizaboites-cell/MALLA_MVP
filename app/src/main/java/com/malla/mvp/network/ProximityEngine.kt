package com.malla.mvp.network

import android.content.Context
import android.bluetooth.BluetoothDevice
import com.malla.mvp.core.model.NearbyUser
import com.malla.mvp.core.model.SignalType
import com.malla.mvp.core.engine.LogBuffer
import com.malla.mvp.identity.IdentityManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

object ProximityEngine {
    private val _nearbyUsers = MutableStateFlow<List<NearbyUser>>(emptyList())
    val nearbyUsers: StateFlow<List<NearbyUser>> = _nearbyUsers.asStateFlow()

    private var discoveryJob: Job? = null
    private var advertising = false
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var appContext: Context? = null

    fun start(context: Context) {
        if (discoveryJob?.isActive == true) return
        appContext = context.applicationContext
        discoveryJob = scope.launch {
            LogBuffer.add("PROX", "ProximityEngine iniciado")
            // BLE scanning
            BleManager.startScanningWithCallback { token, name, seed, strength, device ->
                addOrUpdate(token, name, seed, SignalType.BLE, strength, device)
                // Si el dispositivo BLE es accesible, intentar obtener IP y conectar
                scope.launch { tryConnectFromBle(device) }
            }
            // Wi-Fi Direct (en modo descubrimiento)
            WifiDirectManager.start(context)
            // mDNS
            DiscoveryService.onPeerResolved = { address ->
                val localIp = DhtService.getLocalAddress() ?: "127.0.0.1"
                val remoteIp = address.substringBefore(":")
                if (remoteIp != localIp && remoteIp != "127.0.0.1") {
                    addOrUpdate("mdns_$remoteIp", remoteIp, 0, SignalType.MDNS, 3)
                    // Conectar automáticamente
                    NetworkService.connectToPeer(remoteIp)
                }
            }
            DiscoveryService.start(context)
        }
    }

    private suspend fun tryConnectFromBle(device: BluetoothDevice) {
        try {
            val ip = BleManager.connectAndReadIp(device)
            if (ip != null) {
                LogBuffer.add("PROX", "Conectando a IP obtenida por BLE: $ip")
                NetworkService.connectToPeer(ip)
            }
        } catch (e: Exception) {
            LogBuffer.add("PROX", "No se pudo obtener IP por BLE: ${e.message}")
        }
    }

    fun stop() {
        discoveryJob?.cancel()
        BleManager.stopProximityScanning()
        WifiDirectManager.stop()
        DiscoveryService.stop()
        _nearbyUsers.value = emptyList()
    }

    fun startAdvertising(displayName: String, avatarSeed: Int) {
        if (advertising) return
        val userId = IdentityManager.getIdentityId()
        val token = generateToken(userId)
        BleManager.startAdvertisingWithData(token, displayName, avatarSeed)
        advertising = true
    }

    fun stopAdvertising() {
        BleManager.stopProximityAdvertising()
        advertising = false
    }

    fun hideUser(token: String) {
        _nearbyUsers.value = _nearbyUsers.value.filter { it.token != token }
    }

    fun blockUser(token: String) {
        _nearbyUsers.value = _nearbyUsers.value.filter { it.token != token }
    }

    private fun addOrUpdate(token: String, name: String, seed: Int, type: SignalType, strength: Int, device: BluetoothDevice? = null) {
        val current = _nearbyUsers.value.toMutableList()
        val idx = current.indexOfFirst { it.token == token }
        if (idx != -1) {
            current[idx] = current[idx].copy(displayName = name, signalStrength = strength)
        } else {
            current.add(NearbyUser(token, name, seed, type, strength, bluetoothDevice = device))
        }
        _nearbyUsers.value = current
    }

    private fun generateToken(userId: String): String {
        val day = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
        val input = "$userId-$day-malla-proximity"
        return input.hashCode().toUInt().toString(16).take(12)
    }
}
