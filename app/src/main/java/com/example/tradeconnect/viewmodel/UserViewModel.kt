package com.example.tradeconnect.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tradeconnect.data.model.User
import com.example.tradeconnect.data.repository.IUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel(
    private val userRepository: IUserRepository
) : ViewModel() {

    // ==================== ÉTATS ====================

    // Utilisateur actuellement connecté
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Liste de tous les utilisateurs (pour découverte)
    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> = _allUsers.asStateFlow()

    // Profil d'un utilisateur sélectionné
    private val _selectedUser = MutableStateFlow<User?>(null)
    val selectedUser: StateFlow<User?> = _selectedUser.asStateFlow()

    // Liste des followers
    private val _followers = MutableStateFlow<List<User>>(emptyList())
    val followers: StateFlow<List<User>> = _followers.asStateFlow()

    // Liste des following
    private val _following = MutableStateFlow<List<User>>(emptyList())
    val following: StateFlow<List<User>> = _following.asStateFlow()

    // Map du statut de suivi pour chaque utilisateur (uid -> isFollowing)
    private val _followingStatus = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val followingStatus: StateFlow<Map<String, Boolean>> = _followingStatus.asStateFlow()

    // États UI
    var isLoading by mutableStateOf(false)
        private set

    var isFollowLoading by mutableStateOf<String?>(null) // UID de l'utilisateur en cours de follow/unfollow
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var successMessage by mutableStateOf<String?>(null)
        private set

    // ==================== INITIALISATION ====================

    init {
        observeCurrentUser()
        observeAllUsers()
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            userRepository.observeCurrentUser().collect { user ->
                _currentUser.value = user
                // Mettre à jour la map des statuts de suivi
                user?.following?.let { followingList ->
                    val statusMap = followingList.associateWith { true }
                    _followingStatus.value = statusMap
                }
            }
        }
    }

    private fun observeAllUsers() {
        viewModelScope.launch {
            userRepository.getAllUsers().collect { users ->
                _allUsers.value = users
            }
        }
    }

    // ==================== PROFIL UTILISATEUR ====================

    /**
     * Charger le profil d'un utilisateur spécifique
     */
    fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val result = userRepository.getUserById(userId)
            result.onSuccess { user ->
                _selectedUser.value = user
            }.onFailure { e ->
                errorMessage = e.message ?: "Erreur lors du chargement du profil"
            }

            isLoading = false
        }
    }

    /**
     * Observer un profil en temps réel
     */
    fun observeUserProfile(userId: String) {
        viewModelScope.launch {
            userRepository.observeUser(userId).collect { user ->
                _selectedUser.value = user
            }
        }
    }

    /**
     * Effacer l'utilisateur sélectionné
     */
    fun clearSelectedUser() {
        _selectedUser.value = null
    }

    // ==================== FOLLOW / UNFOLLOW ====================

    /**
     * Suivre un utilisateur
     */
    fun followUser(targetUserId: String) {
        viewModelScope.launch {
            isFollowLoading = targetUserId
            errorMessage = null

            val result = userRepository.followUser(targetUserId)
            result.onSuccess {
                // Mettre à jour le statut local immédiatement
                _followingStatus.value = _followingStatus.value + (targetUserId to true)
                successMessage = "Vous suivez maintenant cet utilisateur"
            }.onFailure { e ->
                errorMessage = e.message ?: "Erreur lors du suivi"
            }

            isFollowLoading = null
        }
    }

    /**
     * Ne plus suivre un utilisateur
     */
    fun unfollowUser(targetUserId: String) {
        viewModelScope.launch {
            isFollowLoading = targetUserId
            errorMessage = null

            val result = userRepository.unfollowUser(targetUserId)
            result.onSuccess {
                // Mettre à jour le statut local immédiatement
                _followingStatus.value = _followingStatus.value + (targetUserId to false)
                successMessage = "Vous ne suivez plus cet utilisateur"
            }.onFailure { e ->
                errorMessage = e.message ?: "Erreur lors du désabonnement"
            }

            isFollowLoading = null
        }
    }

    /**
     * Basculer le statut de suivi (follow <-> unfollow)
     */
    fun toggleFollow(targetUserId: String) {
        val isCurrentlyFollowing = isFollowing(targetUserId)
        if (isCurrentlyFollowing) {
            unfollowUser(targetUserId)
        } else {
            followUser(targetUserId)
        }
    }

    /**
     * Vérifier si l'utilisateur actuel suit un autre utilisateur
     */
    fun isFollowing(userId: String): Boolean {
        return _followingStatus.value[userId] ?: false
    }

    /**
     * Vérifier si un utilisateur nous suit (follower)
     */
    fun isFollowedBy(userId: String): Boolean {
        return _currentUser.value?.followers?.contains(userId) ?: false
    }

    // ==================== LISTES FOLLOWERS / FOLLOWING ====================

    /**
     * Charger la liste des followers d'un utilisateur
     */
    fun loadFollowers(userId: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val result = userRepository.getFollowers(userId)
            result.onSuccess { users ->
                _followers.value = users
            }.onFailure { e ->
                errorMessage = e.message ?: "Erreur lors du chargement des followers"
            }

            isLoading = false
        }
    }

    /**
     * Charger la liste des utilisateurs suivis
     */
    fun loadFollowing(userId: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val result = userRepository.getFollowing(userId)
            result.onSuccess { users ->
                _following.value = users
            }.onFailure { e ->
                errorMessage = e.message ?: "Erreur lors du chargement des abonnements"
            }

            isLoading = false
        }
    }

    /**
     * Effacer les listes followers/following
     */
    fun clearFollowLists() {
        _followers.value = emptyList()
        _following.value = emptyList()
    }

    // ==================== RECHERCHE ====================

    /**
     * Rechercher des utilisateurs
     */
    fun searchUsers(query: String) {
        if (query.isBlank()) {
            // Si la recherche est vide, recharger tous les utilisateurs
            observeAllUsers()
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            val result = userRepository.searchUsers(query)
            result.onSuccess { users ->
                _allUsers.value = users
            }.onFailure { e ->
                errorMessage = e.message ?: "Erreur lors de la recherche"
            }

            isLoading = false
        }
    }

    // ==================== STATUT EN LIGNE ====================

    /**
     * Mettre à jour le statut en ligne
     */
    fun updateOnlineStatus(isOnline: Boolean) {
        viewModelScope.launch {
            userRepository.updateOnlineStatus(isOnline)
        }
    }

    // ==================== UTILITAIRES ====================

    /**
     * Effacer les messages d'erreur/succès
     */
    fun clearMessages() {
        errorMessage = null
        successMessage = null
    }

    /**
     * Obtenir l'UID de l'utilisateur actuel
     */
    fun getCurrentUserId(): String? = userRepository.getCurrentUserId()

    /**
     * Vérifier si c'est le profil de l'utilisateur actuel
     */
    fun isCurrentUser(userId: String): Boolean {
        return getCurrentUserId() == userId
    }

    /**
     * Rafraîchir toutes les données
     */
    fun refresh() {
        observeCurrentUser()
        observeAllUsers()
    }

    // ==================== FACTORY ====================

    class Factory(
        private val userRepository: IUserRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return UserViewModel(userRepository) as T
        }
    }
}