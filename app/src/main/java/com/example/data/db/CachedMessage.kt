package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_messages")
data class CachedMessage(
    @PrimaryKey val compositeId: String, // mailboxAddress + "_" + messageId
    val messageId: Long,
    val mailboxAddress: String,
    val fromAddress: String,
    val subject: String,
    val dateString: String,
    val bodyText: String = "",
    val bodyHtml: String = "",
    val isRead: Boolean = false,
    val extractedOtp: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
