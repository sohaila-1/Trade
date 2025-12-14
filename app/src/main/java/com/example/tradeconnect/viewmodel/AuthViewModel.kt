package com.example.tradeconnect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import com.example.tradeconnect.data.datastore.IUserPreferences
import com.example.tradeconnect.data.repository.IAuthRepository
import com.example.tradeconnect.data.repository.MessageRepository
import com.example.tradeconnect.model.AppUser
import com.google.firebase.auth.FirebaseAuth

class AuthViewModel(
    private val repo: IAuthRepository,
    private val prefs: IUserPreferences,
    private val messageRepository: MessageRepository? = null
) : ViewModel() {

    // form fields
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var firstName by mutableStateOf("")
    var lastName by mutableStateOf("")
    var phone by mutableStateOf("")

    // UI state
    var errorMessage by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(false)

    // Firebase auth instance
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // remember-me flag (UI state)
    var rememberMe by mutableStateOf(true)
        private set

    fun updateRememberMe(value: Boolean) {
        rememberMe = value
        viewModelScope.launch { prefs.setRememberMe(value) }
    }

    // -------------------------
    // 🔹 Utilisateur connecté
    // -------------------------
    var currentUser = mutableStateOf<AppUser?>(null)
        private set

    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn = _isLoggedIn

    init {
        viewModelScope.launch {
            prefs.rememberMeFlow.collectLatest { shouldRemember ->

                val user = repo.getCurrentUserModel()  // AppUser?

                if (shouldRemember && user != null) {
                    currentUser.value = user
                    _isLoggedIn.value = true
                } else {
                    _isLoggedIn.value = false
                }
            }
        }
    }

    /**
     * Clear any previous error message
     */


    /**
     * Validate login fields
     */
    private fun validateLoginFields(): Boolean {
        // Clear previous error
        errorMessage = null

        // Check email
        if (email.isBlank()) {
            errorMessage = "Please enter your email"
            return false
        }

        if (!isValidEmail(email)) {
            errorMessage = "Please enter a valid email address"
            return false
        }

        // Check password
        if (password.isBlank()) {
            errorMessage = "Please enter your password"
            return false
        }

        return true
    }

    /**
     * Validate signup fields
     */
    private fun validateSignUpFields(): Boolean {
        // Clear previous error
        errorMessage = null

        // Check first name
        if (firstName.isBlank()) {
            errorMessage = "Please enter your first name"
            return false
        }

        // Check last name
        if (lastName.isBlank()) {
            errorMessage = "Please enter your last name"
            return false
        }

        // Check email
        if (email.isBlank()) {
            errorMessage = "Please enter your email"
            return false
        }

        if (!isValidEmail(email)) {
            errorMessage = "Please enter a valid email address"
            return false
        }

        // Check phone (optional but if provided, validate format)
        // You can make this required by uncommenting:
        // if (phone.isBlank()) {
        //     errorMessage = "Please enter your phone number"
        //     return false
        // }

        // Check password
        if (password.isBlank()) {
            errorMessage = "Please enter a password"
            return false
        }

        if (password.length < 6) {
            errorMessage = "Password must be at least 6 characters"
            return false
        }

        return true
    }

    /**
     * Simple email validation
     */
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun login(onSuccess: () -> Unit) {
        // Validate before attempting login
        if (!validateLoginFields()) {
            return
        }

        isLoading = true
        errorMessage = null
        repo.login(email, password) { success, error ->
            isLoading = false
            if (success) {
                //maybe put this 2 lines inside the viewmodelscope
                val user = repo.getCurrentUserModel()
                currentUser.value = user
                viewModelScope.launch {
                    // persist remember me if checked
                    prefs.setRememberMe(rememberMe)

                    // if we kept user logged in, mark loggedIn true
                    _isLoggedIn.value = true
                    onSuccess()
                }
            } else {
                errorMessage = mapFirebaseError(error)
            }
        }
    }

    fun signUp(onSuccess: () -> Unit) {
        // Validate before attempting signup
        if (!validateSignUpFields()) {
            return
        }

        isLoading = true
        errorMessage = null

        repo.signUp(
            email.trim(),
            password,
            firstName.trim(),
            lastName.trim(),
            phone.trim()
        ) { success, error ->
            isLoading = false
            if (success) {
                val user = repo.getCurrentUserModel()
                currentUser.value = user
                viewModelScope.launch {
                    if (!rememberMe) {
                        rememberMe = true
                        prefs.setRememberMe(true)
                    } else {
                        prefs.setRememberMe(rememberMe)
                    }
                    _isLoggedIn.value = true
                    onSuccess()
                }
            } else {
                // Map Firebase errors to user-friendly messages
                errorMessage = mapFirebaseError(error)
            }
        }
    }

    /**
     * Map Firebase error messages to user-friendly messages
     */
    private fun mapFirebaseError(error: String?): String {
        return when {
            error == null -> "An unknown error occurred"
            error.contains("email address is badly formatted", ignoreCase = true) ->
                "Please enter a valid email address"
            error.contains("password is invalid", ignoreCase = true) ||
                    error.contains("wrong password", ignoreCase = true) ->
                "Incorrect password"
            error.contains("no user record", ignoreCase = true) ||
                    error.contains("user not found", ignoreCase = true) ->
                "No account found with this email"
            error.contains("email address is already in use", ignoreCase = true) ->
                "An account with this email already exists"
            error.contains("weak password", ignoreCase = true) ->
                "Password is too weak. Use at least 6 characters"
            error.contains("network error", ignoreCase = true) ||
                    error.contains("network", ignoreCase = true) ->
                "Network error. Please check your connection"
            error.contains("too many requests", ignoreCase = true) ||
                    error.contains("blocked", ignoreCase = true) ->
                "Too many attempts. Please try again later"
            error.contains("empty or null", ignoreCase = true) ->
                "Please fill in all required fields"
            else -> error
        }
    }

    fun logout(onComplete: (() -> Unit)? = null) {
        currentUser.value = null
        viewModelScope.launch {
            // Clear all local cached data
            messageRepository?.clearAllData()

            // Sign out from Firebase
            repo.logout()

            // Clear form fields for security
            email = ""
            password = ""
            firstName = ""
            lastName = ""
            phone = ""
            errorMessage = null

            // Update login state
            _isLoggedIn.value = false

            onComplete?.invoke()
        }
    }

    fun getCurrentUserId(): String? {
        return currentUser.value?.uid
    }

    class Factory(
        private val repo: IAuthRepository,
        private val prefs: IUserPreferences,
        private val messageRepository: MessageRepository? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(repo, prefs, messageRepository) as T
        }
    }

    fun clearError() {
        errorMessage = null
    }

}
