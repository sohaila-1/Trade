package com.example.tradeconnect.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.tradeconnect.data.model.MessageStatus

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    val id: String,
    val senderId: String,
    val receiverId: String,
    val text: String,
    val timestamp: Long,
    val status: String, // Store as String
    val isSentByCurrentUser: Boolean,
    val chatPartnerId: String, // For querying conversations
    val ownerUserId: String = "" // The logged-in user who owns this message cache
)