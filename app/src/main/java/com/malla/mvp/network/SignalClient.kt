package com.malla.mvp.network

import android.util.Log
import com.malla.mvp.App
import com.malla.mvp.BuildConfig
import com.malla.mvp.identity.IdentityManager
import okhttp3.*
import okio.ByteString
import org.json.JSONObject

/**
 * Cliente de señalización WebSocket.
 * Se conecta al servidor bootstrap para intercambiar SDP/ICE de WebRTC.
 * No almacena mensajes ni metadatos persistentes.
 */
object SignalClient {
    private const val TAG = "SignalClient"

    private var webSocket: WebSocket? = null
    private var listener: ((from: String, payload: String) -> Unit)? = null

    fun connect() {
        val userId = IdentityManager.getIdentityId()
        val serverUrl = "${BuildConfig.SIGNAL_SERVER_URL}?userId=$userId"
        val request = Request.Builder().url(serverUrl).build()
        val client = OkHttpClient.Builder()
            .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "Conectado al servidor de señalización")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    val from = json.optString("from")
                    val payload = json.optString("payload")
                    listener?.invoke(from, payload)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parseando señal", e)
                }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {}

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "Error en señalización: ${t.message}")
            }
        })
    }

    fun sendSignal(to: String, payload: String) {
        try {
            val json = JSONObject()
                .put("to", to)
                .put("from", IdentityManager.getIdentityId())
                .put("payload", payload)
            webSocket?.send(json.toString())
        } catch (e: Exception) {
            Log.e(TAG, "Error enviando señal", e)
        }
    }

    fun setListener(l: (from: String, payload: String) -> Unit) {
        listener = l
    }

    fun disconnect() {
        webSocket?.close(1000, "bye")
        webSocket = null
    }
}
