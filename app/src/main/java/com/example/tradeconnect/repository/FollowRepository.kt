package com.example.tradeconnect.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FollowRepository {

    private val db = FirebaseFirestore.getInstance()
    private val followRef = db.collection("follows")

    // ----------------------------------------------
    // 🔥 Récupérer les UID suivis par l'utilisateur
    // ----------------------------------------------
    fun getFollowing(currentUid: String, onResult: (List<String>) -> Unit) {
        followRef.document(currentUid)
            .get()
            .addOnSuccessListener { doc ->
                val list = doc.get("following") as? List<String> ?: emptyList()
                onResult(list)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    // ----------------------------------------------
    // 🔥 FOLLOW (corrigé)
    // ----------------------------------------------
    fun followUser(currentUid: String, targetUid: String) {
        followRef.document(currentUid)
            .update("following", FieldValue.arrayUnion(targetUid))
            .addOnFailureListener {
                // document n'existe pas → on le crée
                followRef.document(currentUid)
                    .set(mapOf("following" to listOf(targetUid)))
            }
    }

    // ----------------------------------------------
    // 🔥 UNFOLLOW
    // ----------------------------------------------
    fun unfollowUser(currentUid: String, targetUid: String) {
        followRef.document(currentUid)
            .update("following", FieldValue.arrayRemove(targetUid))
    }
}
