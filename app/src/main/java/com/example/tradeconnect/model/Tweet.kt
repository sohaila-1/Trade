package com.example.tradeconnect.model

data class Tweet(
    val id: String = "",
    val userId: String = "",
    val username: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
