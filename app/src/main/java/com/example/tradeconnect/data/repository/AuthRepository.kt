package com.example.tradeconnect.data.repository

import com.example.tradeconnect.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

open class AuthRepository(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : IAuthRepository {

    companion object {
        private const val USERS_COLLECTION = "users"
    }

//    override fun signUp(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
//        firebaseAuth.createUserWithEmailAndPassword(email, password)
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) onResult(true, null)
//                else onResult(false, task.exception?.message)
//            }
//    }

    override fun signUp(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Create user document in Firestore immediately after signup
                    val user = firebaseAuth.currentUser
                    if (user != null) {
                        val username = "$firstName $lastName".trim()
                        val newUser = User(
                            uid = user.uid,
                            email = email,
                            username = username,
                            mobile = phone,
                            profileImageUrl = ""
                        )

                        firestore.collection(USERS_COLLECTION)
                            .document(user.uid)
                            .set(newUser)
                            .addOnSuccessListener {
                                onResult(true, null)
                            }
                            .addOnFailureListener { e ->
                                onResult(false, e.message)
                            }
                    } else {
                        onResult(true, null) // Auth succeeded but no user object
                    }
                } else {
                    onResult(false, task.exception?.message)
                }
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
