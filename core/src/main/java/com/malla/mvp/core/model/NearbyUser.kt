package com.malla.mvp.core.model

import android.bluetooth.BluetoothDevice

enum class SignalType { BLE, WIFI_DIRECT, MDNS }

data class NearbyUser(
    val token: String,
    val displayName: String,
    val avatarSeed: Int,
    val signalType: SignalType,
    val signalStrength: Int = 0,
    val publicKeyFingerprint: String? = null,
    val bluetoothDevice: BluetoothDevice? = null   // solo para BLE
)
