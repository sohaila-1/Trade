package com.example.tradeconnect.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.tradeconnect.model.Tweet
import com.example.tradeconnect.repository.TweetRepository
import java.util.UUID

class TweetViewModel : ViewModel() {

    private val repository = TweetRepository()

    val tweets = mutableStateOf<List<Tweet>>(emptyList())
    val currentUserId = mutableStateOf("")
    val currentUsername = mutableStateOf("")

    init {
        loadTweets()
    }

    fun loadTweets() {
        repository.getTweets {
            tweets.value = it
        }
    }

    fun createTweet(content: String) {
        val tweet = Tweet(
            id = UUID.randomUUID().toString(),
            userId = currentUserId.value,
            username = currentUsername.value,
            content = content
        )

        repository.postTweet(tweet) {
            loadTweets()
        }
    }

    // ---- OFFICIEL: ta fonction d'édition existante ----
    fun editTweet(id: String, newContent: String) {
        repository.updateTweet(id, newContent) {
            loadTweets()
        }
    }

    // ---- AJOUT : alias pour éviter les crashs ----
    fun updateTweet(id: String, newContent: String) {
        editTweet(id, newContent)
    }

    fun deleteTweet(id: String) {
        repository.deleteTweet(id) {
            loadTweets()
        }
    }

    fun getTweetById(id: String): Tweet? {
        return tweets.value.firstOrNull { it.id == id }
    }
}
