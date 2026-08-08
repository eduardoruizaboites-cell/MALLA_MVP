package com.malla.mvp.network

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.malla.mvp.identity.IdentityManager
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

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

    /**
     * Deriva una clave AES de 256 bits a partir del userId.
     */
    private fun deriveKey(userId: String): SecretKeySpec {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val keyBytes = digest.digest(userId.toByteArray(Charsets.UTF_8))
        return SecretKeySpec(keyBytes, "AES")
    }

    /**
     * Encripta una IP usando AES/CBC/PKCS5Padding con un IV fijo (solo para ofuscar, no para alta seguridad).
     * Retorna el resultado en Base64.
     */
    fun encryptIp(ip: String, userId: String): String {
        val key = deriveKey(userId)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val iv = IvParameterSpec(ByteArray(16)) // IV cero (simplificado)
        cipher.init(Cipher.ENCRYPT_MODE, key, iv)
        val encrypted = cipher.doFinal(ip.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }

    /**
     * Desencripta una IP previamente encriptada con encryptIp.
     */
    fun decryptIp(encryptedIp: String, userId: String): String {
        val key = deriveKey(userId)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val iv = IvParameterSpec(ByteArray(16))
        cipher.init(Cipher.DECRYPT_MODE, key, iv)
        val decoded = Base64.decode(encryptedIp, Base64.NO_WRAP)
        return String(cipher.doFinal(decoded), Charsets.UTF_8)
    }
}
