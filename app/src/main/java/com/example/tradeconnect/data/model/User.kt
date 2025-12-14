package com.example.tradeconnect.data.model

data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val profileImageUrl: String = "",
    val mobile: String = "",

    val isOnline: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis(),
    val bio: String = "",
    val isFollowed: Boolean = false
)
