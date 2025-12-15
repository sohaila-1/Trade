// app/src/main/java/com/example/tradeconnect/repository/FollowRepository.kt
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

    // Observer l'utilisateur actuel en temps réel
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

    // Obtenir un utilisateur par son UID
    suspend fun getUserById(uid: String): User? {
        return try {
            val doc = usersRef.document(uid).get().await()
            doc.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // Observer un utilisateur en temps réel
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

    // Obtenir tous les utilisateurs (sauf l'utilisateur actuel)
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

    // Suivre un utilisateur
    suspend fun followUser(targetUserId: String): Result<Unit> {
        val currentUserId = getCurrentUserId()
            ?: return Result.failure(Exception("Non connecté"))

        if (currentUserId == targetUserId) {
            return Result.failure(Exception("Vous ne pouvez pas vous suivre"))
        }

        return try {
            firestore.runBatch { batch ->
                val currentUserRef = usersRef.document(currentUserId)
                val targetUserRef = usersRef.document(targetUserId)

                // Ajouter targetUserId à ma liste "following"
                batch.update(currentUserRef, "following", FieldValue.arrayUnion(targetUserId))

                // Ajouter currentUserId à la liste "followers" de l'autre
                batch.update(targetUserRef, "followers", FieldValue.arrayUnion(currentUserId))
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Ne plus suivre un utilisateur
    suspend fun unfollowUser(targetUserId: String): Result<Unit> {
        val currentUserId = getCurrentUserId()
            ?: return Result.failure(Exception("Non connecté"))

        return try {
            firestore.runBatch { batch ->
                val currentUserRef = usersRef.document(currentUserId)
                val targetUserRef = usersRef.document(targetUserId)

                // Retirer targetUserId de ma liste "following"
                batch.update(currentUserRef, "following", FieldValue.arrayRemove(targetUserId))

                // Retirer currentUserId de la liste "followers" de l'autre
                batch.update(targetUserRef, "followers", FieldValue.arrayRemove(currentUserId))
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================== LISTES FOLLOWERS / FOLLOWING ====================

    // Obtenir la liste des followers d'un utilisateur
    suspend fun getFollowers(userId: String): List<User> {
        return try {
            val userDoc = usersRef.document(userId).get().await()
            val user = userDoc.toObject(User::class.java)
            val followerIds = user?.followers ?: emptyList()

            if (followerIds.isEmpty()) return emptyList()

            // Récupérer les utilisateurs par chunks de 10 (limite Firestore)
            followerIds.chunked(10).flatMap { chunk ->
                usersRef.whereIn("uid", chunk).get().await()
                    .documents.mapNotNull { it.toObject(User::class.java) }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Obtenir la liste des utilisateurs suivis
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
}