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
import kotlinx.coroutines.launch

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

    private var currentUid: String? = null

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
                    errorMessage = exception.message ?: "Failed to load profile"
                    isLoading = false
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

        isLoading = true
        errorMessage = null
        successMessage = null

        viewModelScope.launch {
            val username = "$firstName $lastName".trim()
            val updatedUser = User(
                uid = uid,
                username = username,
                email = email,
                mobile = mobile,
                profileImageUrl = profileImageUrl
            )

            profileRepo.updateUserProfile(updatedUser).fold(
                onSuccess = {
                    isLoading = false
                    successMessage = "Profile updated successfully"
                    onSuccess()
                },
                onFailure = { exception ->
                    isLoading = false
                    errorMessage = exception.message ?: "Failed to update profile"
                }
            )
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