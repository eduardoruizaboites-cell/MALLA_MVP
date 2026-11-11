package com.malla.mvp.network

import android.content.Context
import android.provider.Settings
import com.malla.mvp.events.MallaEventBus
import com.malla.mvp.core.model.NearbyUser
import com.malla.mvp.core.model.SignalType
import com.malla.mvp.core.engine.LogBuffer
import com.malla.mvp.core.engine.DiagnosticsLogger
import com.malla.mvp.identity.IdentityManager
import com.malla.mvp.core.wifi.WifiDirectPeer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import com.malla.mvp.core.config.MeshFlags

object ProximityEngine {
    @Volatile private var started = false
    private val _nearbyUsers = MutableStateFlow<List<NearbyUser>>(emptyList())
    val nearbyUsers: StateFlow<List<NearbyUser>> = _nearbyUsers.asStateFlow()

    private var discoveryJob: Job? = null
    private var advertising = false
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var appContext: Context? = null

    /**
     * Iter 67B: cache de contactos registrados (userId -> displayName).
     * Poblada por Injector observando ContactDao.observeAllVisible().
     * Usada por isGhostPeer() para ocultar duplicados huérfanos en "cerca de ti".
     */
    @Volatile private var contactCache: Map<String, String> = emptyMap()

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
            BleManager.startScanningWithCallback { token, userId, name, seed, strength, device ->
                val myId = IdentityManager.getIdentityId()
                DiagnosticsLogger.logThrottled("prox_callback_${device.address}", "PROX", "Callback BLE: token=$token, userId=$userId, name=$name, seed=$seed, device=${device.address}")
                if (token != generateToken(myId)) {
                    addOrUpdate(token, userId, name, seed, SignalType.BLE, strength, device)
                } else {
                    DiagnosticsLogger.log("PROX", "Ignorando anuncio propio: $token")
                }
            }
            // Iniciar advertising MALLA para que otros dispositivos nos detecten
            val myUserId = IdentityManager.getIdentityId()
            val myName = IdentityManager.getUserName(context)
            val myAvatarSeed = myUserId.hashCode()
            val token = generateToken(myUserId)
            DiagnosticsLogger.log("PROX", "Advertising propio: userId=$myUserId, name=$myName, token=$token")
            if (IdentityManager.isVisibleInBle(context)) {
                BleManager.startAdvertisingWithUserData(myUserId, token, myName)
                advertising = true
            } else {
                DiagnosticsLogger.log("PROX", "Advertising omitido (modo invisible)")
            }

            // Wi‑Fi Direct (en modo descubrimiento) solo si es soportado Y habilitado
            if (!MeshFlags.enableWifiDirect) {
                LogBuffer.add("PROX", "Wi-Fi Direct deshabilitado por MeshFlags (modo minimalista)")
            } else if (!WifiDirectManager.wifiDirectUnsupported) {
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
                    val remoteIp = address.substringBefore(":")
                    // Fix iter 59: enumerar TODAS las IPs locales (no solo una).
                    // DhtService.getLocalAddress() puede devolver la IP pública/móvil
                    // y falla al filtrar la IP del hotspot en el Cubot.
                    val myIps: Set<String> = try {
                        java.net.NetworkInterface.getNetworkInterfaces().toList()
                            .flatMap { iface -> iface.inetAddresses.toList().mapNotNull { it.hostAddress } }
                            .filter { it.isNotBlank() && !it.contains(":") }
                            .toSet()
                    } catch (e: Exception) {
                        setOf(DhtService.getLocalAddress() ?: "127.0.0.1")
                    }
                    // Ignorar IPs de rango Wi-Fi Direct para evitar auto-detección del propio grupo
                    if (remoteIp.startsWith("192.168.49.")) {
                        DiagnosticsLogger.log("PROX", "Ignorando IP Wi-Fi Direct: $remoteIp")
                    } else if (remoteIp !in myIps && remoteIp != "127.0.0.1") {
                        // Fix iter 61: NO agregar a _nearbyUsers. El mDNS no trae userId
                        // real ni bluetoothDevice, y crea entradas fantasmas ("Lalo (2)"
                        // sin userId) que nunca se fusionan con la entrada BLE del mismo
                        // peer ("eduard" con userId real). Los peers visibles en la UI
                        // vienen 100% del BLE. El mDNS solo se usa para disparar TCP.
                        MallaEventBus.peerMdnsResolved.tryEmit(remoteIp)
                    }
                }
            DiscoveryService.start(context)

