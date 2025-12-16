package com.example.tradeconnect.repository

import com.example.tradeconnect.data.model.User
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository {

    private val db = FirebaseFirestore.getInstance()
    private val usersRef = db.collection("users")

    fun getAllUsers(onResult: (List<User>) -> Unit) {
        usersRef.get().addOnSuccessListener { snap ->
            val list = snap.toObjects(User::class.java)
            onResult(list)
        }
    }
}
