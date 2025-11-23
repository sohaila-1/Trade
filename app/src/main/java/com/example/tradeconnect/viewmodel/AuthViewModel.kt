package com.example.tradeconnect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tradeconnect.data.datastore.UserPreferences
import com.example.tradeconnect.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import com.example.tradeconnect.data.datastore.IUserPreferences
import com.example.tradeconnect.data.repository.IAuthRepository

class AuthViewModel(
    private val repo: IAuthRepository,
    private val prefs: IUserPreferences
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

    // remember-me flag (UI state)
    var rememberMe by mutableStateOf(true)
        private set

    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn = _isLoggedIn // observe in UI

    init {
        viewModelScope.launch {
            prefs.rememberMeFlow.collectLatest { stored ->
                val current = repo.getCurrentUser()
                _isLoggedIn.value = if (stored && current != null) true else false
            }
        }
    }

    fun updateRememberMe(value: Boolean) {
        rememberMe = value
        viewModelScope.launch {
            prefs.setRememberMe(value)
        }
    }

    fun login(onSuccess: () -> Unit) {
        isLoading = true
        repo.login(email, password) { success, error ->
            isLoading = false
            if (success) {
                viewModelScope.launch {
                    // persist remember me if checked
                    prefs.setRememberMe(rememberMe)
                    // if we kept user logged in, mark loggedIn true
                    _isLoggedIn.value = true
                    onSuccess()
                }
            } else {
                errorMessage = error
            }
        }
    }

    fun signUp(onSuccess: () -> Unit) {
        isLoading = true
        repo.signUp(email, password) { success, error ->
            isLoading = false
            if (success) {
                viewModelScope.launch {
                    // default keep logged in after sign up
                    // if user didn't explicitly set rememberMe, keep it true by default
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
                errorMessage = error
            }
        }
    }

    fun logout(onComplete: (() -> Unit)? = null) {
        repo.logout()
        viewModelScope.launch {
//            prefs.setRememberMe(false)
//            rememberMe = false
            _isLoggedIn.value = false
            onComplete?.invoke()
        }
    }

    class Factory(
        private val repo: IAuthRepository,
        private val prefs: IUserPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(repo, prefs) as T
        }
    }
}
