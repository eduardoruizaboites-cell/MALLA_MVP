package com.malla.mvp.crypto

import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.SecretKey

object SessionCipher {
    fun deriveSessionKey(privateKey: PrivateKey, contactPublicKey: PublicKey): SecretKey {
        return CryptoEngine.deriveSharedSecret(privateKey, contactPublicKey)
    }

    fun encrypt(plaintext: String, secretKey: SecretKey): String {
        val encryptedBytes = CryptoEngine.encrypt(plaintext, secretKey)
        return android.util.Base64.encodeToString(encryptedBytes, android.util.Base64.NO_WRAP)
    }

    fun decrypt(ciphertext: String, secretKey: SecretKey): String {
        val encryptedBytes = android.util.Base64.decode(ciphertext, android.util.Base64.NO_WRAP)
        return CryptoEngine.decrypt(encryptedBytes, secretKey)
    }
}
