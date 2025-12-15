package com.example.tradeconnect.model

data class Comment(
    val id: String = "",
    val tweetId: String = "",
    val userId: String = "",
    val username: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val likes: List<String> = emptyList()
) {
    val likesCount: Int get() = likes.size
}