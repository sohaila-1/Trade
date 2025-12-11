package com.example.tradeconnect.data.model

data class User(
    val uid: String = "",
    val username: String = "",
    val bio: String = "",
    val isFollowed: Boolean = false
)
