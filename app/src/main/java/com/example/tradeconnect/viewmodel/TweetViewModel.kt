package com.example.tradeconnect.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tradeconnect.data.model.User
import com.example.tradeconnect.model.Tweet
import com.example.tradeconnect.repository.FollowRepository
import com.example.tradeconnect.repository.TweetRepository
import com.example.tradeconnect.repository.UserRepository
import kotlinx.coroutines.launch

import java.util.UUID

class TweetViewModel(
    private val tweetRepo: TweetRepository,
    private val followRepo: FollowRepository,
    private val authVM: AuthViewModel,
    private val userRepo: UserRepository
) : ViewModel() {

    // ----------------------------------------------
    // 🌟 STATES
    // ----------------------------------------------
    val myTweets = mutableStateOf<List<Tweet>>(emptyList())
    val followingList = mutableStateOf<List<String>>(emptyList())
    val followingTweets = mutableStateOf<List<Tweet>>(emptyList())
    val allUsers = mutableStateOf<List<User>>(emptyList())

    // ----------------------------------------------
    // 🔥 CHARGER MES TWEETS
    // ----------------------------------------------
    fun loadMyTweets() {
        val user = authVM.currentUser.value ?: return
        tweetRepo.getTweetsByUser(user.uid) { list ->
            myTweets.value = list
        }
    }

    // ----------------------------------------------
    // 🔥 CHARGER LES PERSONNES QUE JE SUIS
    // ----------------------------------------------
    fun loadFollowingUsers() {
        val user = authVM.currentUser.value ?: return

        followRepo.getFollowing(user.uid) { list ->
            followingList.value = list
            loadFollowingTweets(list)
        }
    }

    private fun loadFollowingTweets(ids: List<String>) {
        if (ids.isEmpty()) {
            followingTweets.value = emptyList()
            return
        }

        tweetRepo.getTweetsFromUsers(ids) { tweets ->
            followingTweets.value = tweets
        }
    }

    // ----------------------------------------------
    // 🔥 CRÉER UN TWEET
    // ----------------------------------------------
    fun createTweet(content: String) {
        val user = authVM.currentUser.value ?: return

        val tweet = Tweet(
            id = UUID.randomUUID().toString(),
            userId = user.uid,
            username = user.username,
            content = content,
            timestamp = System.currentTimeMillis()
        )

        tweetRepo.postTweet(tweet) {
            loadMyTweets()
        }
    }

    // ----------------------------------------------
    // 🔥 ÉDITER & SUPPRIMER
    // ----------------------------------------------
    fun editTweet(id: String, newContent: String) {
        tweetRepo.updateTweet(id, newContent) {
            loadMyTweets()
        }
    }

    fun deleteTweet(id: String) {
        tweetRepo.deleteTweet(id) {
            loadMyTweets()
        }
    }

    fun getTweetById(id: String): Tweet? =
        myTweets.value.firstOrNull { it.id == id }

    // ----------------------------------------------
    // 🔥 RÉCUPÉRER TOUS LES USERS (onglet abonnements)
    // ----------------------------------------------
    fun loadAllUsers() {
        userRepo.getAllUsers { list ->
            allUsers.value = list
        }
    }

    // ----------------------------------------------
    // 🔥 FOLLOW / UNFOLLOW
    // ----------------------------------------------

    fun followUser(targetUid: String) {
        val currentUid = authVM.getCurrentUserId() ?: return

        viewModelScope.launch {
            followRepo.followUser(currentUid, targetUid)
            loadFollowingUsers()
        }
    }


    fun unfollowUser(targetUid: String) {
        val currentUid = authVM.getCurrentUserId() ?: return

        viewModelScope.launch {
            followRepo.unfollowUser(currentUid, targetUid)
            loadFollowingUsers()
        }
    }






    // ----------------------------------------------
    // 🔥 FACTORY POUR NAVHOST
    // ----------------------------------------------
    class Factory(
        private val tweetRepo: TweetRepository,
        private val followRepo: FollowRepository,
        private val authVM: AuthViewModel,
        private val userRepo: UserRepository
    ) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TweetViewModel(tweetRepo, followRepo, authVM, userRepo) as T
        }
    }
}
