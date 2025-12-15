package com.example.tradeconnect.model

data class Tweet(
    val id: String = "",
    val userId: String = "",
    val username: String = "",
    val content: String = "",
    val timestamp: Long = 0,

    val likes: List<String> = emptyList(),
    val saves: List<String> = emptyList(),
    val retweets: List<String> = emptyList(),  // 🆕 Liste des retweets
    val commentsCount: Int = 0
)