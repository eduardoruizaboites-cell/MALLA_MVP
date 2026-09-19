package com.malla.mvp.core.engine

import android.content.Context
import android.os.Build
import android.os.Environment
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.IntentFilter
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

object DiagnosticsLogger {
    private const val FILENAME = "malla_diagnostics.txt"
    private var logFile: File? = null
    private val throttleMap = ConcurrentHashMap<String, Long>()

    fun init(context: Context) {
        if (logFile == null) {
            val candidates = mutableListOf<File>()
            try {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (downloadsDir.exists() || downloadsDir.mkdirs()) candidates.add(downloadsDir)
            } catch (_: Exception) {}
            context.getExternalFilesDir(null)?.let { candidates.add(it) }
            candidates.add(context.filesDir)

            for (dir in candidates) {
                try {
                    val file = File(dir, FILENAME)
                    if (!file.exists()) file.createNewFile()
                    if (file.canWrite()) {
                        logFile = file
                        Log.i("MallaDiagnostics", "Archivo de diagnóstico: ${file.absolutePath}")
                        break
                    }
                } catch (_: Exception) {}
            }
            if (logFile == null) {
                val fallback = File(context.cacheDir, FILENAME)
                try { fallback.createNewFile() } catch (_: Exception) {}
                logFile = fallback
                Log.i("MallaDiagnostics", "Fallback de diagnóstico: ${fallback.absolutePath}")
            }
        }
    }

    fun log(tag: String, message: String) {
        val file = logFile ?: return
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
        val line = "[$timestamp] [$tag] $message\n"
        try {
            file.appendText(line)
        } catch (e: Exception) {
            Log.e("MallaDiagnostics", "No se pudo escribir log: ${e.message}")
        }
        Log.i("Malla", "[$tag] $message")
    }

    /**
     * Igual que [log], pero silencia llamadas repetidas con la misma [key]
     * dentro de [intervalMs]. Para eventos de alta frecuencia (advertising BLE,
     * callbacks de proximidad) que de otro modo saturan el archivo.
     */
    fun logThrottled(key: String, tag: String, message: String, intervalMs: Long = 5000L) {
        val now = System.currentTimeMillis()
        val last = throttleMap[key]
        if (last == null || now - last >= intervalMs) {
            throttleMap[key] = now
            log(tag, message)
        }
    }

    fun getLogFilePath(): String? = logFile?.absolutePath

    fun logDeviceInfo(context: Context) {
        try {
            val appContext = context.applicationContext
            log("DIAG", "=== INFORMACIÓN DEL DISPOSITIVO ===")
            log("DIAG", "Modelo: ${Build.MODEL} (${Build.MANUFACTURER} ${Build.BRAND})")
            log("DIAG", "Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            log("DIAG", "Batería: ${getBatteryLevel(appContext)}%")
            log("DIAG", "Wi-Fi Direct soportado: ${appContext.packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT)}")
            log("DIAG", "Bluetooth LE soportado: ${appContext.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)}")
            log("DIAG", "Bluetooth clásico soportado: ${appContext.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)}")
            log("DIAG", "Cámara frontal: ${appContext.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)}")
            log("DIAG", "Micrófono: ${appContext.packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)}")
            log("DIAG", "Ubicación GPS: ${appContext.packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)}")
            logWifiInfo(appContext)
            logBluetoothInfo(appContext)
            log("DIAG", "=== FIN INFORMACIÓN DEL DISPOSITIVO ===")
        } catch (e: Exception) {
            log("DIAG", "Error obteniendo información del dispositivo: ${e.message}")
        }
    }

    private fun getBatteryLevel(context: Context): Int {
        return try {
            val batteryStatus = context.registerReceiver(null, IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))
            val level = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1) ?: -1
            if (level >= 0 && scale > 0) (level * 100 / scale) else -1
        } catch (_: Exception) { -1 }
    }

    private fun logWifiInfo(context: Context) {
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            val ssid = wifiInfo?.ssid ?: "Desconocida"
            val ip = wifiInfo?.ipAddress?.let { android.text.format.Formatter.formatIpAddress(it) } ?: "No disponible"
            log("DIAG", "Red Wi-Fi conectada: SSID=$ssid, IP=$ip")
            log("DIAG", "Wi-Fi habilitado: ${wifiManager.isWifiEnabled}")
        } catch (_: Exception) {}
    }

    private fun logBluetoothInfo(context: Context) {
        try {
            val btManager = context.applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val adapter = btManager.adapter
            if (adapter != null) {
                log("DIAG", "Bluetooth habilitado: ${adapter.isEnabled}")
                log("DIAG", "Dirección Bluetooth: ${adapter.address}")
                log("DIAG", "Bluetooth LE advertiser soportado: ${adapter.bluetoothLeAdvertiser != null}")
            } else {
                log("DIAG", "Bluetooth no soportado")
            }
        } catch (_: Exception) {}
    }
}
