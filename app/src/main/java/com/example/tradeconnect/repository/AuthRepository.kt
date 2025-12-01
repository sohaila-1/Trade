package com.example.tradeconnect.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

open class AuthRepository(private val firebaseAuth: FirebaseAuth) : IAuthRepository {

    override fun signUp(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) onResult(true, null)
                else onResult(false, task.exception?.message)
            }
    }

    override fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) onResult(true, null)
                else onResult(false, task.exception?.message)
            }
    }

    override fun logout() = firebaseAuth.signOut()

    override fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser
}
