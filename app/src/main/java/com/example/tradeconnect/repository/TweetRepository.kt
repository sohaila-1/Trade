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
    fun toggleSave(tweetId: String, uid: String) {
        val ref = tweetsRef.document(tweetId)

        firestore.runTransaction { tx ->
            val snap = tx.get(ref)
            val saves = snap.get("saves") as? List<String> ?: emptyList()

            val updatedSaves = if (uid in saves) {
                saves - uid
            } else {
                saves + uid
            }

            tx.update(ref, "saves", updatedSaves)
        }
    }



}
