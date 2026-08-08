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
import java.util.UUID

object InvitationManager {
    private val _incomingInvitation = MutableSharedFlow<ContactInvitation>(replay = 0)
    val incomingInvitation = _incomingInvitation.asSharedFlow()
    private val invitationCharUuid = UUID.fromString("0000abcd-0002-1000-8000-00805f9b34fb")

    suspend fun sendInvitation(context: Context, user: NearbyUser) {
        val myId = IdentityManager.getIdentityId()
        val myName = IdentityManager.getUserName(context)
        val myPubKey = IdentityManager.getPublicKeyBase64() ?: ""
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
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Solicitud enviada a ${user.displayName} (sin BLE)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun receiveInvitation(invitation: ContactInvitation) {
        _incomingInvitation.tryEmit(invitation)
    }
}
