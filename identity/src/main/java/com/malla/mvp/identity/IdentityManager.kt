package com.malla.mvp.identity

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.ECGenParameterSpec
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import android.content.SharedPreferences

object IdentityManager {
    private const val TAG = "IdentityManager"
    private const val KEY_ALIAS = "malla_identity"
    private const val USER_NAME_KEY = "user_name"
    private const val USER_STATUS_KEY = "user_status"
    private const val AVATAR_FILE = "avatar.jpg"
    private const val BANNER_FILE = "banner.jpg"
    private const val ID_PREFS = "identity_id_prefs"
    private const val PERSISTENT_ID_KEY = "persistent_id"

    val deviceId: String = java.util.UUID.randomUUID().toString().take(8)

    @Volatile private var cachedPublicKeyBase64: String? = null
    @Volatile private var cachedIdentityId: String? = null
    private var appContext: Context? = null

    // Avatar reactivo
    private val _avatarBitmap = MutableStateFlow<Bitmap?>(null)
    val avatarBitmap: StateFlow<Bitmap?> = _avatarBitmap

    fun init(context: Context) {
        appContext = context.applicationContext
        try {
            val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            if (!ks.containsAlias(KEY_ALIAS)) {
                generateKeyPair()
                Log.d(TAG, "[IDENTITY] Nuevo keypair ECDH generado en Keystore")
            } else {
                Log.d(TAG, "[IDENTITY] Keypair existente cargado del Keystore")
            }
            getPublicKeyBase64()
            // Cargar avatar guardado al inicio
            val bitmap = loadAvatar(context)
            if (bitmap != null) _avatarBitmap.value = bitmap
        } catch (e: Exception) {
            Log.e(TAG, "[IDENTITY:ERR] Error inicializando identidad: ${e.message}", e)
        }
    }

    private fun generateKeyPair() {
        val kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore")
        kpg.initialize(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            )
                .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
                .setDigests(KeyProperties.DIGEST_SHA256)
                .setUserAuthenticationRequired(false)
                .build()
        )
        kpg.generateKeyPair()
    }

    fun getPublicKeyBase64(): String? {
        cachedPublicKeyBase64?.let { return it }
        return try {
            val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            val entry = ks.getEntry(KEY_ALIAS, null) as? KeyStore.PrivateKeyEntry
                ?: throw IllegalStateException("Keypair no encontrado")
            val base64 = Base64.encodeToString(entry.certificate.publicKey.encoded, Base64.NO_WRAP)
            cachedPublicKeyBase64 = base64
            base64
        } catch (e: Exception) {
            Log.e(TAG, "[IDENTITY:ERR] No se pudo obtener clave pública: ${e.message}", e)
            null
        }
    }

    fun getIdentityId(): String {
        cachedIdentityId?.let { return it }
        val pubKey = getPublicKeyBase64()
        val id = if (pubKey != null) {
            deriveUserIdFromPubKey(pubKey)
        } else {
            getOrCreatePersistentId()
        }
        cachedIdentityId = id
        return id
    }

    /**
     * Deriva un userId único de 16 caracteres hex a partir de SHA-256(pubKeyBase64).
     * A diferencia del esquema anterior (primeros 12 chars del base64 DER, idénticos
     * en todos los dispositivos por el header del DER), este hash incluye la parte
     * única de la clave y por tanto es único por dispositivo.
     */
    private fun deriveUserIdFromPubKey(pubKeyBase64: String): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
            .digest(pubKeyBase64.toByteArray(Charsets.UTF_8))
        return digest.take(8).joinToString("") { "%02x".format(it) }
    }

    private fun getOrCreatePersistentId(): String {
        val context = appContext
            ?: return java.util.UUID.randomUUID().toString().replace("-", "").take(16)
        val prefs = context.getSharedPreferences(ID_PREFS, Context.MODE_PRIVATE)
        var id = prefs.getString(PERSISTENT_ID_KEY, null)
        // Regenerar si no existe o si tiene el formato viejo (8 chars con guiones)
        if (id == null || id.length != 16 || id.contains("-")) {
            id = java.util.UUID.randomUUID().toString().replace("-", "").take(16)
            prefs.edit().putString(PERSISTENT_ID_KEY, id).apply()
        }
        return id
    }

    fun getPrivateKey(): PrivateKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val entry = ks.getEntry(KEY_ALIAS, null) as? KeyStore.PrivateKeyEntry
            ?: throw IllegalStateException("Keypair no encontrado en Keystore")
        return entry.privateKey
    }

    fun getUserName(context: Context): String {
        val prefs = context.getSharedPreferences("identity", Context.MODE_PRIVATE)
        return prefs.getString(USER_NAME_KEY, "Usuario Malla") ?: "Usuario Malla"
    }

    fun setUserName(context: Context, name: String) {
        context.getSharedPreferences("identity", Context.MODE_PRIVATE).edit().putString(USER_NAME_KEY, name).apply()
    }

    fun getUserStatus(context: Context): String {
        val prefs = context.getSharedPreferences("identity", Context.MODE_PRIVATE)
        return prefs.getString(USER_STATUS_KEY, "Conectado") ?: "Conectado"
    }

    fun setUserStatus(context: Context, status: String) {
        context.getSharedPreferences("identity", Context.MODE_PRIVATE).edit().putString(USER_STATUS_KEY, status).apply()
    }

    fun saveAvatar(context: Context, uri: Uri): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val file = File(context.filesDir, AVATAR_FILE)
            FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out) }
            _avatarBitmap.value = bitmap  // Notificar cambio reactivo
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    fun loadAvatar(context: Context): Bitmap? {
        val file = File(context.filesDir, AVATAR_FILE)
        return if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
    }

    fun saveBanner(context: Context, uri: Uri): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val file = File(context.filesDir, BANNER_FILE)
            FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out) }
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    fun loadBanner(context: Context): Bitmap? {
        val file = File(context.filesDir, BANNER_FILE)
        return if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
    }

    fun isRegistrationComplete(context: Context): Boolean {
        val prefs = context.getSharedPreferences("identity", Context.MODE_PRIVATE)
        return prefs.getBoolean("registration_complete", false)
    }

    fun setRegistrationComplete(context: Context, complete: Boolean) {
        context.getSharedPreferences("identity", Context.MODE_PRIVATE).edit().putBoolean("registration_complete", complete).apply()
    }

}