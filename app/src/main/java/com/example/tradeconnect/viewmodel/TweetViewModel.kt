package com.example.tradeconnect.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.tradeconnect.model.Tweet
import com.example.tradeconnect.repository.TweetRepository
import java.util.UUID

class TweetViewModel : ViewModel() {

    private val repository = TweetRepository()

    var tweets = mutableStateOf(listOf<Tweet>())
        private set

    init {
        loadTweets()
    }

    fun loadTweets() {
        repository.getTweets { list ->
            tweets.value = list
        }
    }

    fun createTweet(content: String, username: String, userId: String) {
        val tweet = Tweet(
            id = UUID.randomUUID().toString(),
            userId = userId,
            username = username,
            content = content,
            timestamp = System.currentTimeMillis()
        )

        repository.postTweet(tweet) {
            loadTweets()
        }
    }

    fun editTweet(tweetId: String, newContent: String) {   // ✔️ NON NULL
        val original = tweets.value.firstOrNull { it.id == tweetId } ?: return

        val updatedTweet = original.copy(content = newContent)

        repository.updateTweet(updatedTweet) {
            loadTweets()
        }
    }

    fun deleteTweet(tweetId: String) {     // ✔️ NON NULL
        repository.deleteTweet(tweetId) {
            loadTweets()                   // recharge la liste après suppression
        }
    }


}
