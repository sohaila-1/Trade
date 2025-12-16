package com.example.tradeconnect.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.example.tradeconnect.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

class ProfileRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : IProfileRepository {

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val MAX_IMAGE_SIZE = 800 // Max width/height in pixels
    }

    override suspend fun getUserProfile(uid: String): Result<User> {
        return try {
            val document = firestore.collection(USERS_COLLECTION)
                .document(uid)
                .get()
                .await()

            if (document.exists()) {
                val user = document.toObject(User::class.java)
                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("Failed to parse user data"))
                }
            } else {
                // This should rarely happen now since AuthRepository creates the document
                // But keep as fallback
                val newUser = User(
                    uid = uid,
                    email = auth.currentUser?.email ?: "",
                    username = "",
                    mobile = "",
                    profileImageUrl = ""
                )
                firestore.collection(USERS_COLLECTION)
                    .document(uid)
                    .set(newUser)
                    .await()
                Result.success(newUser)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserProfile(user: User): Result<Unit> {
        return try {
            // Use set with merge to update only the fields provided
            firestore.collection(USERS_COLLECTION)
                .document(user.uid)
                .set(user)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadProfileImage(context: Context, uid: String, imageUri: Uri): Result<String> {
        return try {
            // Read the image from URI
            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: return Result.failure(Exception("Failed to open image"))

            // Decode bitmap
            var bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            // Resize bitmap to reduce size
            bitmap = resizeBitmap(bitmap, MAX_IMAGE_SIZE)

            // Convert to base64
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val byteArray = outputStream.toByteArray()
            val base64String = Base64.encodeToString(byteArray, Base64.DEFAULT)

            // Create data URL format
            val dataUrl = "data:image/jpeg;base64,$base64String"

            Result.success(dataUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxSize && height <= maxSize) {
            return bitmap
        }

        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int

        if (width > height) {
            newWidth = maxSize
            newHeight = (maxSize / ratio).toInt()
        } else {
            newHeight = maxSize
            newWidth = (maxSize * ratio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}

// Fake implementation for previews
class FakeProfileRepository : IProfileRepository {
    override suspend fun getUserProfile(uid: String): Result<User> {
        return Result.success(
            User(
                uid = uid,
                username = "John Doe",
                email = "johndoe@gmail.com",
                mobile = "+91-123456789",
                profileImageUrl = ""
            )
        )
    }

    override suspend fun updateUserProfile(user: User): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun uploadProfileImage(context: Context, uid: String, imageUri: Uri): Result<String> {
        return Result.success("data:image/jpeg;base64,fake_base64_string")
    }
}