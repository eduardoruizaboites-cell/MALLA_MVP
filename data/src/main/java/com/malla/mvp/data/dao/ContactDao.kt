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
}
