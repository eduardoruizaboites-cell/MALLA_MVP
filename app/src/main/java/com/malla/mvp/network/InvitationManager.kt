package com.malla.mvp.network

import android.content.Context
import android.widget.Toast
import com.malla.mvp.core.model.ContactInvitation
import com.malla.mvp.core.model.NearbyUser
import com.malla.mvp.identity.IdentityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import android.bluetooth.BluetoothAdapter
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object InvitationManager {
    private val _incomingInvitation = MutableSharedFlow<ContactInvitation>(replay = 0)
    private val _acceptanceReceived = MutableSharedFlow<Pair<String,Int>>(replay = 0)
    val acceptanceReceived = _acceptanceReceived.asSharedFlow()
    val incomingInvitation = _incomingInvitation.asSharedFlow()
    private val invitationCharUuid = UUID.fromString("0000abcd-0002-1000-8000-00805f9b34fb")
    private const val PREFS_NAME = "invitation_codes"
    private const val EXPIRATION_MS = 24 * 60 * 60 * 1000L

    /**
     * Genera un código de invitación de 12 dígitos con expiración de 24h.
     * Devuelve el código como String.
     */
    fun generateInvitationCode(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val code = java.util.UUID.randomUUID().toString()
            .replace("-", "")
            .take(12)
            .uppercase()
        val userId = IdentityManager.getIdentityId()
        val localIp = DhtService.getLocalAddress() ?: "unknown"
        val timestamp = System.currentTimeMillis()
        prefs.edit()
            .putString("code_$code", userId)
            .putLong("code_time_$code", timestamp)
            .putString("last_user_id", userId)
            .putString("last_local_ip", localIp)
            .apply()
        return code
    }

    /**
     * Valida un código de invitación de 12 dígitos.
     * Devuelve el userId asociado si es válido y no ha expirado, o null si es inválido/expirado.
     */
    fun validateInvitationCode(context: Context, code: String): String? {
        val normalized = code.trim().uppercase()
        if (normalized.length != 12) return null
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val userId = prefs.getString("code_$normalized", null) ?: return null
        val timestamp = prefs.getLong("code_time_$normalized", 0L)
        if (System.currentTimeMillis() - timestamp > EXPIRATION_MS) {
            prefs.edit().remove("code_$normalized").remove("code_time_$normalized").apply()
            return null
        }
        return userId
    }

    suspend fun sendInvitation(context: Context, user: NearbyUser) {
        val myId = IdentityManager.getIdentityId()
        val myName = IdentityManager.getUserName(context)
        val myPubKey = IdentityManager.getPublicKeyBase64() ?: ""
        val myIp = DhtService.getLocalAddress() ?: ""
        val invitation = ContactInvitation(
            senderUserId = myId,
            senderDisplayName = myName,
            senderAvatarSeed = 0,
            senderPublicKey = myPubKey,
            preferredChannels = listOf("BLE", "mDNS", "DHT")
        )

        val json = JSONObject().apply {
            put("senderUserId", invitation.senderUserId)
            put("senderDisplayName", invitation.senderDisplayName)
            put("senderAvatarSeed", invitation.senderAvatarSeed)
            put("senderPublicKey", invitation.senderPublicKey)
            put("timestamp", invitation.timestamp)
            put("nonce", invitation.nonce)
            put("senderDeviceAddress", BleManager.getAdapter()?.address ?: "")
            put("senderLocalIp", myIp)
        }.toString()

        if (user.bluetoothDevice != null) {
            withContext(Dispatchers.IO) {
                val success = BleManager.connectAndWriteData(
                    user.bluetoothDevice!!,
                    invitationCharUuid,
                    json.toByteArray(Charsets.UTF_8)
                )
                withContext(Dispatchers.Main) {
                    if (success) {
                        Toast.makeText(context, "Solicitud enviada a ${user.displayName}", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Error al enviar solicitud", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Toast.makeText(context, "Solicitud enviada a ${user.displayName} (sin BLE)", Toast.LENGTH_SHORT).show()
        }
    }

    suspend fun sendAcceptance(context: Context, invitation: ContactInvitation) {
        val myName = IdentityManager.getUserName(context)
        val myAvatarSeed = 0
        val myIp = DhtService.getLocalAddress() ?: ""
        val payload = "ACCEPT|${invitation.senderUserId}|$myName|$myAvatarSeed|$myIp"
        BleManager.startAdvertisingWithPayload(payload)
        kotlinx.coroutines.delay(30_000)
        BleManager.stopProximityAdvertising()
    }

    fun receiveInvitation(invitation: ContactInvitation) {
        _incomingInvitation.tryEmit(invitation)
    }
}
