// app/src/main/java/com/example/tradeconnect/data/repository/IUserRepository.kt
package com.example.tradeconnect.data.repository

import com.example.tradeconnect.data.model.User
import kotlinx.coroutines.flow.Flow

interface IUserRepository {

    // Obtenir l'UID de l'utilisateur connecté
    fun getCurrentUserId(): String?

    // Obtenir un utilisateur par son UID
    suspend fun getUserById(uid: String): Result<User?>

    // Observer un utilisateur en temps réel
    fun observeUser(uid: String): Flow<User?>

    // Observer l'utilisateur actuel en temps réel
    fun observeCurrentUser(): Flow<User?>

    // Obtenir tous les utilisateurs (sauf l'utilisateur actuel)
    fun getAllUsers(): Flow<List<User>>

    // Rechercher des utilisateurs
    suspend fun searchUsers(query: String): Result<List<User>>

    // Suivre un utilisateur
    suspend fun followUser(targetUserId: String): Result<Unit>

    // Ne plus suivre un utilisateur
    suspend fun unfollowUser(targetUserId: String): Result<Unit>

    // Vérifier si l'utilisateur actuel suit un autre utilisateur
    suspend fun isFollowing(targetUserId: String): Boolean

    // Obtenir la liste des followers d'un utilisateur
    suspend fun getFollowers(userId: String): Result<List<User>>

    // Obtenir la liste des utilisateurs suivis
    suspend fun getFollowing(userId: String): Result<List<User>>

    // Mettre à jour le statut en ligne
    suspend fun updateOnlineStatus(isOnline: Boolean): Result<Unit>
}