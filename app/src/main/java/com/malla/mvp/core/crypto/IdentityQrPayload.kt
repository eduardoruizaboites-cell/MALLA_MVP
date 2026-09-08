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

    // MVP: QR simple sin firma, formato "MALLA:<userId>|<displayName>|<ip>"
    suspend fun generate(
        keystoreManager: KeystoreManager,
        identityManager: IdentityManager,
        displayName: String? = null,
        localIp: String? = null
    ): String {
        val userId = identityManager.getIdentityId()
        val name = displayName ?: "Usuario Malla"
        val ip = localIp ?: ""
        return "$PREFIX$userId|$name|$ip"
    }

    // Parsea el contenido del QR simple (sin verificación de firma)
    fun parseAndVerify(qrContent: String): ParsedPayload? {
        if (!qrContent.startsWith(PREFIX)) return null
        val body = qrContent.removePrefix(PREFIX)
        val parts = body.split("|")
        if (parts.size < 2) return null
        val userId = parts[0]
        val displayName = parts.getOrElse(1) { "Usuario Malla" }
        val localIp = parts.getOrElse(2) { null }
        return ParsedPayload(
            pubKeyBase64 = userId,  // reusamos el campo para userId
            timestamp = System.currentTimeMillis(),
            signatureBase64 = "",
            displayName = displayName,
            localIp = localIp
        )
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
