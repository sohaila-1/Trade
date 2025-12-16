package com.example.tradeconnect.data.model

data class Message(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.SENT,
    val isSentByCurrentUser: Boolean = false
)

enum class MessageStatus {
    PENDING,  // Not sent yet (offline)
    SENT,     // Sent to server
    DELIVERED, // Received by other user's device
    SEEN      // Seen by other user
}