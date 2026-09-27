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
    // Iter 53: tipo de mensaje (chat, sms, ack, read_all, typing, invitation, accept, zumbido, poll_create, poll_vote, reaction, edit, delete_for_all). Permite filtrar control messages de la UI sin heurísticas.
    val type: String = "chat",
    val reaction: String? = null,
    val expireAt: Long? = null,
    val mediaUri: String? = null,
    val viewOnce: Boolean = false,
    val quotedMessageId: String? = null,
    val quotedMessageContent: String? = null,
    val encrypted: Boolean = false,
    val fileName: String? = null,
    val mimeType: String? = null,
    val fileSize: Long? = null,
    val isEdited: Boolean = false,
    val isDeleted: Boolean = false,
    val pollId: String? = null,
    val isPinned: Boolean = false
)
