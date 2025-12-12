package com.example.tradeconnect.repository

import com.example.tradeconnect.model.Tweet
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

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

}
