// app/src/main/java/com/example/tradeconnect/model/Tweet.kt
package com.example.tradeconnect.model

data class Tweet(
    val id: String = "",
    val userId: String = "",
    val username: String = "",
    val content: String = "",
    val timestamp: Long = 0,

    val likes: List<String> = emptyList(),
    val saves: List<String> = emptyList(),
    val commentsCount: Int = 0
)