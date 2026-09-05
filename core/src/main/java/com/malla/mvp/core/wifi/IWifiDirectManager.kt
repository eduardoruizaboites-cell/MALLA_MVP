package com.malla.mvp.core.wifi

import android.content.Context
import kotlinx.coroutines.flow.StateFlow

data class WifiDirectPeer(
    val address: String,
    val deviceName: String? = null
)

interface IWifiDirectManager {
    val peers: StateFlow<List<WifiDirectPeer>>
    val connectionState: StateFlow<WifiDirectConnectionState>
    fun start(context: Context)
    fun stop()
    fun connectToPeer(address: String)
}

enum class WifiDirectConnectionState {
    IDLE,
    DISCOVERING,
    CONNECTING,
    CONNECTED,
    ERROR
}
