package com.example.tradeconnect.repository

import IAuthRepository
import com.example.tradeconnect.model.AppUser
import com.google.firebase.auth.FirebaseUser

// Fake repository for previews / tests
class FakeAuthRepository : IAuthRepository {

    override fun signUp(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String,
        username: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        // Fake signup success (preview / tests)
        onResult(true, null)
    }

    override fun login(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        onResult(true, null)
    }

    override fun logout() {
        // nothing
    }

    override fun getCurrentUser(): FirebaseUser? = null

    override fun getCurrentUserModel(): AppUser? {
        // Fake user pour previews
        return AppUser(
            uid = "fake_uid",
            username = "Preview User",
            email = "preview@tradeconnect.dev",
            mobile = "",
            profileImageUrl = ""
        )
    }
}
