package com.example.tradeconnect.repository

import com.example.tradeconnect.model.AppUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthRepository(
    private val auth: FirebaseAuth
) : IAuthRepository {

    override fun signUp(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener {
                onResult(false, it.message)
            }
    }

    override fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener {
                onResult(false, it.message)
            }
    }

    override fun logout() {
        auth.signOut()
    }

    override fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    // 🔥🔥🔥 LE PLUS IMPORTANT : retourne un AppUser
    override fun getCurrentUserModel(): AppUser? {
        val user = auth.currentUser ?: return null

        return AppUser(
            uid = user.uid,
            username = user.email?.substringBefore("@") ?: "unknown"
        )
    }
}
