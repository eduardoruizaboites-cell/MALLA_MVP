package com.malla.mvp.network

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.Channel
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener
import android.net.wifi.p2p.WifiP2pManager.PeerListListener
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest
import android.util.Log
import com.malla.mvp.core.engine.DiagnosticsLogger
import com.malla.mvp.core.wifi.IWifiDirectManager
import com.malla.mvp.core.wifi.WifiDirectPeer
import com.malla.mvp.core.wifi.WifiDirectConnectionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap

@SuppressLint("MissingPermission")
object WifiDirectManager : IWifiDirectManager {
    private const val TAG = "WifiDirectManager"
    private const val SERVICE_TYPE = "_malla._tcp"
    private const val SERVICE_NAME = "MALLA_SERVICE"
    private const val PORT = 8889

    private val _peers = MutableStateFlow<List<WifiDirectPeer>>(emptyList())
    override val peers: StateFlow<List<WifiDirectPeer>> = _peers

    private val _connectionState = MutableStateFlow(WifiDirectConnectionState.IDLE)
    override val connectionState: StateFlow<WifiDirectConnectionState> = _connectionState

    private var manager: WifiP2pManager? = null
    private var channel: Channel? = null
    private var receiver: BroadcastReceiver? = null
    private val connectedSockets = ConcurrentHashMap<String, Socket>()
    private var serverSocket: ServerSocket? = null
    private var groupOwnerIp: String? = null
    private var isRunning = false
    private var appContext: Context? = null
    private var isDiscovering = false
    private var isConnecting = false
    private var discoveryRetryCount = 0

    override fun start(context: Context) {
        startWithGroupName(context, null)
    }

