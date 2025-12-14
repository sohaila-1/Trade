package com.example.tradeconnect.repository

import android.util.Log
import com.example.tradeconnect.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository {

    private val db = FirebaseFirestore.getInstance()
    private val usersRef = db.collection("users")
    private val auth = FirebaseAuth.getInstance()

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun getAllUsers(onResult: (List<User>) -> Unit) {
        usersRef.get()
            .addOnSuccessListener { snapshot ->
                val users = snapshot.toObjects(User::class.java)
                onResult(users)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    // ---------------------------
    // 🔹 SEARCH USERS (username)
    // ---------------------------
    fun searchUsers(
        query: String,
        onResult: (List<User>) -> Unit
    ) {
        if (query.isBlank()) {
            onResult(emptyList())
            return
        }

        usersRef
            .orderBy("username")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .get()
            .addOnSuccessListener { snapshot ->
                val users = snapshot.toObjects(User::class.java)
                onResult(users)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    // ---------------------------
// 🔹 USERNAME DU USER COURANT
// ---------------------------
    fun getCurrentUsername(onResult: (String) -> Unit) {
        val userId = getCurrentUserId() ?: return onResult("?")

        usersRef.document(userId)
            .get()
            .addOnSuccessListener { doc ->
                Log.d("USER_REPO", "User doc = ${doc.data}")
                val username = doc.getString("username")
                onResult(username ?: "UNKNOWN")
            }
            .addOnFailureListener {
                onResult("ERROR")
            }
    }


}
