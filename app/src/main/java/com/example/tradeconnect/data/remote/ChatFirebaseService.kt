package com.example.tradeconnect.data.remote

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.tradeconnect.data.model.Message
import com.example.tradeconnect.data.model.MessageStatus
import com.example.tradeconnect.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatFirebaseService(
    private val database: FirebaseDatabase,
    private val auth: FirebaseAuth
) {

    private val messagesRef = database.getReference("messages")
    private val firestore = FirebaseFirestore.getInstance()

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    // Send message to Firebase
    suspend fun sendMessage(message: Message): Result<String> {
        return try {
            val messageId = message.id.ifEmpty { messagesRef.push().key ?: return Result.failure(Exception("Failed to generate ID")) }
            val messageMap = mapOf(
                "id" to messageId,
                "senderId" to message.senderId,
                "receiverId" to message.receiverId,
                "text" to message.text,
                "timestamp" to message.timestamp,
                "status" to MessageStatus.SENT.name
            )

            // Save to both users' message paths
            val updates = hashMapOf<String, Any>(
                "/messages/${message.senderId}/${message.receiverId}/$messageId" to messageMap,
                "/messages/${message.receiverId}/${message.senderId}/$messageId" to messageMap
            )

            database.reference.updateChildren(updates).await()
            Result.success(messageId)
        } catch (e: Exception) {
            Log.e("FirebaseMessaging", "Error sending message", e)
            Result.failure(e)
        }
    }

    // Listen to messages in real-time
    fun listenToMessages(currentUserId: String, otherUserId: String): Flow<List<Message>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<Message>()
                snapshot.children.forEach { messageSnapshot ->
                    val message = messageSnapshot.toMessage(currentUserId)
                    message?.let { messages.add(it) }
                }
                trySend(messages.sortedBy { it.timestamp })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseMessaging", "Error listening to messages", error.toException())
                close(error.toException())
            }
        }

        val ref = messagesRef.child(currentUserId).child(otherUserId)
        ref.addValueEventListener(listener)

        awaitClose { ref.removeEventListener(listener) }
    }

    // Get recent conversations (list of partner IDs)
    suspend fun getRecentConversations(currentUserId: String): List<String> {
        return try {
            val snapshot = messagesRef.child(currentUserId).get().await()
            snapshot.children.mapNotNull { it.key }
        } catch (e: Exception) {
            Log.e("FirebaseMessaging", "Error getting recent conversations", e)
            emptyList()
        }
    }

    // Fetch message history (for offline caching)
    suspend fun fetchMessageHistory(currentUserId: String, otherUserId: String, limit: Int = 50): List<Message> {
        return try {
            // Fetch without ordering (to avoid index requirement)
            val snapshot = messagesRef.child(currentUserId).child(otherUserId)
                .get()
                .await()

            val messages = mutableListOf<Message>()
            snapshot.children.forEach { messageSnapshot ->
                val message = messageSnapshot.toMessage(currentUserId)
                message?.let { messages.add(it) }
            }

            // Sort and limit in memory
            messages.sortedBy { it.timestamp }.takeLast(limit)
        } catch (e: Exception) {
            Log.e("FirebaseMessaging", "Error fetching message history", e)
            emptyList()
        }
    }

    // Mark messages as seen
    suspend fun markMessagesAsSeen(currentUserId: String, otherUserId: String) {
        try {
            val snapshot = messagesRef.child(currentUserId).child(otherUserId).get().await()
            val updates = hashMapOf<String, Any>()

            snapshot.children.forEach { messageSnapshot ->
                val senderId = messageSnapshot.child("senderId").getValue(String::class.java)
                if (senderId == otherUserId) {
                    val messageId = messageSnapshot.key ?: return@forEach
                    updates["/messages/$currentUserId/$otherUserId/$messageId/status"] = MessageStatus.SEEN.name
                    updates["/messages/$otherUserId/$currentUserId/$messageId/status"] = MessageStatus.SEEN.name
                }
            }

            if (updates.isNotEmpty()) {
                database.reference.updateChildren(updates).await()
            }
        } catch (e: Exception) {
            Log.e("FirebaseMessaging", "Error marking messages as seen", e)
        }
    }

    // Search users from Firestore
    suspend fun searchUsers(query: String, limit: Int = 20): List<User> {
        return try {
            Log.d("FirebaseMessaging", "Searching users with query: $query")

            val snapshot = firestore.collection("users")
                .get()
                .await()

            Log.d("FirebaseMessaging", "Found ${snapshot.documents.size} total users")

            // Filter users locally by username or email
            val results = snapshot.documents.mapNotNull { doc ->
                try {
                    val uid = doc.id
                    val email = doc.getString("email") ?: ""
                    val username = doc.getString("username") ?: ""
                    val mobile = doc.getString("mobile") ?: ""
                    val profileImageUrl = doc.getString("profileImageUrl") ?: ""

                    // Filter by query (case-insensitive)
                    val matchesQuery = query.isBlank() ||
                            username.contains(query, ignoreCase = true) ||
                            email.contains(query, ignoreCase = true)

                    // Exclude current user
                    val isNotCurrentUser = uid != getCurrentUserId()

                    if (matchesQuery && isNotCurrentUser) {
                        User(
                            uid = uid,
                            email = email,
                            username = username.ifEmpty { email.substringBefore("@") },
                            mobile = mobile,
                            profileImageUrl = profileImageUrl
                        )
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    Log.e("FirebaseMessaging", "Error parsing user document: ${doc.id}", e)
                    null
                }
            }.take(limit)

            Log.d("FirebaseMessaging", "Returning ${results.size} matching users")
            results

        } catch (e: Exception) {
            Log.e("FirebaseMessaging", "Error searching users", e)
            emptyList()
        }
    }

    // Get user by ID from Firestore
    suspend fun getUserById(userId: String): User? {
        return try {
            val doc = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            if (doc.exists()) {
                val uid = doc.id
                val email = doc.getString("email") ?: ""
                val username = doc.getString("username") ?: ""
                val mobile = doc.getString("mobile") ?: ""
                val profileImageUrl = doc.getString("profileImageUrl") ?: ""

                User(
                    uid = uid,
                    email = email,
                    username = username.ifEmpty { email.substringBefore("@") },
                    mobile = mobile,
                    profileImageUrl = profileImageUrl
                )
            } else {
                Log.e("FirebaseMessaging", "User document does not exist: $userId")
                null
            }
        } catch (e: Exception) {
            Log.e("FirebaseMessaging", "Error getting user by ID", e)
            null
        }
    }

    // Helper extensions
    private fun DataSnapshot.toMessage(currentUserId: String): Message? {
        return try {
            Message(
                id = child("id").getValue(String::class.java) ?: "",
                senderId = child("senderId").getValue(String::class.java) ?: "",
                receiverId = child("receiverId").getValue(String::class.java) ?: "",
                text = child("text").getValue(String::class.java) ?: "",
                timestamp = child("timestamp").getValue(Long::class.java) ?: 0L,
                status = MessageStatus.valueOf(child("status").getValue(String::class.java) ?: "SENT"),
                isSentByCurrentUser = child("senderId").getValue(String::class.java) == currentUserId
            )
        } catch (e: Exception) {
            Log.e("FirebaseMessaging", "Error parsing message", e)
            null
        }
    }
}