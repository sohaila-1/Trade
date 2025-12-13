package com.example.tradeconnect.viewmodel

import IAuthRepository
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tradeconnect.data.model.User
import com.example.tradeconnect.repository.IProfileRepository
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout


class ProfileViewModel(
    private val profileRepo: IProfileRepository,
    private val authRepo: IAuthRepository
) : ViewModel() {


    var firstName by mutableStateOf("")
    var lastName by mutableStateOf("")
    var email by mutableStateOf("")
    var mobile by mutableStateOf("")
    var profileImageUrl by mutableStateOf("")

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)
    var isUploadingImage by mutableStateOf(false)

    private var currentUid: String? = null

    companion object {
        private const val SAVE_TIMEOUT_MS = 5_000L
    }

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        val user = authRepo.getCurrentUser() ?: run {
            errorMessage = "No user logged in"
            return
        }

        currentUid = user.uid
        isLoading = true

        viewModelScope.launch {
            profileRepo.getUserProfile(user.uid).fold(
                onSuccess = { userData ->
                    val names = userData.username.split(" ", limit = 2)
                    firstName = names.getOrNull(0).orEmpty()
                    lastName = names.getOrNull(1).orEmpty()
                    email = userData.email
                    mobile = userData.mobile
                    profileImageUrl = userData.profileImageUrl
                    isLoading = false
                },
                onFailure = {
                    isLoading = false
                    errorMessage = it.message
                }
            )
        }
    }

    fun saveProfile(onSuccess: () -> Unit) {
        val uid = currentUid ?: return

        isLoading = true
        viewModelScope.launch {
            try {
                val result = withTimeout(SAVE_TIMEOUT_MS) {
                    profileRepo.updateUserProfile(
                        User(
                            uid = uid,
                            username = "$firstName $lastName",
                            email = email,
                            mobile = mobile,
                            profileImageUrl = profileImageUrl
                        )
                    )
                }

                result.fold(
                    onSuccess = {
                        isLoading = false
                        successMessage = "Profile updated"
                        onSuccess()
                    },
                    onFailure = {
                        isLoading = false
                        errorMessage = it.message
                    }
                )
            } catch (e: TimeoutCancellationException) {
                isLoading = false
                errorMessage = "No internet connection"
            }
        }
    }

    fun uploadProfileImage(context: Context, imageUri: Uri) {
        val uid = currentUid ?: return
        isUploadingImage = true

        viewModelScope.launch {
            profileRepo.uploadProfileImage(context, uid, imageUri).fold(
                onSuccess = {
                    profileImageUrl = it
                    isUploadingImage = false
                },
                onFailure = {
                    errorMessage = it.message
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
