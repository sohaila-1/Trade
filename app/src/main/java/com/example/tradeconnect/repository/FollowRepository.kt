package com.example.tradeconnect.repository

import com.example.tradeconnect.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FollowRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val usersRef = firestore.collection("users")

    // ==================== UTILISATEUR ACTUEL ====================

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun observeCurrentUser(): Flow<User?> = callbackFlow {
        val uid = getCurrentUserId()
        if (uid == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener = usersRef.document(uid)
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

    suspend fun getUserById(uid: String): User? {
        return try {
            val doc = usersRef.document(uid).get().await()
            doc.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun observeUser(uid: String): Flow<User?> = callbackFlow {
        val listener = usersRef.document(uid)
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

    fun getAllUsers(): Flow<List<User>> = callbackFlow {
        val currentUid = getCurrentUserId()

        val listener = usersRef
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val users = snapshot?.documents
                    ?.mapNotNull { it.toObject(User::class.java) }
                    ?.filter { it.uid != currentUid }
                    ?: emptyList()
                trySend(users)
            }
        awaitClose { listener.remove() }
    }

    // ==================== FOLLOW / UNFOLLOW ====================

    suspend fun followUser(targetUserId: String): Result<Unit> {
        val currentUserId = getCurrentUserId()
            ?: return Result.failure(Exception("Non connecté"))

        if (currentUserId == targetUserId) {
            return Result.failure(Exception("Vous ne pouvez pas vous suivre"))
        }

        // Vérifier si l'utilisateur est bloqué
        if (isBlocked(targetUserId)) {
            return Result.failure(Exception("Vous avez bloqué cet utilisateur"))
        }

        if (isBlockedBy(targetUserId)) {
            return Result.failure(Exception("Cet utilisateur vous a bloqué"))
        }

        return try {
            firestore.runBatch { batch ->
                val currentUserRef = usersRef.document(currentUserId)
                val targetUserRef = usersRef.document(targetUserId)

                batch.update(currentUserRef, "following", FieldValue.arrayUnion(targetUserId))
                batch.update(targetUserRef, "followers", FieldValue.arrayUnion(currentUserId))
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unfollowUser(targetUserId: String): Result<Unit> {
        val currentUserId = getCurrentUserId()
            ?: return Result.failure(Exception("Non connecté"))

        return try {
            firestore.runBatch { batch ->
                val currentUserRef = usersRef.document(currentUserId)
                val targetUserRef = usersRef.document(targetUserId)

                batch.update(currentUserRef, "following", FieldValue.arrayRemove(targetUserId))
                batch.update(targetUserRef, "followers", FieldValue.arrayRemove(currentUserId))
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== LISTES FOLLOWERS / FOLLOWING ====================

    suspend fun getFollowers(userId: String): List<User> {
        return try {
            val userDoc = usersRef.document(userId).get().await()
            val user = userDoc.toObject(User::class.java)
            val followerIds = user?.followers ?: emptyList()

            if (followerIds.isEmpty()) return emptyList()

            followerIds.chunked(10).flatMap { chunk ->
                usersRef.whereIn("uid", chunk).get().await()
                    .documents.mapNotNull { it.toObject(User::class.java) }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getFollowing(userId: String): List<User> {
        return try {
            val userDoc = usersRef.document(userId).get().await()
            val user = userDoc.toObject(User::class.java)
            val followingIds = user?.following ?: emptyList()

            if (followingIds.isEmpty()) return emptyList()

            followingIds.chunked(10).flatMap { chunk ->
                usersRef.whereIn("uid", chunk).get().await()
                    .documents.mapNotNull { it.toObject(User::class.java) }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ==================== BLOCKED USERS ====================

    suspend fun getBlockedUsers(): List<User> {
        val currentUserId = getCurrentUserId() ?: return emptyList()

        return try {
            val snapshot = usersRef
                .document(currentUserId)
                .collection("blocked")
                .get()
                .await()

            val blockedIds = snapshot.documents.map { it.id }

            if (blockedIds.isEmpty()) {
                return emptyList()
            }

            // Récupérer les infos des utilisateurs bloqués
            val users = mutableListOf<User>()
            for (id in blockedIds) {
                val userDoc = usersRef.document(id).get().await()
                userDoc.toObject(User::class.java)?.let { users.add(it) }
            }

            users
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun blockUser(userId: String): Result<Unit> {
        val currentUserId = getCurrentUserId()
            ?: return Result.failure(Exception("Non connecté"))

        if (currentUserId == userId) {
            return Result.failure(Exception("Vous ne pouvez pas vous bloquer"))
        }

        return try {
            // Ajouter à la collection "blocked"
            usersRef
                .document(currentUserId)
                .collection("blocked")
                .document(userId)
                .set(mapOf(
                    "blockedAt" to System.currentTimeMillis()
                ))
                .await()

            // Unfollow mutuellement
            firestore.runBatch { batch ->
                val currentUserRef = usersRef.document(currentUserId)
                val targetUserRef = usersRef.document(userId)

                // Je ne suis plus cette personne
                batch.update(currentUserRef, "following", FieldValue.arrayRemove(userId))
                // Cette personne n'est plus mon follower
                batch.update(currentUserRef, "followers", FieldValue.arrayRemove(userId))

                // L'inverse aussi
                batch.update(targetUserRef, "following", FieldValue.arrayRemove(currentUserId))
                batch.update(targetUserRef, "followers", FieldValue.arrayRemove(currentUserId))
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unblockUser(userId: String): Result<Unit> {
        val currentUserId = getCurrentUserId()
            ?: return Result.failure(Exception("Non connecté"))

        return try {
            usersRef
                .document(currentUserId)
                .collection("blocked")
                .document(userId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isBlocked(userId: String): Boolean {
        val currentUserId = getCurrentUserId() ?: return false

        return try {
            val doc = usersRef
                .document(currentUserId)
                .collection("blocked")
                .document(userId)
                .get()
                .await()

            doc.exists()
        } catch (e: Exception) {
            false
        }
    }

    // Vérifier si l'utilisateur actuel est bloqué par userId
    suspend fun isBlockedBy(userId: String): Boolean {
        val currentUserId = getCurrentUserId() ?: return false

        return try {
            val doc = usersRef
                .document(userId)
                .collection("blocked")
                .document(currentUserId)
                .get()
                .await()

            doc.exists()
        } catch (e: Exception) {
            false
        }
    }
}