package com.malla.mvp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isOwn: Boolean = false,
    val status: Int = 0,
    val reaction: String? = null,
    val expireAt: Long? = null,
    val mediaUri: String? = null,
    val viewOnce: Boolean = false,
    val quotedMessageId: String? = null,
    val quotedMessageContent: String? = null,
    val encrypted: Boolean = false,  // NUEVO: indica si 'content' está cifrado

    val fileName: String? = null,
    val mimeType: String? = null,
    val fileSize: Long? = null
)
