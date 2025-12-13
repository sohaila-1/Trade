// app/src/main/java/com/example/tradeconnect/data/repository/UserRepository.kt
package com.example.tradeconnect.data.repository

import com.example.tradeconnect.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : IUserRepository {

    companion object {
        private const val USERS_COLLECTION = "users"
    }

    // ==================== UTILISATEUR ACTUEL ====================

    override fun getCurrentUserId(): String? = auth.currentUser?.uid

    override fun observeCurrentUser(): Flow<User?> = callbackFlow {
        val uid = getCurrentUserId()
        if (uid == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener = firestore.collection(USERS_COLLECTION)
            .document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val user = snapshot?.toObject(User::class.java)
                trySend(user)
            }
        awaitClose { listener.remove() }
    }

    // ==================== RÉCUPÉRATION UTILISATEURS ====================

    override suspend fun getUserById(uid: String): Result<User?> {
        return try {
            val doc = firestore.collection(USERS_COLLECTION)
                .document(uid)
                .get()
                .await()
            val user = doc.toObject(User::class.java)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeUser(uid: String): Flow<User?> = callbackFlow {
        val listener = firestore.collection(USERS_COLLECTION)
            .document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val user = snapshot?.toObject(User::class.java)
                trySend(user)
            }
        awaitClose { listener.remove() }
    }

    override fun getAllUsers(): Flow<List<User>> = callbackFlow {
        val currentUid = getCurrentUserId()

        val listener = firestore.collection(USERS_COLLECTION)
            .orderBy("username", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val users = snapshot?.documents
                    ?.mapNotNull { it.toObject(User::class.java) }
                    ?.filter { it.uid != currentUid } // Exclure l'utilisateur actuel
                    ?: emptyList()
                trySend(users)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun searchUsers(query: String): Result<List<User>> {
        return try {
            val currentUid = getCurrentUserId()
            val queryLower = query.lowercase().trim()

            if (queryLower.isEmpty()) {
                return Result.success(emptyList())
            }

            // Recherche par username (commence par)
            val snapshot = firestore.collection(USERS_COLLECTION)
                .orderBy("username")
                .startAt(queryLower)
                .endAt(queryLower + "\uf8ff")
                .get()
                .await()

            val users = snapshot.documents
                .mapNotNull { it.toObject(User::class.java) }
                .filter { it.uid != currentUid }

            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== FOLLOW / UNFOLLOW ====================

    override suspend fun followUser(targetUserId: String): Result<Unit> {
        val currentUserId = getCurrentUserId()
            ?: return Result.failure(Exception("Utilisateur non connecté"))

        if (currentUserId == targetUserId) {
            return Result.failure(Exception("Vous ne pouvez pas vous suivre vous-même"))
        }

        return try {
            // Utiliser une transaction batch pour garantir la cohérence
            firestore.runBatch { batch ->
                val currentUserRef = firestore.collection(USERS_COLLECTION).document(currentUserId)
                val targetUserRef = firestore.collection(USERS_COLLECTION).document(targetUserId)

                // Ajouter targetUserId à la liste "following" de l'utilisateur actuel
                batch.update(currentUserRef, "following", FieldValue.arrayUnion(targetUserId))

                // Ajouter currentUserId à la liste "followers" de l'utilisateur cible
                batch.update(targetUserRef, "followers", FieldValue.arrayUnion(currentUserId))
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unfollowUser(targetUserId: String): Result<Unit> {
        val currentUserId = getCurrentUserId()
            ?: return Result.failure(Exception("Utilisateur non connecté"))

        return try {
            firestore.runBatch { batch ->
                val currentUserRef = firestore.collection(USERS_COLLECTION).document(currentUserId)
                val targetUserRef = firestore.collection(USERS_COLLECTION).document(targetUserId)

                // Retirer targetUserId de la liste "following" de l'utilisateur actuel
                batch.update(currentUserRef, "following", FieldValue.arrayRemove(targetUserId))

                // Retirer currentUserId de la liste "followers" de l'utilisateur cible
                batch.update(targetUserRef, "followers", FieldValue.arrayRemove(currentUserId))
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isFollowing(targetUserId: String): Boolean {
        val currentUserId = getCurrentUserId() ?: return false

        return try {
            val doc = firestore.collection(USERS_COLLECTION)
                .document(currentUserId)
                .get()
                .await()
            val user = doc.toObject(User::class.java)
            user?.following?.contains(targetUserId) ?: false
        } catch (e: Exception) {
            false
        }
    }

    // ==================== LISTES FOLLOWERS / FOLLOWING ====================

    override suspend fun getFollowers(userId: String): Result<List<User>> {
        return try {
            // Récupérer l'utilisateur pour obtenir sa liste de followers
            val userDoc = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .await()

            val user = userDoc.toObject(User::class.java)
            val followerIds = user?.followers ?: emptyList()

            if (followerIds.isEmpty()) {
                return Result.success(emptyList())
            }

            // Récupérer les utilisateurs par chunks de 10 (limite Firestore whereIn)
            val followers = followerIds.chunked(10).flatMap { chunk ->
                firestore.collection(USERS_COLLECTION)
                    .whereIn("uid", chunk)
                    .get()
                    .await()
                    .documents
                    .mapNotNull { it.toObject(User::class.java) }
            }

            Result.success(followers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getFollowing(userId: String): Result<List<User>> {
        return try {
            val userDoc = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .await()

            val user = userDoc.toObject(User::class.java)
            val followingIds = user?.following ?: emptyList()

            if (followingIds.isEmpty()) {
                return Result.success(emptyList())
            }

            val following = followingIds.chunked(10).flatMap { chunk ->
                firestore.collection(USERS_COLLECTION)
                    .whereIn("uid", chunk)
                    .get()
                    .await()
                    .documents
                    .mapNotNull { it.toObject(User::class.java) }
            }

            Result.success(following)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== STATUT EN LIGNE ====================

    override suspend fun updateOnlineStatus(isOnline: Boolean): Result<Unit> {
        val currentUserId = getCurrentUserId()
            ?: return Result.failure(Exception("Utilisateur non connecté"))

        return try {
            val updates = mapOf(
                "isOnline" to isOnline,
                "lastSeen" to System.currentTimeMillis()
            )

            firestore.collection(USERS_COLLECTION)
                .document(currentUserId)
                .update(updates)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}