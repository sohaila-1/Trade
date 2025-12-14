package com.example.tradeconnect.repository

import com.example.tradeconnect.model.Tweet
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class TweetRepository(private val firestore: FirebaseFirestore) {

    private val tweetsRef = firestore.collection("tweets")

    fun getTweetsByUser(uid: String, onResult: (List<Tweet>) -> Unit) {
        tweetsRef.whereEqualTo("userId", uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                onResult(snap?.toObjects(Tweet::class.java) ?: emptyList())
            }
    }

    fun getTweetsFromUsers(uids: List<String>, onResult: (List<Tweet>) -> Unit) {
        tweetsRef.whereIn("userId", uids)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                onResult(snap?.toObjects(Tweet::class.java) ?: emptyList())
            }
    }

    fun postTweet(tweet: Tweet, onSuccess: () -> Unit) {
        tweetsRef.document(tweet.id).set(tweet).addOnSuccessListener { onSuccess() }
    }

    fun updateTweet(id: String, newContent: String, onSuccess: () -> Unit) {
        tweetsRef.document(id)
            .update("content", newContent)
            .addOnSuccessListener { onSuccess() }
    }

    fun deleteTweet(id: String, onSuccess: () -> Unit) {
        tweetsRef.document(id).delete().addOnSuccessListener { onSuccess() }
    }


    fun getAllTweets(onResult: (List<Tweet>) -> Unit) {
        tweetsRef
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents.mapNotNull { it.toObject(Tweet::class.java) }
                onResult(list)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    fun toggleLike(tweetId: String, uid: String) {
        val ref = tweetsRef.document(tweetId)

        firestore.runTransaction { tx ->
            val snap = tx.get(ref)
            val likes = snap.get("likes") as? List<String> ?: emptyList()

            val updatedLikes = if (uid in likes) {
                likes - uid   // unlike
            } else {
                likes + uid   // like
            }

            tx.update(ref, "likes", updatedLikes)
        }
    }
    fun toggleSave(tweetId: String, userId: String) {
        val ref = tweetsRef.document(tweetId)

        ref.get().addOnSuccessListener { doc ->
            val saves = doc.get("saves") as? MutableList<String> ?: mutableListOf()

            val update = if (saves.contains(userId))
                FieldValue.arrayRemove(userId)
            else
                FieldValue.arrayUnion(userId)

            ref.update("saves", update)
        }
    }

    fun getSavedTweets(userId: String, onResult: (List<Tweet>) -> Unit) {
        tweetsRef.whereArrayContains("saves", userId)
            .get()
            .addOnSuccessListener { snap ->
                onResult(snap.toObjects(Tweet::class.java))
            }
            .addOnFailureListener { onResult(emptyList()) }
    }
    suspend fun getBookmarkedTweets(userId: String): List<Tweet> {
        return firestore.collection("tweets")
            .whereArrayContains("savedBy", userId)
            .get()
            .await()
            .toObjects(Tweet::class.java)
    }



}
