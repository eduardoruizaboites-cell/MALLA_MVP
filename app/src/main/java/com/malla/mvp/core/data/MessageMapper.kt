package com.malla.mvp.core.data

import com.malla.mvp.data.entity.MessageEntity

/**
 * Mapeador centralizado entre MessageEntity (Room) y MessageData (dominio/UI).
 * Cualquier cambio en la entidad solo requiere ajustar este archivo.
 */
object MessageMapper {
    fun toMessageData(entity: MessageEntity): MessageData {
        return MessageData(
            id = entity.id,
            conversationId = entity.conversationId,
            content = entity.content,
            timestamp = entity.timestamp,
            isOwn = entity.isOwn,
            mediaUri = entity.mediaUri,
            expireAt = entity.expireAt,
            viewOnce = entity.viewOnce,
            reaction = entity.reaction,
            fileName = entity.fileName,
            mimeType = entity.mimeType,
            fileSize = entity.fileSize,
            quotedMessageId = entity.quotedMessageId,
            quotedMessageContent = entity.quotedMessageContent,
            isEdited = entity.isEdited,
            isDeleted = entity.isDeleted,
            pollId = entity.pollId
        )
    }

    fun toMessageEntity(data: MessageData): MessageEntity {
        return MessageEntity(
            id = data.id,
            conversationId = data.conversationId,
            content = data.content,
            timestamp = data.timestamp,
            isOwn = data.isOwn,
            mediaUri = data.mediaUri,
            expireAt = data.expireAt,
            viewOnce = data.viewOnce,
            reaction = data.reaction,
            fileName = data.fileName,
            mimeType = data.mimeType,
            fileSize = data.fileSize,
            quotedMessageId = data.quotedMessageId,
            quotedMessageContent = data.quotedMessageContent,
            isEdited = data.isEdited,
            isDeleted = data.isDeleted,
            pollId = data.pollId
        )
    }
}
