package com.example.tradeconnect.repository

import IAuthRepository
import com.example.tradeconnect.model.AppUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : IAuthRepository {

    override fun login(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                onResult(false, e.message)
            }
    }

    override fun signUp(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener

                val user = hashMapOf(
                    "uid" to uid,
                    "email" to email,
                    "firstName" to firstName,
                    "lastName" to lastName,
                    "phone" to phone
                )

                firestore.collection("users")
                    .document(uid)
                    .set(user)
                    .addOnSuccessListener { onResult(true, null) }
                    .addOnFailureListener { e -> onResult(false, e.message) }
            }
            .addOnFailureListener { e ->
                onResult(false, e.message)
            }
    }

    override fun logout() {
        auth.signOut()
    }

    override fun getCurrentUser() = auth.currentUser

    override fun getCurrentUserModel(): AppUser? {
        val user = auth.currentUser ?: return null
        return AppUser(
            uid = user.uid,
            email = user.email ?: ""
        )
    }



}
