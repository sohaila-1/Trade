package com.example.tradeconnect.model

import com.google.firebase.firestore.DocumentId

data class Tweet(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val username: String = "",
    val content: String = "",
    val timestamp: Long = 0L
)

