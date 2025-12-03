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
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    val tweets = snapshot.toObjects(Tweet::class.java)
                    onResult(tweets)
                }
            }
    }

    fun postTweet(tweet: Tweet, onComplete: () -> Unit) {
        tweetsRef
            .add(tweet)
            .addOnSuccessListener { onComplete() }
    }

    fun updateTweet(tweet: Tweet, onComplete: () -> Unit) {
        tweetsRef
            .document(tweet.id)
            .set(tweet)
            .addOnSuccessListener { onComplete() }
    }

    fun deleteTweet(tweetId: String, onSuccess: () -> Unit) {
        tweetsRef
            .document(tweetId)
            .delete()
            .addOnSuccessListener { onSuccess() }
    }
}

