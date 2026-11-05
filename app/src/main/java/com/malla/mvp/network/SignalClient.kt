package com.malla.mvp.network

import android.util.Log
import com.malla.mvp.BuildConfig
import com.malla.mvp.identity.IdentityManager
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import okhttp3.*
import okio.ByteString
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Cliente de señalización WebSocket.
 * Se conecta al servidor bootstrap para intercambiar SDP/ICE de WebRTC.
 * No almacena mensajes ni metadatos persistentes (stateless relay).
 *
 * Características (iter 64):
 * - Reconexión con backoff exponencial (1s → 30s máx)
 * - Cola de outbound con buffer 100 si el socket no está abierto
 * - Múltiples listeners por key (no se sobrescriben)
 * - Ping automático vía OkHttp (detecta socket muerto)
 * - TLS configurado por build type
 */
object SignalClient {
    private const val TAG = "SignalClient"
    private const val MAX_BACKOFF_MS = 30_000L
    private const val OUTBOUND_BUFFER = 100
    private const val PING_INTERVAL_MS = 20_000L

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var webSocket: WebSocket? = null
    private var reconnectJob: Job? = null
    private val outboundChannel: Channel<String> = Channel(OUTBOUND_BUFFER)
    private val listeners = ConcurrentHashMap<String, (from: String, payload: String) -> Unit>()
    private val isConnecting = AtomicBoolean(false)
    private var currentBackoffMs = 1000L
    private var intentionalDisconnect = false

    fun connect() {
        if (webSocket != null || isConnecting.get()) return
        intentionalDisconnect = false
        isConnecting.set(true)

        val userId = IdentityManager.getIdentityId()
        val serverUrl = "${BuildConfig.SIGNAL_SERVER_URL}?userId=$userId"
        Log.d(TAG, "Conectando a $serverUrl (build=${BuildConfig.BUILD_TYPE})")

        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .pingInterval(PING_INTERVAL_MS, TimeUnit.MILLISECONDS)
            .build()

        val request = Request.Builder().url(serverUrl).build()
        webSocket = client.newWebSocket(request, socketListener)
    }

    private val socketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d(TAG, "Conectado al servidor de señalizacion")
            isConnecting.set(false)
            currentBackoffMs = 1000L
            flushOutbound()
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            try {
                val json = JSONObject(text)
                if (json.optString("type") == "ping") return
                val from = json.optString("from")
                val payload = json.optString("payload")
                listeners.values.forEach { it(from, payload) }
            } catch (e: Exception) {
                Log.e(TAG, "Error parseando senal", e)
            }
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {}

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e(TAG, "Error en senalizacion: ${t.message}")
            isConnecting.set(false)
            this@SignalClient.webSocket = null
            if (!intentionalDisconnect) scheduleReconnect()
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "Cerrado: $code $reason")
            isConnecting.set(false)
            this@SignalClient.webSocket = null
            if (!intentionalDisconnect) scheduleReconnect()
        }
    }

    fun sendSignal(to: String, payload: String) {
        val json = JSONObject()
            .put("to", to)
            .put("from", IdentityManager.getIdentityId())
            .put("payload", payload)
            .toString()

        val socket = webSocket
        if (socket != null && socket.send(json)) return

        val result = outboundChannel.trySend(json)
        if (result.isFailure) {
            Log.w(TAG, "Cola outbound llena, descartando senal")
        }
    }

    private fun flushOutbound() {
        scope.launch {
            while (true) {
                val json = outboundChannel.tryReceive().getOrNull() ?: break
                if (webSocket?.send(json) != true) {
                    outboundChannel.trySend(json)
                    break
                }
            }
        }
    }

    fun addListener(key: String, l: (from: String, payload: String) -> Unit) {
        listeners[key] = l
    }

    fun removeListener(key: String) {
        listeners.remove(key)
    }

    @Deprecated("Usar addListener(key, l) para permitir multiples consumidores")
    fun setListener(l: (from: String, payload: String) -> Unit) {
        listeners["default"] = l
    }

    private fun scheduleReconnect() {
        reconnectJob?.cancel()
        val backoff = currentBackoffMs
        Log.d(TAG, "Reconectando en ${backoff}ms")
        reconnectJob = scope.launch {
            delay(backoff)
            currentBackoffMs = (currentBackoffMs * 2).coerceAtMost(MAX_BACKOFF_MS)
            if (!intentionalDisconnect) connect()
        }
    }

    fun disconnect() {
        intentionalDisconnect = true
        reconnectJob?.cancel()
        webSocket?.close(1000, "bye")
        webSocket = null
    }
}
