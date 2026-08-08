package com.malla.mvp.core.model

enum class SignalType { BLE, WIFI_DIRECT, MDNS }

data class NearbyUser(
    val token: String,
    val displayName: String,
    val avatarSeed: Int,
    val signalType: SignalType,
    val signalStrength: Int = 0, // 0–3
    val publicKeyFingerprint: String? = null
)
