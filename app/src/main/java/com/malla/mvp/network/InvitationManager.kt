package com.malla.mvp.network

import android.content.Context
import com.malla.mvp.core.engine.DiagnosticsLogger
import android.widget.Toast
import com.malla.mvp.core.model.ContactInvitation
import com.malla.mvp.core.model.NearbyUser
import com.malla.mvp.identity.IdentityManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import android.bluetooth.BluetoothAdapter
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object InvitationManager {
    private var appContext: Context? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _incomingInvitation = MutableSharedFlow<ContactInvitation>(replay = 0)
    private val _acceptanceReceived = MutableSharedFlow<Pair<String,Int>>(replay = 0)
    val acceptanceReceived = _acceptanceReceived.asSharedFlow()
    val incomingInvitation = _incomingInvitation.asSharedFlow()
    private val invitationCharUuid = UUID.fromString("0000abcd-0002-1000-8000-00805f9b34fb")
    private const val PREFS_NAME = "invitation_codes"
    private const val EXPIRATION_MS = 24 * 60 * 60 * 1000L

    /**
     * Inicia la escucha de invitaciones BLE entrantes.
     * Debe llamarse al arrancar la app.
     */
    fun start(context: Context) {
        appContext = context.applicationContext
        scope.launch {
            BleTransport.invitationPayloads.collect { payload ->
                processIncomingInvitationPayload(payload)
            }
        }
    }

    fun stop() {
        scope.cancel()
    }

    private fun processIncomingInvitationPayload(payload: String) {
        try {
            val json = JSONObject(payload)
            val invitation = ContactInvitation(
                senderUserId = json.optString("senderUserId", ""),
                senderDisplayName = json.optString("senderDisplayName", "Usuario Malla"),
                senderAvatarSeed = json.optInt("senderAvatarSeed", 0),
                senderPublicKey = json.optString("senderPublicKey", ""),
                preferredChannels = listOf("BLE")
            )
            _incomingInvitation.tryEmit(invitation)
            DiagnosticsLogger.log("InvitationManager", "Invitación procesada de ${invitation.senderDisplayName}")
        } catch (e: Exception) {
            DiagnosticsLogger.log("InvitationManager", "Error parseando invitación: ${e.message}")
        }
    }

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
        DiagnosticsLogger.log("InvitationManager", "Código generado: $code para userId=$userId")
        return code
    }

    /**
     * Valida un código de invitación de 12 dígitos.
     * Devuelve el userId asociado si es válido y no ha expirado, o null si es inválido/expirado.
     */
    fun validateInvitationCode(context: Context, code: String): String? {
        val normalized = code.trim().uppercase()
        if (normalized.length != 12) {
            DiagnosticsLogger.log("InvitationManager", "Código inválido: longitud incorrecta")
            return null
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val userId = prefs.getString("code_$normalized", null)
        val timestamp = prefs.getLong("code_time_$normalized", 0L)
        if (userId == null) {
            DiagnosticsLogger.log("InvitationManager", "Código no encontrado: $normalized")
            return null
        }
        val expired = System.currentTimeMillis() - timestamp > EXPIRATION_MS
        if (expired) {
            DiagnosticsLogger.log("InvitationManager", "Código expirado: $normalized")
            return null
        }
        DiagnosticsLogger.log("InvitationManager", "Código válido: $normalized, userId=$userId")
        return userId
    }

    suspend fun sendInvitation(context: Context, user: NearbyUser) {
        // Log al PRINCIPIO ABSOLUTO — si esto no aparece, la corrutina no llegó a ejecutarse
        DiagnosticsLogger.log("InvitationManager", "[sendInvitation] INICIO user=${user.displayName} userId=${user.userId} device=${user.bluetoothDevice?.address ?: "null"}")
        try {
            val myId = IdentityManager.getIdentityId()
            DiagnosticsLogger.log("InvitationManager", "[sendInvitation] myId=$myId")
            val myName = IdentityManager.getUserName(context)
            val myPubKey = IdentityManager.getPublicKeyBase64() ?: ""
            DiagnosticsLogger.log("InvitationManager", "[sendInvitation] pubKey length=${myPubKey.length}")
            val myIp = DhtService.getLocalAddress() ?: ""
            DiagnosticsLogger.log("InvitationManager", "[sendInvitation] myIp=$myIp")

            val invitation = ContactInvitation(
                senderUserId = myId,
                senderDisplayName = myName,
                senderAvatarSeed = 0,
                senderPublicKey = myPubKey,
                preferredChannels = listOf("BLE", "mDNS", "DHT")
            )
            DiagnosticsLogger.log("InvitationManager", "[sendInvitation] ContactInvitation creada")

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
            DiagnosticsLogger.log("InvitationManager", "[sendInvitation] JSON preparado (${json.length} chars)")

            DiagnosticsLogger.log("InvitationManager", "Enviando invitación a ${user.displayName} (userId=${user.userId}, device=${user.bluetoothDevice?.address ?: "sin BLE"})")

            // Fallback: si no hay device BLE directo, intentar buscar en foundBluetoothDevices
            val targetDevice = user.bluetoothDevice
                ?: BleManager.foundBluetoothDevices.value.firstOrNull { it.address == user.bluetoothDevice?.address }

            if (targetDevice != null) {
                DiagnosticsLogger.log("InvitationManager", "[sendInvitation] Enviando por BLE a ${targetDevice.address}")
                BleTransport.sendInvitation(targetDevice, json.toByteArray(Charsets.UTF_8))
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Solicitud enviada a ${user.displayName}", Toast.LENGTH_SHORT).show()
                }
            } else {
                DiagnosticsLogger.log("InvitationManager", "[sendInvitation] SIN device BLE — fallback broadcast")
                // Fallback: escribir por cualquier GATT conectado
                val sent = BleTransport.broadcast(json.toByteArray(Charsets.UTF_8))
                DiagnosticsLogger.log("InvitationManager", "[sendInvitation] broadcast=$sent")
                withContext(Dispatchers.Main) {
                    val msg = if (sent) "Solicitud difundida a ${user.displayName}" else "Sin canal BLE disponible para ${user.displayName}"
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Throwable) {
            val stack = e.stackTraceToString().take(600)
            DiagnosticsLogger.log("InvitationManager", "[sendInvitation] EXCEPCIÓN: ${e.javaClass.simpleName}: ${e.message}")
            DiagnosticsLogger.log("InvitationManager", "[sendInvitation] STACK: $stack")
            try {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error invitación: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } catch (_: Exception) {}
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
