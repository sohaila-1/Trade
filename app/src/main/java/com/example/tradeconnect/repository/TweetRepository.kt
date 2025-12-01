package com.example.tradeconnect.repository

import com.example.tradeconnect.model.Tweet
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class TweetRepository {

    private val db = FirebaseFirestore.getInstance()

    fun getTweets(onResult: (List<Tweet>) -> Unit) {
        db.collection("tweets")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    val tweets = snapshot.toObjects(Tweet::class.java)
                    onResult(tweets)
                }
            }
    }

    fun postTweet(tweet: Tweet, onComplete: () -> Unit) {
        db.collection("tweets")
            .add(tweet)
            .addOnSuccessListener { onComplete() }
    }
}
