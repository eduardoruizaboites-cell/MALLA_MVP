package com.malla.mvp.data.dao

import androidx.room.*
import com.malla.mvp.data.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<MessageEntity>>
    
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    suspend fun getMessagesForConversationOnce(conversationId: String): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("UPDATE messages SET status = :status WHERE id = :messageId")
    suspend fun updateStatus(messageId: String, status: Int)

    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: String)

    @Query("UPDATE messages SET reaction = :reaction WHERE id = :messageId")
    suspend fun updateReaction(messageId: String, reaction: String?)

    @Query("UPDATE messages SET content = :content, isEdited = 1 WHERE id = :messageId")
    suspend fun updateContent(messageId: String, content: String)

    @Query("UPDATE messages SET isDeleted = 1, content = 'Mensaje eliminado' WHERE id = :messageId")
    suspend fun markAsDeleted(messageId: String)

    @Query("SELECT * FROM messages WHERE id = :id")
    suspend fun getMessageById(id: String): MessageEntity?

    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    fun observeAllMessages(): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<MessageEntity>)

    @Query("DELETE FROM messages WHERE timestamp < :threshold")
    suspend fun deleteMessagesOlderThan(threshold: Long)

    @Query("DELETE FROM messages WHERE expireAt IS NOT NULL AND expireAt < :now AND conversationId = :convId")
    suspend fun deleteExpiredMessages(convId: String, now: Long)
}
