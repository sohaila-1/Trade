package com.example.tradeconnect.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FollowRepository {

    private val db = FirebaseFirestore.getInstance()
    private val followRef = db.collection("follows")

    // ----------------------------------------------
    // 🔥 Récupérer la liste des UID suivis par currentUid
    // ----------------------------------------------
    fun getFollowing(currentUid: String, onResult: (List<String>) -> Unit) {
        followRef
            .document(currentUid)
            .get()
            .addOnSuccessListener { doc ->
                val list = doc.get("following") as? List<String> ?: emptyList()
                onResult(list)
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    // ----------------------------------------------
    // 🔥 FOLLOW (suspend)
    // ----------------------------------------------
    suspend fun followUser(currentUid: String, targetUid: String) {
        followRef.document(currentUid)
            .update("following", FieldValue.arrayUnion(targetUid))
            .addOnFailureListener {
                // document doesn't exist → create it
                followRef.document(currentUid)
                    .set(mapOf("following" to listOf(targetUid)))
            }
            .await()
    }

    // ----------------------------------------------
    // 🔥 UNFOLLOW (suspend)
    // ----------------------------------------------
    suspend fun unfollowUser(currentUid: String, targetUid: String) {
        followRef.document(currentUid)
            .update("following", FieldValue.arrayRemove(targetUid))
            .await()
    }
}
