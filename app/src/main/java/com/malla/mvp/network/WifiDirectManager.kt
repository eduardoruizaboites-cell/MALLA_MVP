package com.malla.mvp.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object WifiDirectManager {
    private val _peers = MutableStateFlow<List<String>>(emptyList())
    val peers: StateFlow<List<String>> = _peers

    fun start(context: android.content.Context) {
        // Inicializar Wi-Fi Direct
    }

    fun stop() {
        // Detener Wi-Fi Direct
    }

    fun connectToPeer(address: String) {
        // Conectar a un peer
    }
}
