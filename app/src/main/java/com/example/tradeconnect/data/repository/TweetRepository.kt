package com.example.tradeconnect.data.repository

import com.example.tradeconnect.model.Tweet
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.Flow
import com.google.firebase.firestore.Query


class TweetRepository {

    private val db = FirebaseFirestore.getInstance()

    fun getTweets(): Flow<List<Tweet>> = callbackFlow {

        val subscription = db.collection("tweets")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val tweets = snapshot?.documents?.map { doc ->
                    Tweet(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        username = doc.getString("username") ?: "",
                        content = doc.getString("content") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0
                    )
                } ?: emptyList()

                trySend(tweets)
            }

        awaitClose { subscription.remove() }
    }
}