    fun startWithGroupName(context: Context, groupName: String?) {
        if (isRunning) return
        appContext = context.applicationContext
        manager = context.getSystemService(Context.WIFI_P2P_SERVICE) as? WifiP2pManager
        if (manager == null) {
            DiagnosticsLogger.log(TAG, "Wi-Fi Direct no soportado")
            return
        }
        channel = manager?.initialize(context, context.mainLooper, null)
        receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                when (intent.action) {
                    WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                        val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                        DiagnosticsLogger.log(TAG, "Estado Wi-Fi Direct: $state")
                    }
                    WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                        manager?.requestPeers(channel) { peerList ->
                            val allPeers = peerList.deviceList?.map { WifiDirectPeer(it.deviceAddress, it.deviceName) } ?: emptyList()
                            _peers.value = allPeers
                            DiagnosticsLogger.log(TAG, "Peers encontrados (${allPeers.size}): ${allPeers.joinToString(", ") { "${it.deviceName}" }}")
                            val mallaPeers = allPeers.filter { it.deviceName?.startsWith("MALLA_") == true }
                            if (mallaPeers.isNotEmpty() && !isConnecting && _connectionState.value != WifiDirectConnectionState.CONNECTED) {
                                val first = mallaPeers.first()
                                DiagnosticsLogger.log(TAG, "Auto-conectando al peer MALLA: ${first.deviceName} (${first.address})")
                                connectToPeer(first.address)
                            } else {
                                DiagnosticsLogger.log(TAG, "No se encontraron peers MALLA para auto-conectar")
                            }
                        }
                    }
                    WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                        manager?.requestConnectionInfo(channel) { info ->
                            handleConnectionInfo(info)
                        }
                    }
                    WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {}
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        }
        appContext?.registerReceiver(receiver, filter)
        discoverPeers()
        if (groupName != null) {
            createGroup(groupName)
        }
        isRunning = true
        DiagnosticsLogger.log(TAG, "Wi-Fi Direct iniciado")
    }

    override fun stop() {
        if (!isRunning) return
        try {
            appContext?.let { ctx -> receiver?.let { r -> ctx.unregisterReceiver(r) } }
        } catch (_: Exception) {}
        serverSocket?.close()
        serverSocket = null
        isRunning = false
        isDiscovering = false
        isConnecting = false
        _connectionState.value = WifiDirectConnectionState.IDLE
        DiagnosticsLogger.log(TAG, "Wi-Fi Direct detenido")
    }


    /**
     * Envía un payload de texto a todos los sockets Wi-Fi Direct conectados.
     * Retorna true si al menos un envío se intentó.
     */
    fun broadcast(payload: String): Boolean {
        var attempted = false
        connectedSockets.values.forEach { socket ->
            try {
                val writer = OutputStreamWriter(socket.getOutputStream())
                writer.write(payload + "\n")
                writer.flush()
                attempted = true
            } catch (e: Exception) {
                DiagnosticsLogger.log(TAG, "Error enviando por Wi-Fi Direct: ${e.message}")
            }
        }
        return attempted
    }

    override fun connectToPeer(address: String) {
        if (manager == null || channel == null || isConnecting) return
        isConnecting = true
        _connectionState.value = WifiDirectConnectionState.CONNECTING
        val config = WifiP2pConfig().apply {
            deviceAddress = address
        }
        manager?.connect(channel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                isConnecting = false
                DiagnosticsLogger.log(TAG, "Conexión iniciada a $address")
            }
            override fun onFailure(reason: Int) {
                isConnecting = false
                _connectionState.value = WifiDirectConnectionState.ERROR
                DiagnosticsLogger.log(TAG, "Fallo al conectar a $address: razón $reason")
            }
        })
    }

    private fun createGroup(groupName: String) {
        if (manager == null || channel == null) return
        manager?.requestGroupInfo(channel) { group ->
            if (group != null) {
                DiagnosticsLogger.log(TAG, "Ya existe un grupo, no se crea uno nuevo")
                return@requestGroupInfo
            }
            manager?.createGroup(channel, object : WifiP2pManager.ActionListener {
                override fun onSuccess() {
                    DiagnosticsLogger.log(TAG, "Grupo Wi-Fi Direct creado con nombre $groupName")
                }
                override fun onFailure(reason: Int) {
                    DiagnosticsLogger.log(TAG, "Fallo al crear grupo: razón $reason")
                }
            })
        }
    }

    private fun discoverPeers() {
        if (isDiscovering) return
        isDiscovering = true
        _connectionState.value = WifiDirectConnectionState.DISCOVERING
        manager?.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                isDiscovering = false
                discoveryRetryCount = 0
                DiagnosticsLogger.log(TAG, "Descubrimiento de peers iniciado")
            }
            override fun onFailure(reason: Int) {
                isDiscovering = false
                discoveryRetryCount++
                DiagnosticsLogger.log(TAG, "Fallo al descubrir peers: razón $reason")
                if (isRunning) {
                    val delay = minOf(5_000L * discoveryRetryCount, 30_000L)
                    kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                        kotlinx.coroutines.delay(delay)
                        discoverPeers()
                    }
                }
            }
        })
    }

    private fun handleConnectionInfo(info: WifiP2pInfo) {
        groupOwnerIp = info.groupOwnerAddress?.hostAddress
        _connectionState.value = WifiDirectConnectionState.CONNECTED
        DiagnosticsLogger.log(TAG, "Información de conexión: groupOwnerIp=$groupOwnerIp, isGroupOwner=${info.isGroupOwner}")
        if (info.isGroupOwner) {
            startServerSocket()
        } else {
            groupOwnerIp?.let { ip ->
                GlobalScope.launch(Dispatchers.IO) {
                    connectToSocket(ip)
                }
            }
        }
    }

    private fun startServerSocket() {
        if (serverSocket != null) return
        GlobalScope.launch(Dispatchers.IO) {
            try {
                serverSocket = ServerSocket(PORT)
                DiagnosticsLogger.log(TAG, "Servidor Wi-Fi Direct escuchando en $PORT")
                while (isRunning) {
                    val client = serverSocket?.accept()
                    client?.let { handleSocket(it) }
                }
            } catch (e: Exception) {
                DiagnosticsLogger.log(TAG, "Error en servidor Wi-Fi Direct: ${e.message}")
            }
        }
    }

    private fun connectToSocket(ip: String) {
        try {
            val socket = Socket()
            socket.connect(InetSocketAddress(ip, PORT), 5000)
            handleSocket(socket)
        } catch (e: Exception) {
            DiagnosticsLogger.log(TAG, "Error conectando socket Wi-Fi Direct: ${e.message}")
        }
    }

    private fun handleSocket(socket: Socket) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val address = socket.inetAddress?.hostAddress ?: "unknown"
                connectedSockets[address] = socket
                DiagnosticsLogger.log(TAG, "Socket Wi-Fi Direct activo: $address")
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                val writer = OutputStreamWriter(socket.getOutputStream())
                while (true) {
                    val line = reader.readLine() ?: break
                    DiagnosticsLogger.log(TAG, "Mensaje Wi-Fi Direct recibido: $line")
                    // Aquí se integrará con el bus de mensajes
                }
                reader.close()
                writer.close()
                socket.close()
                connectedSockets.remove(address)
            } catch (e: Exception) {
                DiagnosticsLogger.log(TAG, "Cierre de socket Wi-Fi Direct: ${e.message}")
            }
        }
    }
}
