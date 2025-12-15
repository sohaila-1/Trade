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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID



class TweetViewModel(
    private val tweetRepo: TweetRepository,
    private val followRepo: FollowRepository,
    val authVM: AuthViewModel,
    private val userRepo: UserRepository
) : ViewModel() {

    val myTweets = mutableStateOf<List<Tweet>>(emptyList())
    val followingList = mutableStateOf<List<String>>(emptyList())
    val followingTweets = mutableStateOf<List<Tweet>>(emptyList())

    val allTweets = mutableStateOf<List<Tweet>>(emptyList())
    val allUsers = mutableStateOf<List<User>>(emptyList())

    // -----------------------------------------------------
    // 🔥 CHARGER MES TWEETS
    // -----------------------------------------------------
    fun loadMyTweets() {
        val user = authVM.currentUser.value ?: return
        tweetRepo.getTweetsByUser(user.uid) { list ->
            myTweets.value = list
        }
    }

    // -----------------------------------------------------
    // 🔥 CHARGER TOUS LES TWEETS
    // -----------------------------------------------------
    fun loadAllTweets() {
        tweetRepo.getAllTweets { list ->
            allTweets.value = list
        }
    }

    // -----------------------------------------------------
    // 🔥 CHARGER LES PERSONNES QUE JE SUIS
    // -----------------------------------------------------
    fun loadFollowingUsers() {
        val user = authVM.currentUser.value ?: return
        viewModelScope.launch {
            val userList = followRepo.getFollowing(user.uid)
            val idList = userList.map { it.uid }
            followingList.value = idList
            loadFollowingTweets(idList)
        }
    }

    // -----------------------------------------------------
    // 🔥 VERSION UNIQUE ET CORRECTE loadFollowingTweets
    // -----------------------------------------------------
    private fun loadFollowingTweets(ids: List<String>) {
        if (ids.isEmpty()) {
            followingTweets.value = emptyList()
            return
        }

        tweetRepo.getTweetsFromUsers(ids) { tweets ->

            // ⭐ On prend SEULEMENT le dernier tweet de chaque utilisateur suivi
            val lastTweets = tweets
                .groupBy { it.userId }
                .map { (_, userTweets) ->
                    userTweets.maxByOrNull { it.timestamp }!!
                }
                .sortedByDescending { it.timestamp }

            followingTweets.value = lastTweets
        }
    }

    // -----------------------------------------------------
    // 🔥 CRÉER UN TWEET
    // -----------------------------------------------------
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
            loadAllTweets()
        }
    }

    // -----------------------------------------------------
    // 🔥 ÉDITER / SUPPRIMER
    // -----------------------------------------------------
    fun editTweet(id: String, newContent: String) {
        tweetRepo.updateTweet(id, newContent) {
            loadMyTweets()
            loadAllTweets()
        }
    }

    fun deleteTweet(id: String) {
        tweetRepo.deleteTweet(id) {
            loadMyTweets()
            loadAllTweets()
        }
    }

    fun getTweetById(id: String): Tweet? =
        myTweets.value.firstOrNull { it.id == id }

    // -----------------------------------------------------
    // 🔥 CHARGER TOUS LES USERS (onglet abonnements)
    // -----------------------------------------------------
    fun loadAllUsers() {
        val currentUid = authVM.getCurrentUserId() ?: return

        userRepo.getAllUsers { users ->
            allUsers.value = users.filter { it.uid != currentUid }
        }
    }

    // -----------------------------------------------------
    // 🔥 FOLLOW / UNFOLLOW
    // -----------------------------------------------------
    fun followUser(targetUid: String) {
        viewModelScope.launch {
            followRepo.followUser(targetUid)
            loadFollowingUsers()
            loadAllUsers()
            loadAllTweets()
        }
    }

    fun unfollowUser(targetUid: String) {
        viewModelScope.launch {
            followRepo.unfollowUser(targetUid)
            loadFollowingUsers()
            loadAllUsers()
            loadAllTweets()
        }
    }


    // -----------------------------------------------------
    fun getLastTweetOfUser(userId: String): String =
        allTweets.value
            .filter { it.userId == userId }
            .maxByOrNull { it.timestamp }
            ?.content ?: ""

    // -----------------------------------------------------
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

    fun toggleLike(tweetId: String) {
        val uid = authVM.getCurrentUserId() ?: return
        tweetRepo.toggleLike(tweetId, uid)
    }

    fun toggleSave(tweetId: String) {
        val userId = authVM.getCurrentUserId() ?: return

        tweetRepo.toggleSave(tweetId, userId)

        loadSavedTweets()
    }

    val savedTweets = mutableStateOf<List<Tweet>>(emptyList())

    fun loadSavedTweets() {
        val userId = authVM.getCurrentUserId() ?: return

        tweetRepo.getSavedTweets(userId) { list ->
            savedTweets.value = list
        }
    }




}
