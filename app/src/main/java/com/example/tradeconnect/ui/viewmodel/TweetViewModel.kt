package com.example.tradeconnect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tradeconnect.data.repository.TweetRepository
import com.example.tradeconnect.model.Tweet
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TweetViewModel(
    private val repo: TweetRepository = TweetRepository()
) : ViewModel() {

    private val _tweets = MutableStateFlow<List<Tweet>>(emptyList())
    val tweets: StateFlow<List<Tweet>> = _tweets

    init {
        loadTweets()
    }

    private fun loadTweets() {
        viewModelScope.launch {
            repo.getTweets().collect { list ->
                _tweets.value = list
            }
        }
    }
    fun addTweet(content: String) {
        val tweet = hashMapOf(
            "userId" to "123", // TODO: prendre le vrai user
            "username" to "souhaila",
            "content" to content,
            "timestamp" to System.currentTimeMillis()
        )

        FirebaseFirestore.getInstance()
            .collection("tweets")
            .add(tweet)
    }
}
