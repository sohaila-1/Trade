// app/src/main/java/com/example/tradeconnect/repository/TweetRepository.kt
package com.example.tradeconnect.repository

import com.example.tradeconnect.model.Comment
import com.example.tradeconnect.model.Tweet
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TweetRepository(private val firestore: FirebaseFirestore) {

    private val tweetsRef = firestore.collection("tweets")

    // ==================== TWEETS ====================

    fun getTweetsByUser(uid: String, onResult: (List<Tweet>) -> Unit) {
        tweetsRef.whereEqualTo("userId", uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                onResult(snap?.toObjects(Tweet::class.java) ?: emptyList())
            }
    }

    fun getTweetsFromUsers(uids: List<String>, onResult: (List<Tweet>) -> Unit) {
        tweetsRef.whereIn("userId", uids)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                onResult(snap?.toObjects(Tweet::class.java) ?: emptyList())
            }
    }

    fun postTweet(tweet: Tweet, onSuccess: () -> Unit) {
        tweetsRef.document(tweet.id).set(tweet).addOnSuccessListener { onSuccess() }
    }

    fun updateTweet(id: String, newContent: String, onSuccess: () -> Unit) {
        tweetsRef.document(id)
            .update("content", newContent)
            .addOnSuccessListener { onSuccess() }
    }

    fun deleteTweet(id: String, onSuccess: () -> Unit) {
        tweetsRef.document(id).delete().addOnSuccessListener { onSuccess() }
    }

    fun getAllTweets(onResult: (List<Tweet>) -> Unit) {
        tweetsRef
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents.mapNotNull { it.toObject(Tweet::class.java) }
                onResult(list)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    // Observer un tweet en temps réel
    fun observeTweet(tweetId: String): Flow<Tweet?> = callbackFlow {
        val listener = tweetsRef.document(tweetId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val tweet = snapshot?.toObject(Tweet::class.java)
                trySend(tweet)
            }
        awaitClose { listener.remove() }
    }

    // Récupérer un tweet par son ID
    suspend fun getTweetById(tweetId: String): Tweet? {
        return try {
            tweetsRef.document(tweetId).get().await().toObject(Tweet::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun toggleLike(tweetId: String, uid: String) {
        val ref = tweetsRef.document(tweetId)

        firestore.runTransaction { tx ->
            val snap = tx.get(ref)
            val likes = snap.get("likes") as? List<String> ?: emptyList()

            val updatedLikes = if (uid in likes) {
                likes - uid
            } else {
                likes + uid
            }

            tx.update(ref, "likes", updatedLikes)
        }
    }

    fun toggleSave(tweetId: String, userId: String) {
        val ref = tweetsRef.document(tweetId)

        ref.get().addOnSuccessListener { doc ->
            val saves = doc.get("saves") as? MutableList<String> ?: mutableListOf()

            val update = if (saves.contains(userId))
                FieldValue.arrayRemove(userId)
            else
                FieldValue.arrayUnion(userId)

            ref.update("saves", update)
        }
    }

    fun getSavedTweets(userId: String, onResult: (List<Tweet>) -> Unit) {
        tweetsRef.whereArrayContains("saves", userId)
            .get()
            .addOnSuccessListener { snap ->
                onResult(snap.toObjects(Tweet::class.java))
            }
            .addOnFailureListener { onResult(emptyList()) }
    }

    suspend fun getBookmarkedTweets(userId: String): List<Tweet> {
        return firestore.collection("tweets")
            .whereArrayContains("savedBy", userId)
            .get()
            .await()
            .toObjects(Tweet::class.java)
    }

    // ==================== COMMENTAIRES ====================

    // Ajouter un commentaire
    suspend fun addComment(comment: Comment): Result<Unit> {
        return try {
            val tweetRef = tweetsRef.document(comment.tweetId)
            val commentRef = tweetRef.collection("comments").document(comment.id)

            firestore.runBatch { batch ->
                // Ajouter le commentaire
                batch.set(commentRef, comment)
                // Incrémenter le compteur de commentaires
                batch.update(tweetRef, "commentsCount", FieldValue.increment(1))
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Supprimer un commentaire
    suspend fun deleteComment(tweetId: String, commentId: String): Result<Unit> {
        return try {
            val tweetRef = tweetsRef.document(tweetId)
            val commentRef = tweetRef.collection("comments").document(commentId)

            firestore.runBatch { batch ->
                // Supprimer le commentaire
                batch.delete(commentRef)
                // Décrémenter le compteur de commentaires
                batch.update(tweetRef, "commentsCount", FieldValue.increment(-1))
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Observer les commentaires d'un tweet en temps réel
    fun observeComments(tweetId: String): Flow<List<Comment>> = callbackFlow {
        val listener = tweetsRef.document(tweetId)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val comments = snapshot?.toObjects(Comment::class.java) ?: emptyList()
                trySend(comments)
            }
        awaitClose { listener.remove() }
    }

    // Récupérer les commentaires d'un tweet (une seule fois)
    suspend fun getComments(tweetId: String): List<Comment> {
        return try {
            tweetsRef.document(tweetId)
                .collection("comments")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .await()
                .toObjects(Comment::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Liker/Unliker un commentaire
    fun toggleCommentLike(tweetId: String, commentId: String, userId: String) {
        val ref = tweetsRef.document(tweetId)
            .collection("comments")
            .document(commentId)

        firestore.runTransaction { tx ->
            val snap = tx.get(ref)
            val likes = snap.get("likes") as? List<String> ?: emptyList()

            val updatedLikes = if (userId in likes) {
                likes - userId
            } else {
                likes + userId
            }

            tx.update(ref, "likes", updatedLikes)
        }
    }
}