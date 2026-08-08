package com.malla.mvp.network

import android.content.Context
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
            }
            // Wi‑Fi Direct (en modo descubrimiento)
            WifiDirectManager.start(context)
            // mDNS
            DiscoveryService.onPeerResolved = { address ->
                val parts = address.split(":")
                val token = "mdns_${parts.getOrNull(0) ?: ""}"
                addOrUpdate(token, parts.getOrNull(0) ?: "Desconocido", 0, SignalType.MDNS, 3)
            }
            DiscoveryService.start(context)
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
        // En el futuro se guardará en persistencia
    }

    private fun addOrUpdate(token: String, name: String, seed: Int, type: SignalType, strength: Int, device: android.bluetooth.BluetoothDevice? = null) {
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
