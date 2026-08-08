package com.malla.mvp.dht

import com.malla.mvp.network.DhtService

object DhtHelper {
    suspend fun resolveIp(userId: String): String? {
        return DhtService.lookup(userId)
    }
}
