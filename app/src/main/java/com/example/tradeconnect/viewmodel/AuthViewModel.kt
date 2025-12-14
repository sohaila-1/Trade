package com.example.tradeconnect.viewmodel

import IAuthRepository
import android.util.Patterns
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tradeconnect.data.datastore.IUserPreferences
import com.example.tradeconnect.data.repository.MessageRepository
import com.example.tradeconnect.model.AppUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repo: IAuthRepository,
    private val prefs: IUserPreferences,
    private val messageRepository: MessageRepository? = null
) : ViewModel() {

    /* -------------------------
       FORM FIELDS
     ------------------------- */
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var firstName by mutableStateOf("")
    var lastName by mutableStateOf("")
    var phone by mutableStateOf("")
    var username by mutableStateOf("")   // ✅ IMPORTANT
    var errorMessage by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(false)

    /* -------------------------
       REMEMBER ME
     ------------------------- */
    var rememberMe by mutableStateOf(true)
        private set

    fun updateRememberMe(value: Boolean) {
        rememberMe = value
        viewModelScope.launch { prefs.setRememberMe(value) }
    }

    /* -------------------------
       AUTH STATE
     ------------------------- */
    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn = _isLoggedIn

    var currentUser = mutableStateOf<AppUser?>(null)
        private set

    /* -------------------------
       INIT
     ------------------------- */
    init {
        viewModelScope.launch {
            prefs.rememberMeFlow.collectLatest { shouldRemember ->
                val user = repo.getCurrentUserModel()
                if (shouldRemember && user != null) {
                    currentUser.value = user
                    _isLoggedIn.value = true
                } else {
                    _isLoggedIn.value = false
                }
            }
        }
    }

    /* -------------------------
       LOGIN
     ------------------------- */
    fun login(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        this.email = email
        this.password = password

        if (!validateLoginFields()) {
            onResult(false, errorMessage)
            return
        }

        isLoading = true
        errorMessage = null

        repo.login(email.trim(), password) { success, error ->
            isLoading = false

            if (!success) {
                errorMessage = mapFirebaseError(error)
                onResult(false, errorMessage)
                return@login
            }

            currentUser.value = repo.getCurrentUserModel()
            _isLoggedIn.value = true

            viewModelScope.launch {
                prefs.setRememberMe(rememberMe)
            }

            onResult(true, null)
        }
    }

    /* -------------------------
       SIGN UP
     ------------------------- */
    fun signUp(onSuccess: () -> Unit) {
        if (!validateSignUpFields()) return

        isLoading = true
        errorMessage = null

        repo.signUp(
            email.trim(),
            password,
            firstName.trim(),
            lastName.trim(),
            phone.trim(),
            username.trim() // ✅ PASSAGE DU USERNAME
        ) { success, error ->

            isLoading = false

            if (!success) {
                errorMessage = mapFirebaseError(error)
                return@signUp
            }

            currentUser.value = repo.getCurrentUserModel()
            _isLoggedIn.value = true

            viewModelScope.launch {
                prefs.setRememberMe(rememberMe)
            }

            onSuccess()
        }
    }

    /* -------------------------
       LOGOUT
     ------------------------- */
    fun logout(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            messageRepository?.clearAllData()
            repo.logout()

            email = ""
            password = ""
            firstName = ""
            lastName = ""
            phone = ""
            username = ""
            errorMessage = null

            currentUser.value = null
            _isLoggedIn.value = false
            prefs.setRememberMe(false)

            onComplete?.invoke()
        }
    }

    /* -------------------------
       VALIDATION
     ------------------------- */
    private fun validateLoginFields(): Boolean {
        errorMessage = null
        if (email.isBlank()) return setError("Please enter your email")
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
            return setError("Please enter a valid email")
        if (password.isBlank()) return setError("Please enter your password")
        return true
    }

    private fun validateSignUpFields(): Boolean {
        errorMessage = null
        if (firstName.isBlank()) return setError("Please enter your first name")
        if (lastName.isBlank()) return setError("Please enter your last name")
        if (username.isBlank()) return setError("Please enter a username") // ✅
        if (email.isBlank()) return setError("Please enter your email")
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
            return setError("Please enter a valid email")
        if (password.length < 6)
            return setError("Password must be at least 6 characters")
        return true
    }

    private fun setError(msg: String): Boolean {
        errorMessage = msg
        return false
    }

    fun clearError() {
        errorMessage = null
    }

    private fun mapFirebaseError(error: String?): String {
        return when {
            error == null -> "An unknown error occurred"
            error.contains("email", true) -> "Invalid email"
            error.contains("password", true) -> "Invalid password"
            error.contains("network", true) -> "Network error"
            error.contains("already", true) -> "Account already exists"
            else -> error
        }
    }

    fun getCurrentUserId(): String? = currentUser.value?.uid

    /* -------------------------
       FACTORY
     ------------------------- */
    class Factory(
        private val repo: IAuthRepository,
        private val prefs: IUserPreferences,
        private val messageRepository: MessageRepository? = null
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(repo, prefs, messageRepository) as T
        }
    }
}
