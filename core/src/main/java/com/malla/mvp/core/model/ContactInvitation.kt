package com.malla.mvp.core.model

data class ContactInvitation(
    val senderUserId: String,
    val senderDisplayName: String,
    val senderAvatarSeed: Int,
    val senderPublicKey: String,
    val preferredChannels: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val nonce: String = java.util.UUID.randomUUID().toString()
)
