package com.example.tradeconnect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.*
import com.example.tradeconnect.model.AppUser
import com.example.tradeconnect.repository.IAuthRepository
import com.example.tradeconnect.data.datastore.IUserPreferences
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repo: IAuthRepository,
    private val prefs: IUserPreferences
) : ViewModel() {

    // -------------------------
    // 🔹 Champs de formulaire
    // -------------------------
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var errorMessage by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(false)

    // --- SIGN UP FIELDS ---
    var firstName by mutableStateOf("")
    var lastName by mutableStateOf("")
    var phone by mutableStateOf("")

    // Firebase auth instance
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // -------------------------
    // 🔹 Remember Me
    // -------------------------
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


    // -------------------------
    // INIT → vérifie si user déjà loggé
    // -------------------------
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

    // -------------------------
    // LOGIN
    // -------------------------
    fun login(onSuccess: () -> Unit) {
        isLoading = true

        repo.login(email, password) { success, error ->
            isLoading = false

            if (!success) {
                errorMessage = error
                return@login
            }

            val user = repo.getCurrentUserModel()
            currentUser.value = user
            _isLoggedIn.value = true

            viewModelScope.launch { prefs.setRememberMe(rememberMe) }

            onSuccess()
        }
    }

    // -------------------------
    // SIGN UP
    // -------------------------
    fun signUp(onSuccess: () -> Unit) {
        isLoading = true

        repo.signUp(email, password) { success, error ->
            isLoading = false

            if (!success) {
                errorMessage = error
                return@signUp
            }

            val user = repo.getCurrentUserModel()
            currentUser.value = user
            _isLoggedIn.value = true

            viewModelScope.launch { prefs.setRememberMe(rememberMe) }

            onSuccess()
        }
    }

    // -------------------------
    // LOGOUT
    // -------------------------
    fun logout() {
        repo.logout()
        currentUser.value = null
        _isLoggedIn.value = false

        viewModelScope.launch { prefs.setRememberMe(false) }
    }


    // -------------------------
    // UTILITY → obtenir UID utilisateur Firebase
    // -------------------------
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }


    // -------------------------
    // FACTORY
    // -------------------------
    class Factory(
        private val repo: IAuthRepository,
        private val prefs: IUserPreferences
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(repo, prefs) as T
        }
    }
}
