package com.example.tradeconnect.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tradeconnect.data.model.User
import com.example.tradeconnect.data.repository.IAuthRepository
import com.example.tradeconnect.data.repository.IProfileRepository
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.net.UnknownHostException

class ProfileViewModel(
    private val profileRepo: IProfileRepository,
    private val authRepo: IAuthRepository
) : ViewModel() {

    // Form fields
    var firstName by mutableStateOf("")
    var lastName by mutableStateOf("")
    var email by mutableStateOf("")
    var mobile by mutableStateOf("")
    var profileImageUrl by mutableStateOf("")

    // UI state
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)
    var isUploadingImage by mutableStateOf(false)
    var isOffline by mutableStateOf(false)

    private var currentUid: String? = null

    companion object {
        private const val SAVE_TIMEOUT_MS = 5_000L // 10 seconds timeout for save
    }

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        val user = authRepo.getCurrentUser()
        if (user == null) {
            errorMessage = "No user logged in"
            return
        }

        currentUid = user.uid
        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            profileRepo.getUserProfile(user.uid).fold(
                onSuccess = { userData ->
                    // Parse the username into first and last name
                    val names = userData.username.split(" ", limit = 2)
                    firstName = names.getOrNull(0) ?: ""
                    lastName = names.getOrNull(1) ?: ""
                    email = userData.email
                    mobile = userData.mobile
                    profileImageUrl = userData.profileImageUrl
                    isLoading = false
                },
                onFailure = { exception ->
                    isLoading = false
                    errorMessage = exception.message ?: "Failed to load profile"
                }
            )
        }
    }

    fun saveProfile(onSuccess: () -> Unit) {
        val uid = currentUid
        if (uid == null) {
            errorMessage = "No user logged in"
            return
        }

        // Validate fields
        if (firstName.isBlank()) {
            errorMessage = "First name is required"
            return
        }

        if (lastName.isBlank()) {
            errorMessage = "Last name is required"
            return
        }

        if (email.isBlank()) {
            errorMessage = "Email is required"
            return
        }

        if (!isValidEmail(email)) {
            errorMessage = "Please enter a valid email address"
            return
        }

        isLoading = true
        errorMessage = null
        successMessage = null

        viewModelScope.launch {
            try {
                // Add timeout to detect offline state when saving
                val result = withTimeout(SAVE_TIMEOUT_MS) {
                    val username = "$firstName $lastName".trim()
                    val updatedUser = User(
                        uid = uid,
                        username = username,
                        email = email,
                        mobile = mobile,
                        profileImageUrl = profileImageUrl
                    )
                    profileRepo.updateUserProfile(updatedUser)
                }

                result.fold(
                    onSuccess = {
                        isLoading = false
                        successMessage = "Profile updated successfully"
                        onSuccess()
                    },
                    onFailure = { exception ->
                        isLoading = false
                        if (isNetworkError(exception)) {
                            errorMessage = "No internet connection. Please try again."
                        } else {
                            errorMessage = exception.message ?: "Failed to update profile"
                        }
                    }
                )
            } catch (e: TimeoutCancellationException) {
                // Timeout - likely offline
                isLoading = false
                errorMessage = "No internet connection. Please try again."
            } catch (e: Exception) {
                isLoading = false
                if (isNetworkError(e)) {
                    errorMessage = "No internet connection. Please try again."
                } else {
                    errorMessage = e.message ?: "Failed to update profile"
                }
            }
        }
    }

    fun uploadProfileImage(context: Context, imageUri: Uri) {
        val uid = currentUid
        if (uid == null) {
            errorMessage = "No user logged in"
            return
        }

        isUploadingImage = true
        errorMessage = null

        viewModelScope.launch {
            profileRepo.uploadProfileImage(context, uid, imageUri).fold(
                onSuccess = { base64String ->
                    profileImageUrl = base64String
                    isUploadingImage = false
                },
                onFailure = { exception ->
                    errorMessage = exception.message ?: "Failed to upload image"
                    isUploadingImage = false
                }
            )
        }
    }

    /**
     * Change user password using Firebase Auth
     * Requires current password for re-authentication (Firebase security requirement)
     */
    fun changePassword(
        currentPassword: String,
        newPassword: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val user = authRepo.getCurrentUser()
        if (user == null) {
            onResult(false, "No user logged in")
            return
        }

        val userEmail = user.email
        if (userEmail.isNullOrBlank()) {
            onResult(false, "User email not found")
            return
        }

        // Validate passwords
        if (currentPassword.isBlank()) {
            onResult(false, "Please enter your current password")
            return
        }

        if (newPassword.length < 6) {
            onResult(false, "New password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            try {
                // First, re-authenticate the user with their current password
                val credential = com.google.firebase.auth.EmailAuthProvider
                    .getCredential(userEmail, currentPassword)

                user.reauthenticate(credential)
                    .addOnCompleteListener { reauthTask ->
                        if (reauthTask.isSuccessful) {
                            // Re-authentication successful, now update password
                            user.updatePassword(newPassword)
                                .addOnCompleteListener { updateTask ->
                                    if (updateTask.isSuccessful) {
                                        onResult(true, null)
                                    } else {
                                        val error = updateTask.exception?.message
                                            ?: "Failed to change password"
                                        onResult(false, mapPasswordError(error))
                                    }
                                }
                        } else {
                            // Re-authentication failed
                            val error = reauthTask.exception?.message
                                ?: "Authentication failed"
                            onResult(false, mapPasswordError(error))
                        }
                    }
            } catch (e: Exception) {
                val errorMsg = if (isNetworkError(e)) {
                    "No internet connection. Please try again."
                } else {
                    e.message ?: "Failed to change password"
                }
                onResult(false, errorMsg)
            }
        }
    }

    /**
     * Map Firebase password errors to user-friendly messages
     */
    private fun mapPasswordError(error: String): String {
        return when {
            error.contains("wrong-password", ignoreCase = true) ||
                    error.contains("invalid-credential", ignoreCase = true) ||
                    error.contains("password is invalid", ignoreCase = true) ->
                "Current password is incorrect"
            error.contains("requires-recent-login", ignoreCase = true) ->
                "Please log out and log in again before changing your password"
            error.contains("network", ignoreCase = true) ->
                "No internet connection. Please try again."
            error.contains("weak-password", ignoreCase = true) ->
                "New password is too weak. Use at least 6 characters."
            error.contains("too-many-requests", ignoreCase = true) ->
                "Too many attempts. Please try again later."
            else -> error
        }
    }

    /**
     * Clear messages (useful when navigating away)
     */
    fun clearMessages() {
        errorMessage = null
        successMessage = null
    }

    /**
     * Check if exception is a network-related error
     */
    private fun isNetworkError(exception: Throwable): Boolean {
        return exception is UnknownHostException ||
                exception.message?.contains("network", ignoreCase = true) == true ||
                exception.message?.contains("internet", ignoreCase = true) == true ||
                exception.message?.contains("connection", ignoreCase = true) == true ||
                exception.message?.contains("timeout", ignoreCase = true) == true ||
                exception.message?.contains("unreachable", ignoreCase = true) == true ||
                exception.cause is UnknownHostException
    }

    /**
     * Simple email validation
     */
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    class Factory(
        private val profileRepo: IProfileRepository,
        private val authRepo: IAuthRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProfileViewModel(profileRepo, authRepo) as T
        }
    }
}