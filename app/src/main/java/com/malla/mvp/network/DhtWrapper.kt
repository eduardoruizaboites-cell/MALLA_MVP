package com.malla.mvp.network

import android.content.Context
import android.content.SharedPreferences
import com.malla.mvp.identity.IdentityManager

object DhtWrapper {
    private const val PREFS_NAME = "dht_presence"
    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun publish(userId: String, ip: String, port: Int) {
        prefs?.edit()?.putString(userId, "$ip:$port")?.apply()
    }

    fun getLocalAddress(): String? {
        // Obtener IP local (sin depender de :network)
        try {
            val interfaces = java.net.NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address.hostAddress?.contains(":") == false) {
                        return address.hostAddress
                    }
                }
            }
        } catch (_: Exception) {}
        return null
    }
}
