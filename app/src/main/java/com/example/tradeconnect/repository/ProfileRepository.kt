package com.example.tradeconnect.data.repository

import android.content.Context
import android.net.Uri
import com.example.tradeconnect.data.model.User
import com.example.tradeconnect.repository.IProfileRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProfileRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : IProfileRepository {

    override suspend fun getUserProfile(uid: String): Result<User> {
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            Result.success(doc.toObject(User::class.java)!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserProfile(user: User): Result<Unit> {
        return try {
            firestore.collection("users").document(user.uid).set(user)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadProfileImage(
        context: Context,
        uid: String,
        imageUri: Uri
    ): Result<String> {
        return Result.success("") // tu géreras Firebase Storage après
    }
}
