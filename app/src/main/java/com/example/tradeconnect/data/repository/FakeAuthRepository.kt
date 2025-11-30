package com.example.tradeconnect.data.repository

import com.google.firebase.auth.FirebaseUser

// Fake repository for previews only
class FakeAuthRepository : IAuthRepository {
    override fun signUp(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        // Simulate successful signup for previews
        onResult(true, null)
    }
    override fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        onResult(true, null)
    }
    override fun logout() {}
    override fun getCurrentUser(): FirebaseUser? = null
}

