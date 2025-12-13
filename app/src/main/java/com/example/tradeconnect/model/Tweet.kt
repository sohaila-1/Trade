package com.example.tradeconnect.model

data class Tweet(
    val id: String = "",
    val userId: String = "",
    val username: String = "",
    val content: String = "",
    val timestamp: Long = 0,

    val likes: List<String> = emptyList(),   // utilisateurs qui ont liké
    val saves: List<String> = emptyList()    // utilisateurs qui ont sauvegardé
)


