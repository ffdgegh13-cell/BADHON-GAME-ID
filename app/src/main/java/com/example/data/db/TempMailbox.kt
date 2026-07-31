package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "temp_mailboxes")
data class TempMailbox(
    @PrimaryKey val emailAddress: String,
    val username: String,
    val domain: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val lastChecked: Long = System.currentTimeMillis()
)