            // Watchdog: Android apaga el escaneo BLE en background/doze sin avisar
            // (a veces sin disparar onScanFailed). Re-registrar cada 2 min garantiza
            // continuidad del descubrimiento y evita que nearbyUsers quede vacio.
            while (isActive) {
                delay(30_000L) // Fix iter 57: bajar de 2min a 30s para Xiaomi
                DiagnosticsLogger.log("PROX", "Watchdog BLE: re-registrando escaneo de proximidad")
                BleManager.restartProximityScanning()
            }
        }

        // Iter 68: TTL de peers — limpiar los que dejaron de anunciar (RC de UI stale)
        scope.launch {
            while (isActive) {
                delay(15_000L)
                val now = System.currentTimeMillis()
                val current = _nearbyUsers.value
                val filtered = current.filter { now - it.lastSeen < 90_000L }
                if (filtered.size != current.size) {
                    _nearbyUsers.value = filtered
                    DiagnosticsLogger.log("PROX", "Peers expirados (TTL 90s): ${current.size - filtered.size}")
                }
            }
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

    /**
     * Reintenta iniciar el escaneo BLE si no está activo y hay permisos.
     * Necesario porque MeshChatService arranca antes de que el usuario conceda
     * permisos: ProximityEngine.start() intenta escanear, falla por permisos, y
     * proximityScanCallback queda null. El watchdog de 2min entra a
     * restartProximityScanning, ve null y sale silencioso. Sin este método,
     * nearbyUsers queda vacío hasta el próximo reinicio de la app.
     */
    fun ensureScanning(context: Context) {
        if (!BleManager.hasBlePermissions(context)) {
            DiagnosticsLogger.log("PROX", "ensureScanning: sin permisos BLE, no se inicia scan")
            return
        }
        // BleManager.startScanningWithCallback es idempotente: si ya está escaneando, no-op.
        BleManager.startScanningWithCallback { token, userId, name, seed, strength, device ->
            val myId = IdentityManager.getIdentityId()
            DiagnosticsLogger.logThrottled("prox_callback_${device.address}", "PROX", "Callback BLE: token=$token, userId=$userId, name=$name, seed=$seed, device=${device.address}")
            if (token != generateToken(myId)) {
                addOrUpdate(token, userId, name, seed, SignalType.BLE, strength, device)
            } else {
                DiagnosticsLogger.log("PROX", "Ignorando anuncio propio: $token")
            }
        }
    }

    fun startAdvertising(displayName: String, avatarSeed: Int) {
        if (advertising) return
        val ctx = appContext ?: return
        if (!IdentityManager.isVisibleInBle(ctx)) {
            DiagnosticsLogger.log("PROX", "startAdvertising: omitido (modo invisible)")
            return
        }
        val userId = IdentityManager.getIdentityId()
        val token = generateToken(userId)
        BleManager.startAdvertisingWithUserData(userId, token, displayName)
        advertising = true
    }

    fun stopAdvertising() {
        BleManager.stopProximityAdvertising()
        advertising = false
    }

    /**
     * Bug H (iter 45): el advertising inicial arranca en MainActivity.onCreate antes de
     * que el usuario complete el registro. Cuando setUserName se llama despues, el advertising
     * sigue anunciando el nombre viejo ("Usuario Malla") porque startAdvertising() retorna
     * temprano si advertising==true.
     *
     * Este helper detiene el advertising actual y lo vuelve a arrancar con el nombre
     * recien persistido. Se debe llamar desde RegistrationScreen (post-registro),
     * PerfilScreen (edicion de nombre) y EditProfileScreen (guardar cambios).
     */
    fun refreshAdvertising(context: android.content.Context) {
        stopAdvertising()
        if (!IdentityManager.isVisibleInBle(context)) {
            DiagnosticsLogger.log("PROX", "refreshAdvertising: modo invisible, advertising detenido")
            return
        }
        val name = IdentityManager.getUserName(context)
        startAdvertising(name, 0)
        // Fix iter 61: usar restart() que espera al callback async del listener.
        // El stop()+start() de iter 59 fallaba con "listener already in use".
        try {
            DiscoveryService.restart(context)
        } catch (e: Exception) {
            DiagnosticsLogger.log("PROX", "refreshAdvertising: error refrescando mDNS: ${e.message}")
        }
        DiagnosticsLogger.log("PROX", "Advertising refrescado con nombre=$name (BLE + mDNS)")
    }

    /**
     * Iter 67: cambia el modo de visibilidad BLE del usuario.
     * Persiste el flag y aplica el cambio inmediatamente.
     */
    fun setVisibility(context: android.content.Context, visible: Boolean) {
        IdentityManager.setVisibleInBle(context, visible)
        if (visible) {
            refreshAdvertising(context)
            DiagnosticsLogger.log("PROX", "Visibilidad activada (visible=true)")
        } else {
            stopAdvertising()
            DiagnosticsLogger.log("PROX", "Visibilidad desactivada (visible=false)")
        }
    }

    /**
     * Iter 67B: actualiza el cache de contactos registrados.
     * Llamado por Injector cada vez que ContactDao.observeAllVisible() emite.
     */
    fun setContactCache(cache: Map<String, String>) {
        contactCache = cache
        DiagnosticsLogger.log("PROX", "contactCache actualizado: ${cache.size} contactos")
    }

    /**
     * Iter 67B: detecta si un peer es "ghost" (duplicado huérfano de un contacto
     * registrado cuyo userId cambió por reinstalación).
     *
     * Condiciones (las 3 deben cumplirse):
     *  1. El peer tiene userId (no es entrada mDNS sin identidad).
     *  2. El userId NO está entre los contactos registrados.
     *  3. Existe un contacto con displayName igual (o prefijo significativo >=4 chars,
     *     para tolerar el truncamiento a 6 chars del advertising BLE).
     *
     * Si las 3 se cumplen, se considera ghost y NO se agrega a nearbyUsers.
     */
    private fun isGhostPeer(userId: String?, name: String): Boolean {
        if (userId == null || contactCache.isEmpty()) return false
        if (contactCache.containsKey(userId)) return false
        return contactCache.values.any { existingName ->
            existingName.equals(name, ignoreCase = true) ||
                (name.length >= 4 && existingName.length >= 4 &&
                    (name.startsWith(existingName, ignoreCase = true) ||
                        existingName.startsWith(name, ignoreCase = true)))
        }
    }

    fun hideUser(token: String) {
        _nearbyUsers.value = _nearbyUsers.value.filter { it.token != token }
    }

    fun blockUser(token: String) {
        _nearbyUsers.value = _nearbyUsers.value.filter { it.token != token }
        // En el futuro se guardará en persistencia
    }

    private fun addOrUpdate(token: String, userId: String? = null, name: String, seed: Int, type: SignalType, strength: Int, device: android.bluetooth.BluetoothDevice? = null) {
        if (isSelfUser(name, token)) return
        if (isGhostPeer(userId, name)) {
            DiagnosticsLogger.log("PROX", "Peer oculto: name=$name userId=$userId (ghost de contacto registrado)")
            return
        }
        val current = _nearbyUsers.value.toMutableList()
        // Buscar por token exacto
        var idx = current.indexOfFirst { it.token == token }
        // Si no, buscar por dirección Bluetooth
        if (idx == -1 && device != null) {
            idx = current.indexOfFirst { it.bluetoothDevice?.address == device.address }
        }
        if (idx != -1) {
            current[idx] = current[idx].copy(
                userId = userId ?: current[idx].userId,
                displayName = name,
                signalStrength = maxOf(strength, current[idx].signalStrength),
                signalType = if (type == SignalType.MDNS) current[idx].signalType else type,
                bluetoothDevice = device ?: current[idx].bluetoothDevice,
                lastSeen = System.currentTimeMillis()
            )
        } else {
            // Fix iter 59: fusionar BLE con mDNS huérfano del mismo peer.
            // El advertising BLE trunca el nombre a 6 chars ("Eduard" vs "Eduardo" del mDNS).
            // Caso 1: llega BLE con userId y hay mDNS sin userId con nombre prefijo.
            if (userId != null) {
                idx = current.indexOfFirst { existing ->
                    existing.userId == null &&
                        existing.signalType == SignalType.MDNS &&
                        (name.startsWith(existing.displayName, ignoreCase = true) ||
                            existing.displayName.startsWith(name, ignoreCase = true))
                }
            }
            // Caso 2: llega mDNS sin userId y hay BLE con nombre prefijo.
            if (idx == -1 && type == SignalType.MDNS && userId == null) {
                idx = current.indexOfFirst { existing ->
                    existing.signalType != SignalType.MDNS &&
                        existing.userId != null &&
                        (name.startsWith(existing.displayName, ignoreCase = true) ||
                            existing.displayName.startsWith(name, ignoreCase = true))
                }
            }
            // Buscar por displayName exacto como fallback
            if (idx == -1) {
                idx = current.indexOfFirst { it.displayName.equals(name, ignoreCase = true) }
            }
            if (idx != -1) {
                val existing = current[idx]
                val preferredType = if (type == SignalType.MDNS) existing.signalType else type
                val preferredToken = if (type == SignalType.MDNS) existing.token else token
                // Preservar userId del BLE y nombre más largo del mDNS
                val mergedUserId = userId ?: existing.userId
                val mergedName = if (name.length > existing.displayName.length) name else existing.displayName
                current[idx] = existing.copy(
                    token = preferredToken,
                    userId = mergedUserId,
                    displayName = mergedName,
                    signalType = preferredType,
                    signalStrength = maxOf(strength, existing.signalStrength),
                    bluetoothDevice = device ?: existing.bluetoothDevice,
                    lastSeen = System.currentTimeMillis()
                )
            } else {
                current.add(NearbyUser(token = token, userId = userId, displayName = name, avatarSeed = seed, signalType = type, signalStrength = strength, bluetoothDevice = device))
            }
        }
        _nearbyUsers.value = current
        DiagnosticsLogger.logThrottled("prox_nodes_updated", "PROX", "Nodos actualizados: ${current.map { it.displayName + ":" + it.token }}")
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
        current.add(NearbyUser(token = token, userId = null, displayName = name, avatarSeed = 0, signalType = SignalType.WIFI_DIRECT, signalStrength = 0))
        _nearbyUsers.value = current
        DiagnosticsLogger.logThrottled("prox_nodes_updated", "PROX", "Nodos actualizados: ${current.map { it.displayName + ":" + it.token }}")
    }


    private fun isSelfUser(displayName: String?, token: String): Boolean {
        val myName = appContext?.let { IdentityManager.getUserName(it) }
        if (displayName != null && myName != null && displayName.equals(myName, ignoreCase = true)) return true
        val myId = IdentityManager.getIdentityId()
        if (token == generateToken(myId)) return true
        return false
    }


    private fun logNearbyUsers() {
        DiagnosticsLogger.log("PROX", "Nodos actuales: ${_nearbyUsers.value.map { "${it.displayName}:${it.token}:${it.signalType}" }}")
    }

    private fun generateToken(userId: String): String {
        val androidId = appContext?.let { Settings.Secure.getString(it.contentResolver, Settings.Secure.ANDROID_ID) } ?: userId
        val day = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
        val input = "$androidId-$day-malla-proximity"
        return input.hashCode().toUInt().toString(16).take(8)
    }
}
