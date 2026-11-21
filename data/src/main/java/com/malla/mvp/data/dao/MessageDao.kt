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

    // Iter 52c: IGNORE en vez de REPLACE. Con el ID determinístico, la segunda
    // llegada del mismo mensaje es no-op en vez de pisar la fila existente
    // (evita perder status, isEdited, reaction actualizados entre medio).
    @Insert(onConflict = OnConflictStrategy.IGNORE)
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

    @Query("UPDATE messages SET isPinned = :pinned WHERE id = :messageId")
    suspend fun setPinned(messageId: String, pinned: Boolean)

    @Query("UPDATE messages SET status = :newStatus WHERE conversationId = :conversationId AND isOwn = :isOwn")
    suspend fun updateStatusForConversationAndOwn(conversationId: String, isOwn: Boolean, newStatus: Int)

    // ══════════ Iter 73 (1b): queries para cola persistente de reintentos ══════════

    /**
     * Cola de mensajes pendientes de reintento (WorkManager).
     * Solo mensajes propios, en estado PENDING, con menos de [maxRetries] intentos.
     * Ordenados por antigüedad para procesar FIFO. El índice (status) hace la
     * consulta O(log n); el índice compuesto (conversationId, status) se usa para
     * la variante por conversación.
     */
    @Query("SELECT * FROM messages WHERE isOwn = 1 AND status = 0 AND retryCount < :maxRetries ORDER BY timestamp ASC")
    suspend fun getPendingMessagesForRetry(maxRetries: Int): List<MessageEntity>

    /** Variante por conversación — útil para el botón "reintentar ahora" de la UI. */
    @Query("SELECT * FROM messages WHERE isOwn = 1 AND status = 0 AND retryCount < :maxRetries AND conversationId = :conversationId ORDER BY timestamp ASC")
    suspend fun getPendingMessagesForRetryInConversation(conversationId: String, maxRetries: Int): List<MessageEntity>

    /** Incrementa el contador y registra el último intento (para backoff exponencial). */
    @Query("UPDATE messages SET retryCount = retryCount + 1, lastRetryAt = :now WHERE id = :messageId")
    suspend fun incrementRetry(messageId: String, now: Long)

    /** Marca el mensaje como FAILED definitivo con causa. No se reintenta más. */
    @Query("UPDATE messages SET status = 3, errorMessage = :error WHERE id = :messageId")
    suspend fun markFailed(messageId: String, error: String?)
}
