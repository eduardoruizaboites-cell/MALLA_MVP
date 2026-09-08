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
    @Volatile private var started = false
    private val _nearbyUsers = MutableStateFlow<List<NearbyUser>>(emptyList())
    val nearbyUsers: StateFlow<List<NearbyUser>> = _nearbyUsers.asStateFlow()

    private var discoveryJob: Job? = null
    private var advertising = false
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var appContext: Context? = null

    fun start(context: Context) {
        synchronized(this) {
            if (started) return
            started = true
        }
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
                if (token != generateToken(myId)) {
                    addOrUpdate(token, name, seed, SignalType.BLE, strength, device)
                }
            }
            // Iniciar advertising MALLA para que otros dispositivos nos detecten
            val myUserId = IdentityManager.getIdentityId()
            val myName = IdentityManager.getUserName(context)
            val myAvatarSeed = myUserId.hashCode()
            val token = generateToken(myUserId)
            BleManager.startAdvertisingWithData(token, myName, myAvatarSeed)

            // Wi‑Fi Direct (en modo descubrimiento) solo si es soportado
            if (!WifiDirectManager.wifiDirectUnsupported) {
                val groupName = "MALLA_$myName"
                WifiDirectManager.startWithGroupName(context, groupName)
            } else {
                LogBuffer.add("PROX", "Wi-Fi Direct omitido (no soportado)")
            }
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
        synchronized(this) { started = false }
        discoveryJob?.cancel()
        BleManager.stopProximityScanning()
        WifiDirectManager.stop()
        DiscoveryService.stop()
        _nearbyUsers.value = emptyList()
    }

    fun ensureAdvertising(context: Context) {
        val myName = IdentityManager.getUserName(context)
        val myUserId = IdentityManager.getIdentityId()
        val myAvatarSeed = myUserId.hashCode()
        if (!advertising) {
            startAdvertising(myName, myAvatarSeed)
        }
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
        if (isSelfUser(name, token)) return
        val current = _nearbyUsers.value.toMutableList()
        // Buscar por token exacto
        var idx = current.indexOfFirst { it.token == token }
        // Si no, buscar por dirección Bluetooth
        if (idx == -1 && device != null) {
            idx = current.indexOfFirst { it.bluetoothDevice?.address == device.address }
        }
        if (idx != -1) {
            current[idx] = current[idx].copy(
                displayName = name,
                signalStrength = maxOf(strength, current[idx].signalStrength),
                signalType = if (type == SignalType.MDNS) current[idx].signalType else type,
                bluetoothDevice = device ?: current[idx].bluetoothDevice
            )
        } else {
            // Buscar por displayName para fusionar transportes distintos
            idx = current.indexOfFirst { it.displayName.equals(name, ignoreCase = true) }
            if (idx != -1) {
                val existing = current[idx]
                val preferredType = if (type == SignalType.MDNS) existing.signalType else type
                val preferredToken = if (type == SignalType.MDNS) existing.token else token
                current[idx] = existing.copy(
                    token = preferredToken,
                    signalType = preferredType,
                    signalStrength = maxOf(strength, existing.signalStrength),
                    bluetoothDevice = device ?: existing.bluetoothDevice
                )
            } else {
                current.add(NearbyUser(token, name, seed, type, strength, bluetoothDevice = device))
            }
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
        if (isSelfUser(name, token)) return
        val current = _nearbyUsers.value.toMutableList()
        var idx = current.indexOfFirst { it.token == token }
        if (idx != -1) return
        idx = current.indexOfFirst { it.displayName.equals(name, ignoreCase = true) }
        if (idx != -1) return // ya existe por otro transporte, no duplicar
        current.add(NearbyUser(token, name, 0, SignalType.WIFI_DIRECT, 0))
        _nearbyUsers.value = current
    }


    private fun isSelfUser(displayName: String?, token: String): Boolean {
        val myName = appContext?.let { IdentityManager.getUserName(it) }
        if (displayName != null && myName != null && displayName.equals(myName, ignoreCase = true)) return true
        val myId = IdentityManager.getIdentityId()
        if (token == generateToken(myId)) return true
        return false
    }

    private fun generateToken(userId: String): String {
        val day = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
        val input = "$userId-$day-malla-proximity"
        return input.hashCode().toUInt().toString(16).take(8)
    }
}
