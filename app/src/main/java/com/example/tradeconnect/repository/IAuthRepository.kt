package com.example.tradeconnect.repository

import com.google.firebase.auth.FirebaseUser

interface IAuthRepository {
    fun signUp(email: String, password: String, onResult: (Boolean, String?) -> Unit)
    fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit)
    fun logout()
    fun getCurrentUser(): FirebaseUser?
}