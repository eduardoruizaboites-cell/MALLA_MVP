package com.malla.mvp.network

import android.util.Log
import kotlinx.coroutines.*

object ReconnectManager {
    private const val TAG = "ReconnectManager"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val pendingReconnects = mutableMapOf<String, Job>()

    fun scheduleReconnect(address: String, expectedPublicKey: String? = null, maxRetries: Int = 5) {
        if (pendingReconnects.containsKey(address)) return
        val job = scope.launch {
            var delayMs = 1000L
            for (i in 1..maxRetries) {
                delay(delayMs)
                Log.d(TAG, "Intentando reconectar a $address (intento $i/$maxRetries)")
                try {
                    NetworkService.connectToPeer(address, expectedPublicKey)
                    Log.d(TAG, "Reconexión exitosa a $address")
                    pendingReconnects.remove(address)
                    return@launch
                } catch (e: Exception) {
                    Log.e(TAG, "Error en reconexión a $address: ${e.message}")
                }
                delayMs *= 2
            }
            Log.w(TAG, "Reconexión fallida a $address después de $maxRetries intentos")
            pendingReconnects.remove(address)
        }
        pendingReconnects[address] = job
    }

    fun cancelReconnect(address: String) {
        pendingReconnects[address]?.cancel()
        pendingReconnects.remove(address)
    }
}
