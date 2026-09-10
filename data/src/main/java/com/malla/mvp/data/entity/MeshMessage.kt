package com.malla.mvp.data.entity

data class MeshMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val senderId: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isOwn: Boolean = false,
    val status: Int = 0,                  // 0=enviado, 1=entregado, 2=leído
    val type: String = "chat",            // "chat", "ack", "poll_create", "typing", etc.
    val messageId: String? = null,
    val quotedMessageId: String? = null,
    val quotedMessageContent: String? = null,
    val expireAt: Long? = null,
    val viewOnce: Boolean = false,
    val originalMessageId: String? = null,
    val senderName: String? = null,       // Display name real del emisor (para título del chat)
    val senderAvatarSeed: Int = 0         // Seed para avatar determinista en receptor
)
