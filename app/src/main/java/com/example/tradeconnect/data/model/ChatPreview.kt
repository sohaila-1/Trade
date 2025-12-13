package com.example.tradeconnect.data.model

data class ChatPreview(
    val user: User,
    val lastMessage: String = "",
    val lastMessageTime: Long = 0,
    val unreadCount: Int = 0
)