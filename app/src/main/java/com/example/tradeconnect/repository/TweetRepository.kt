package com.example.tradeconnect.repository

import com.example.tradeconnect.model.Tweet
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class TweetRepository {

    private val db = FirebaseFirestore.getInstance()
    private val tweetsRef = db.collection("tweets")

    fun getTweets(onResult: (List<Tweet>) -> Unit) {
        tweetsRef
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->

                if (e != null) {
                    println("Firestore error : ${e.message}")
                    onResult(emptyList())
                    return@addSnapshotListener
                }

                val tweets = snapshot?.toObjects(Tweet::class.java) ?: emptyList()
                onResult(tweets)
            }
    }

    fun postTweet(tweet: Tweet, onComplete: () -> Unit = {}) {
        tweetsRef
            .document(tweet.id)
            .set(tweet)
            .addOnSuccessListener { onComplete() }
    }


    fun updateTweet(tweetId: String, newContent: String, onComplete: () -> Unit) {
        if (tweetId.isBlank()) {
            println("❌ updateTweet ERROR: tweetId is empty")
            return
        }

        tweetsRef.document(tweetId)
            .update("content", newContent)
            .addOnSuccessListener { onComplete() }
            .addOnFailureListener { e ->
                println("❌ FIRESTORE UPDATE ERROR: ${e.message}")
            }
    }

    fun deleteTweet(tweetId: String, onSuccess: () -> Unit = {}) {
        tweetsRef.document(tweetId)
            .delete()
            .addOnSuccessListener { onSuccess() }
    }
}
