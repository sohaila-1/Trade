// app/src/main/java/com/example/tradeconnect/viewmodel/TweetViewModel.kt
package com.example.tradeconnect.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tradeconnect.data.model.User
import com.example.tradeconnect.model.Comment
import com.example.tradeconnect.model.Tweet
import com.example.tradeconnect.repository.FollowRepository
import com.example.tradeconnect.repository.TweetRepository
import com.example.tradeconnect.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    // ==================== COMMENTAIRES ====================

    // Tweet sélectionné pour les commentaires
    private val _selectedTweet = MutableStateFlow<Tweet?>(null)
    val selectedTweet: StateFlow<Tweet?> = _selectedTweet.asStateFlow()

    // Liste des commentaires
    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    // État de chargement des commentaires
    private val _isCommentsLoading = MutableStateFlow(false)
    val isCommentsLoading: StateFlow<Boolean> = _isCommentsLoading.asStateFlow()

    // État d'envoi d'un commentaire
    private val _isPostingComment = MutableStateFlow(false)
    val isPostingComment: StateFlow<Boolean> = _isPostingComment.asStateFlow()

    // Erreur
    private val _commentError = MutableStateFlow<String?>(null)
    val commentError: StateFlow<String?> = _commentError.asStateFlow()

    private var commentsJob: Job? = null

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
            timestamp = System.currentTimeMillis(),
            commentsCount = 0
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
            ?: allTweets.value.firstOrNull { it.id == id }

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
    // 🔥 LIKE / SAVE
    // -----------------------------------------------------
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

    // ==================== COMMENTAIRES ====================

    // Charger un tweet et ses commentaires
    fun loadTweetWithComments(tweetId: String) {
        viewModelScope.launch {
            _isCommentsLoading.value = true
            _commentError.value = null

            // Charger le tweet
            val tweet = tweetRepo.getTweetById(tweetId)
            _selectedTweet.value = tweet

            // Observer les commentaires en temps réel
            commentsJob?.cancel()
            commentsJob = viewModelScope.launch {
                tweetRepo.observeComments(tweetId).collect { commentsList ->
                    _comments.value = commentsList
                    _isCommentsLoading.value = false
                }
            }
        }
    }

    // Observer un tweet en temps réel (pour voir les updates)
    fun observeTweet(tweetId: String) {
        viewModelScope.launch {
            tweetRepo.observeTweet(tweetId).collect { tweet ->
                _selectedTweet.value = tweet
            }
        }
    }

    // Ajouter un commentaire
    fun addComment(tweetId: String, content: String) {
        val user = authVM.currentUser.value ?: return

        if (content.isBlank()) {
            _commentError.value = "Le commentaire ne peut pas être vide"
            return
        }

        viewModelScope.launch {
            _isPostingComment.value = true
            _commentError.value = null

            val comment = Comment(
                id = UUID.randomUUID().toString(),
                tweetId = tweetId,
                userId = user.uid,
                username = user.username,
                content = content.trim(),
                timestamp = System.currentTimeMillis()
            )

            val result = tweetRepo.addComment(comment)
            result.onFailure { e ->
                _commentError.value = e.message ?: "Erreur lors de l'envoi"
            }

            _isPostingComment.value = false

            // Recharger les tweets pour mettre à jour le compteur
            loadMyTweets()
            loadAllTweets()
        }
    }

    // Supprimer un commentaire
    fun deleteComment(tweetId: String, commentId: String) {
        viewModelScope.launch {
            val result = tweetRepo.deleteComment(tweetId, commentId)
            result.onFailure { e ->
                _commentError.value = e.message ?: "Erreur lors de la suppression"
            }

            // Recharger les tweets pour mettre à jour le compteur
            loadMyTweets()
            loadAllTweets()
        }
    }

    // Liker un commentaire
    fun toggleCommentLike(tweetId: String, commentId: String) {
        val userId = authVM.getCurrentUserId() ?: return
        tweetRepo.toggleCommentLike(tweetId, commentId, userId)
    }

    // Nettoyer quand on quitte l'écran des commentaires
    fun clearComments() {
        commentsJob?.cancel()
        _selectedTweet.value = null
        _comments.value = emptyList()
        _commentError.value = null
    }

    fun clearCommentError() {
        _commentError.value = null
    }

    // -----------------------------------------------------
    // FACTORY
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
}