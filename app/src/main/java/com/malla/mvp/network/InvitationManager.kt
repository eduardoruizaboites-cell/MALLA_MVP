package com.malla.mvp.network

import android.content.Context
import android.widget.Toast
import com.malla.mvp.core.model.ContactInvitation
import com.malla.mvp.identity.IdentityManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object InvitationManager {
    private val _incomingInvitation = MutableSharedFlow<ContactInvitation>(replay = 0)
    val incomingInvitation = _incomingInvitation.asSharedFlow()

    suspend fun sendInvitation(
        context: Context,
        targetToken: String,   // token del usuario cercano
        targetDisplayName: String,
        targetAvatarSeed: Int
    ) {
        // Construir la invitación con nuestros datos
        val myId = IdentityManager.getIdentityId()
        val myName = IdentityManager.getUserName(context)
        val myPubKey = IdentityManager.getPublicKeyBase64() ?: ""
        val invitation = ContactInvitation(
            senderUserId = myId,
            senderDisplayName = myName,
            senderAvatarSeed = 0, // se podría calcular
            senderPublicKey = myPubKey,
            preferredChannels = listOf("BLE", "mDNS", "DHT")
        )

        // TODO: Enviar por BLE usando BleManager (característica de invitación)
        // Por ahora, simulamos el envío y mostramos un toast
        with(android.os.Looper.getMainLooper()) {
            Toast.makeText(context, "Solicitud enviada a $targetDisplayName", Toast.LENGTH_SHORT).show()
        }
    }

    fun receiveInvitation(invitation: ContactInvitation) {
        _incomingInvitation.tryEmit(invitation)
    }
}
