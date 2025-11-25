package com.example.tradeconnect.model

data class Tweet(
    val id: String = "",
    val userId: String = "",
    val username: String = "",
    val content: String = "",
    val timestamp: Long = 0,

    // Pour ton design du feed ↓↓↓
    val replies: Int = 0,
    val retweets: Int = 0,
    val likes: Int = 0,
    val imageRes: Int? = null
)
