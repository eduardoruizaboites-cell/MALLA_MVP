package com.malla.mvp.network

import android.content.Context
import com.malla.mvp.core.model.NearbyUser
import com.malla.mvp.core.model.SignalType
import com.malla.mvp.core.engine.LogBuffer
import com.malla.mvp.core.engine.DiagnosticsLogger
import com.malla.mvp.identity.IdentityManager
import com.malla.mvp.core.wifi.WifiDirectPeer
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
            DiagnosticsLogger.log("PROX", "ProximityEngine iniciado")
            // Inicializar BleManager (adapter, scanner, advertiser)
            BleManager.start(context)
            BleTransport.start(context)
            // BLE scanning
            BleManager.startScanningWithCallback { token, name, seed, strength, device ->
                val myId = IdentityManager.getIdentityId()
                if (token != myId && token != generateToken(myId)) {
                    addOrUpdate(token, name, seed, SignalType.BLE, strength, device)
                }
            }
            // Iniciar advertising MALLA para que otros dispositivos nos detecten
            val myName = IdentityManager.getUserName(context)
            val myUserId = IdentityManager.getIdentityId()
            val myAvatarSeed = myUserId.hashCode()
            BleManager.startAdvertisingWithData(myUserId, myName, myAvatarSeed)

            // Wi‑Fi Direct (en modo descubrimiento)
            WifiDirectManager.start(context)
            // Observar peers Wi-Fi Direct
            scope.launch {
                WifiDirectManager.peers.collect { peers ->
                    peers.forEach { peer ->
                        addWifiDirectPeer(peer)
                    }
                }
            }
            // mDNS
            DiscoveryService.onPeerResolved = { addressWithName ->
                    val parts = addressWithName.split("|")
                    val address = parts.getOrElse(0) { addressWithName }
                    val serviceName = parts.getOrElse(1) { "MALLA_${address.substringBefore(":")}" }
                    val localIp = DhtService.getLocalAddress() ?: "127.0.0.1"
                    val remoteIp = address.substringBefore(":")
                    if (remoteIp != localIp && remoteIp != "127.0.0.1") {
                        val token = "mdns_$remoteIp"
                        val displayName = serviceName.removePrefix("MALLA_")
                        addOrUpdate(token, displayName, 0, SignalType.MDNS, 3)
                    }
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

    private fun addWifiDirectPeer(peer: WifiDirectPeer) {
        val deviceName = peer.deviceName ?: ""
        // Solo agregar peers MALLA, ignorar dispositivos genéricos
        if (!deviceName.startsWith("MALLA_") && !deviceName.contains("MALLA")) {
            return
        }
        val token = "wifi_${peer.address}"
        val name = if (deviceName.startsWith("MALLA_")) deviceName.removePrefix("MALLA_") else deviceName
        val current = _nearbyUsers.value.toMutableList()
        val idx = current.indexOfFirst { it.token == token }
        if (idx == -1) {
            current.add(NearbyUser(token, name, 0, SignalType.WIFI_DIRECT, 0))
            _nearbyUsers.value = current
        }
    }

    private fun generateToken(userId: String): String {
        val day = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
        val input = "$userId-$day-malla-proximity"
        return input.hashCode().toUInt().toString(16).take(12)
    }
}
