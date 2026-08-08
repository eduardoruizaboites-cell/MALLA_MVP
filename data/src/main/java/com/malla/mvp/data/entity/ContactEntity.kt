package com.malla.mvp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val contactUserId: String,
    val displayName: String,
    val avatarSeed: Int,
    val publicKey: String,
    val addedAt: Long,
    val isBlocked: Boolean = false,
    val isHidden: Boolean = false
)
