package com.example.tradeconnect.repository

import android.content.Context
import android.net.Uri
import com.example.tradeconnect.data.model.User

class FakeProfileRepository : IProfileRepository {

    override suspend fun getUserProfile(uid: String): Result<User> {
        return Result.success(
            User(
                uid = uid,
                username = "John Doe",
                email = "john.doe@tradeconnect.dev",
                mobile = "+33 612345678",
                profileImageUrl = ""
            )
        )
    }

    override suspend fun updateUserProfile(user: User): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun uploadProfileImage(
        context: Context,
        uid: String,
        imageUri: Uri
    ): Result<String> {
        // Fake base64 image
        return Result.success("data:image/png;base64,fake_image_data")
    }
}
