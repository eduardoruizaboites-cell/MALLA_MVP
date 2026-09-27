package com.malla.mvp.data.dao

import androidx.room.*
import com.malla.mvp.data.entity.ContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts WHERE isHidden = 0 ORDER BY addedAt DESC")
    fun observeAllVisible(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE contactUserId = :userId")
    suspend fun getById(userId: String): ContactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: ContactEntity)

    @Query("UPDATE contacts SET isBlocked = :blocked WHERE contactUserId = :userId")
    suspend fun setBlocked(userId: String, blocked: Boolean)

    @Query("UPDATE contacts SET isHidden = :hidden WHERE contactUserId = :userId")
    suspend fun setHidden(userId: String, hidden: Boolean)

    @Delete
    suspend fun delete(contact: ContactEntity)

    // ═════ Iter 53: pantalla Contactos ═════
    @Query("SELECT * FROM contacts WHERE isHidden = 0 ORDER BY isFavorite DESC, addedAt DESC")
    fun observeAllSorted(): Flow<List<ContactEntity>>

    @Query("UPDATE contacts SET isFavorite = :fav WHERE contactUserId = :userId")
    suspend fun setFavorite(userId: String, fav: Boolean)

    @Query("UPDATE contacts SET isMuted = :muted WHERE contactUserId = :userId")
    suspend fun setMuted(userId: String, muted: Boolean)

    @Query("DELETE FROM contacts WHERE contactUserId = :userId")
    suspend fun deleteById(userId: String)

    @Query("DELETE FROM conversations WHERE id = :userId")
    suspend fun deleteConversationById(userId: String)

    @Query("DELETE FROM messages WHERE conversationId = :userId")
    suspend fun deleteMessagesByConversation(userId: String)

    /**
     * Iter 53: borrado en cascada. Elimina contacto + conversación + todos los mensajes
     * en una sola transacción. Es el que usa el botón "Eliminar" de la pantalla Contactos.
     */
    @Transaction
    suspend fun deleteCascade(userId: String) {
        deleteMessagesByConversation(userId)
        deleteConversationById(userId)
        deleteById(userId)
    }
}
