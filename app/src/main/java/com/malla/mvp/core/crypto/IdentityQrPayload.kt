package com.malla.mvp.core.crypto

import android.util.Base64
import com.malla.mvp.identity.IdentityManager

object IdentityQrPayload {

    private const val PREFIX = "MALLA:"
    private const val VALIDITY_SECONDS = 60L

    data class ParsedPayload(
        val pubKeyBase64: String,
        val timestamp: Long,
        val signatureBase64: String,
        val displayName: String? = null,
        val localIp: String? = null
    )

    // Genera el contenido del QR: "MALLA:<pubKey>:<timestamp>:<firma>" (opcionalmente con nombre e IP)
    suspend fun generate(
        keystoreManager: KeystoreManager,
        identityManager: IdentityManager,
        displayName: String? = null,
        localIp: String? = null
    ): String {
        val pubKeyBase64 = identityManager.getPublicKeyBase64()
            ?: throw IllegalStateException("No hay identidad creada")
        val timestamp = System.currentTimeMillis()
        val metaParts = listOfNotNull(
            displayName?.let { "name=${android.net.Uri.encode(it)}" },
            localIp?.let { "ip=${android.net.Uri.encode(it)}" }
        ).joinToString("&")
        val dataToSign = "$pubKeyBase64:$timestamp${if (metaParts.isNotBlank()) ":$metaParts" else ""}".toByteArray(Charsets.UTF_8)
        val signature = keystoreManager.signData(dataToSign)
        val signatureBase64 = Base64.encodeToString(signature, Base64.NO_WRAP)
        return "$PREFIX$pubKeyBase64:$timestamp${if (metaParts.isNotBlank()) ":$metaParts" else ""}:$signatureBase64"
    }

    // Parsea y verifica el contenido del QR
    fun parseAndVerify(qrContent: String): ParsedPayload? {
        if (!qrContent.startsWith(PREFIX)) return null
        val parts = qrContent.removePrefix(PREFIX).split(":")
        if (parts.size < 3 || parts.size > 5) return null
        val pubKeyBase64 = parts[0]
        val timestamp = parts[1].toLongOrNull() ?: return null
        val metaPart = if (parts.size >= 4) parts[2] else ""
        val signatureBase64 = parts.last()

        // Verificar vigencia (60 segundos)
        val now = System.currentTimeMillis()
        if (now - timestamp > VALIDITY_SECONDS * 1000) return null

        // Verificar firma con los datos originales
        val dataToVerify = "$pubKeyBase64:$timestamp${if (metaPart.isNotBlank()) ":$metaPart" else ""}".toByteArray(Charsets.UTF_8)
        val signature = Base64.decode(signatureBase64, Base64.NO_WRAP)
        if (!verifySignature(pubKeyBase64, dataToVerify, signature)) return null

        var displayName: String? = null
        var localIp: String? = null
        if (metaPart.isNotBlank()) {
            metaPart.split("&").forEach { kv ->
                val key = kv.substringBefore("=")
                val value = android.net.Uri.decode(kv.substringAfter("=", ""))
                when (key) {
                    "name" -> displayName = value
                    "ip" -> localIp = value
                }
            }
        }
        return ParsedPayload(pubKeyBase64, timestamp, signatureBase64, displayName, localIp)
    }

    private fun verifySignature(pubKeyBase64: String, data: ByteArray, signature: ByteArray): Boolean {
        return try {
            val publicKey = com.malla.mvp.crypto.CryptoEngine.base64ToPublicKey(pubKeyBase64)
            val sig = java.security.Signature.getInstance("SHA256withECDSA")
            sig.initVerify(publicKey)
            sig.update(data)
            sig.verify(signature)
        } catch (e: Exception) {
            false
        }
    }
}
