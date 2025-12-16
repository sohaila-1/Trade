package com.example.tradeconnect.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tradeconnect.data.model.User
import com.example.tradeconnect.repository.FollowRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FollowViewModel(
    private val followRepository: FollowRepository
) : ViewModel() {

    // Utilisateur connecté
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Tous les utilisateurs
    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> = _allUsers.asStateFlow()

    // Profil sélectionné
    private val _selectedUser = MutableStateFlow<User?>(null)
    val selectedUser: StateFlow<User?> = _selectedUser.asStateFlow()

    // Liste des followers
    private val _followers = MutableStateFlow<List<User>>(emptyList())
    val followers: StateFlow<List<User>> = _followers.asStateFlow()

    // Liste des following
    private val _following = MutableStateFlow<List<User>>(emptyList())
    val following: StateFlow<List<User>> = _following.asStateFlow()

    // Map du statut de suivi (uid -> isFollowing)
    private val _followingStatus = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val followingStatus: StateFlow<Map<String, Boolean>> = _followingStatus.asStateFlow()

    // ==================== BLOCKED USERS ====================

    private val _blockedUsers = MutableStateFlow<List<User>>(emptyList())
    val blockedUsers: StateFlow<List<User>> = _blockedUsers.asStateFlow()

    private val _isBlockedLoading = MutableStateFlow(false)
    val isBlockedLoading: StateFlow<Boolean> = _isBlockedLoading.asStateFlow()

    // Map du statut de blocage (uid -> isBlocked)
    private val _blockedStatus = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val blockedStatus: StateFlow<Map<String, Boolean>> = _blockedStatus.asStateFlow()

    // ==================== UI STATES ====================

    var isLoading by mutableStateOf(false)
        private set

    var isFollowLoading by mutableStateOf<String?>(null)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    // ==================== INIT ====================

    init {
        observeCurrentUser()
        observeAllUsers()
        loadBlockedUsers()
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            followRepository.observeCurrentUser().collect { user ->
                _currentUser.value = user
                user?.following?.let { followingList ->
                    _followingStatus.value = followingList.associateWith { true }
                }
            }
        }
    }

    private fun observeAllUsers() {
        viewModelScope.launch {
            followRepository.getAllUsers().collect { users ->
                _allUsers.value = users
            }
        }
    }

    // ==================== PROFIL ====================

    fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            // Vérifier si l'utilisateur est bloqué
            val isBlocked = followRepository.isBlocked(userId)
            val isBlockedBy = followRepository.isBlockedBy(userId)

            _blockedStatus.value = _blockedStatus.value + (userId to isBlocked)

            if (isBlockedBy) {
                errorMessage = "Cet utilisateur vous a bloqué"
                _selectedUser.value = null
                isLoading = false
                return@launch
            }

            val user = followRepository.getUserById(userId)
            _selectedUser.value = user
            isLoading = false
        }
    }

    fun observeUserProfile(userId: String) {
        viewModelScope.launch {
            followRepository.observeUser(userId).collect { user ->
                _selectedUser.value = user
            }
        }
    }

    fun clearSelectedUser() {
        _selectedUser.value = null
    }

    // ==================== FOLLOW / UNFOLLOW ====================

    fun followUser(targetUserId: String) {
        viewModelScope.launch {
            isFollowLoading = targetUserId
            errorMessage = null

            val result = followRepository.followUser(targetUserId)
            result.onSuccess {
                _followingStatus.value = _followingStatus.value + (targetUserId to true)
            }.onFailure { e ->
                errorMessage = e.message
            }

            isFollowLoading = null
        }
    }

    fun unfollowUser(targetUserId: String) {
        viewModelScope.launch {
            isFollowLoading = targetUserId
            errorMessage = null

            val result = followRepository.unfollowUser(targetUserId)
            result.onSuccess {
                _followingStatus.value = _followingStatus.value + (targetUserId to false)
            }.onFailure { e ->
                errorMessage = e.message
            }

            isFollowLoading = null
        }
    }

    fun toggleFollow(targetUserId: String) {
        if (isFollowing(targetUserId)) {
            unfollowUser(targetUserId)
        } else {
            followUser(targetUserId)
        }
    }

    fun isFollowing(userId: String): Boolean {
        return _followingStatus.value[userId] ?: false
    }

    // ==================== LISTES ====================

    fun loadFollowers(userId: String) {
        viewModelScope.launch {
            isLoading = true
            _followers.value = followRepository.getFollowers(userId)
            isLoading = false
        }
    }

    fun loadFollowing(userId: String) {
        viewModelScope.launch {
            isLoading = true
            _following.value = followRepository.getFollowing(userId)
            isLoading = false
        }
    }

    fun clearFollowLists() {
        _followers.value = emptyList()
        _following.value = emptyList()
    }

    // ==================== BLOCKED USERS ====================

    fun loadBlockedUsers() {
        viewModelScope.launch {
            _isBlockedLoading.value = true
            try {
                val users = followRepository.getBlockedUsers()
                _blockedUsers.value = users

                // Mettre à jour le statut de blocage
                val blockedMap = users.associate { it.uid to true }
                _blockedStatus.value = blockedMap
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                _isBlockedLoading.value = false
            }
        }
    }

    fun blockUser(userId: String) {
        viewModelScope.launch {
            isFollowLoading = userId
            errorMessage = null

            val result = followRepository.blockUser(userId)
            result.onSuccess {
                // Mettre à jour le statut local
                _blockedStatus.value = _blockedStatus.value + (userId to true)
                _followingStatus.value = _followingStatus.value + (userId to false)

                // Ajouter à la liste locale
                val user = followRepository.getUserById(userId)
                user?.let {
                    _blockedUsers.value = _blockedUsers.value + it
                }
            }.onFailure { e ->
                errorMessage = e.message
            }

            isFollowLoading = null
        }
    }

    fun unblockUser(userId: String) {
        viewModelScope.launch {
            isFollowLoading = userId
            errorMessage = null

            val result = followRepository.unblockUser(userId)
            result.onSuccess {
                // Mettre à jour le statut local
                _blockedStatus.value = _blockedStatus.value + (userId to false)

                // Retirer de la liste locale
                _blockedUsers.value = _blockedUsers.value.filter { it.uid != userId }
            }.onFailure { e ->
                errorMessage = e.message
            }

            isFollowLoading = null
        }
    }

    fun isUserBlocked(userId: String): Boolean {
        return _blockedStatus.value[userId] ?: false
    }

    suspend fun checkIfBlocked(userId: String): Boolean {
        return followRepository.isBlocked(userId)
    }

    // ==================== UTILS ====================

    fun getCurrentUserId(): String? = followRepository.getCurrentUserId()

    fun isCurrentUser(userId: String): Boolean = getCurrentUserId() == userId

    fun clearError() {
        errorMessage = null
    }

    // ==================== FACTORY ====================

    class Factory(
        private val followRepository: FollowRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FollowViewModel(followRepository) as T
        }
    }
}