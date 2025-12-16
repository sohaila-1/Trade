package com.example.tradeconnect.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_users")
data class UserEntity(
    @PrimaryKey
    val uid: String,
    val email: String,
    val username: String,
    val mobile: String,
    val profileImageUrl: String,
    val lastCached: Long = System.currentTimeMillis()
)