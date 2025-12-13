package com.example.tradeconnect.repository

import android.content.Context
import android.net.Uri
import com.example.tradeconnect.data.model.User

interface IProfileRepository {
    suspend fun getUserProfile(uid: String): Result<User>
    suspend fun updateUserProfile(user: User): Result<Unit>
    suspend fun uploadProfileImage(
        context: Context,
        uid: String,
        imageUri: Uri
    ): Result<String>
}
