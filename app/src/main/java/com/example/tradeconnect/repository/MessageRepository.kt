package com.example.tradeconnect.data.repository

import android.util.Log
import com.example.tradeconnect.data.local.AppDatabase
import com.example.tradeconnect.data.local.MessageEntity
import com.example.tradeconnect.data.local.UserEntity
import com.example.tradeconnect.data.model.ChatPreview
import com.example.tradeconnect.data.model.Message
import com.example.tradeconnect.data.model.MessageStatus
import com.example.tradeconnect.data.model.User
import com.example.tradeconnect.data.remote.ChatFirebaseService
import kotlinx.coroutines.flow.*
import java.util.UUID

class MessageRepository(
    private val database: AppDatabase,
    private val firebaseService: ChatFirebaseService
)
{

    private val messageDao = database.messageDao()
    private val userDao = database.userDao()

    fun getCurrentUserId(): String? = firebaseService.getCurrentUserId()

    // Get chat previews - ONLY for current user
    fun getChatPreviews(): Flow<List<ChatPreview>> = flow {
        val currentUserId = getCurrentUserId()
        if (currentUserId == null) {
            Log.w("MessageRepository", "No current user, returning empty chat previews")
            emit(emptyList())
            return@flow
        }

        Log.d("MessageRepository", "Loading chat previews for user: $currentUserId")

        // Start by emitting local data immediately (for offline support)
        // FIXED: Filter by ownerUserId
        messageDao.getLastMessagesForAllChats(currentUserId)
            .catch { e ->
                Log.e("MessageRepository", "Error loading chat previews", e)
                emit(emptyList())
            }
            .collect { messageEntities ->
                // Group by chat partner
                val chatMap = mutableMapOf<String, MessageEntity>()
                messageEntities.forEach { message ->
                    val partnerId = message.chatPartnerId
                    val existing = chatMap[partnerId]
                    if (existing == null || message.timestamp > existing.timestamp) {
                        chatMap[partnerId] = message
                    }
                }

                // Create chat previews
                val previews = chatMap.map { (partnerId, lastMessage) ->
                    // Get user info (will fetch from Firebase if not cached)
                    val user = getUserById(userId = partnerId) ?: User(
                        uid = partnerId,
                        username = "Unknown",
                        email = "",
                        mobile = "",
                        profileImageUrl = ""
                    )


                    // Count unread messages - FIXED: pass ownerUserId
                    val unreadCount = messageDao.getUnreadCount(partnerId, currentUserId)

                    ChatPreview(
                        user = user,
                        lastMessage = lastMessage.text,
                        lastMessageTime = lastMessage.timestamp,
                        unreadCount = unreadCount
                    )
                }.sortedByDescending { it.lastMessageTime }

                Log.d("MessageRepository", "Emitting ${previews.size} chat previews for user $currentUserId")
                emit(previews)
            }
    }.catch { e ->
        Log.e("MessageRepository", "Error in getChatPreviews flow", e)
        emit(emptyList())
    }

    // Sync a specific conversation's recent messages
    suspend fun syncConversationHistory(partnerId: String, limit: Int = 50) {
        val currentUserId = getCurrentUserId() ?: return

        try {
            Log.d("MessageRepository", "Syncing conversation history with $partnerId")
            val messages = firebaseService.fetchMessageHistory(currentUserId, partnerId, limit)

            // Save to local database - FIXED: include ownerUserId
            messages.forEach { message ->
                messageDao.insertMessage(message.toEntity(partnerId, currentUserId))
            }

            Log.d("MessageRepository", "Synced ${messages.size} messages for $partnerId")
        } catch (e: Exception) {
            Log.e("MessageRepository", "Error syncing conversation history", e)
        }
    }

    /**
     * Sync ALL conversations for the current user from Firebase.
     * This should be called when:
     * 1. User logs in
     * 2. App starts with a logged-in user
     * 3. User opens the chat list screen
     */
    suspend fun syncAllConversations() {
        val currentUserId = getCurrentUserId() ?: run {
            Log.w("MessageRepository", "Cannot sync conversations - no user logged in")
            return
        }

        try {
            Log.d("MessageRepository", "Syncing all conversations for user: $currentUserId")

            // Get list of all conversation partner IDs from Firebase
            val partnerIds = firebaseService.getRecentConversations(currentUserId)
            Log.d("MessageRepository", "Found ${partnerIds.size} conversations to sync")

            // Sync each conversation
            partnerIds.forEach { partnerId ->
                try {
                    // Fetch messages for this conversation
                    val messages = firebaseService.fetchMessageHistory(currentUserId, partnerId, limit = 50)

                    // Save to local database - FIXED: include ownerUserId
                    if (messages.isNotEmpty()) {
                        messages.forEach { message ->
                            messageDao.insertMessage(message.toEntity(partnerId, currentUserId))
                        }
                        Log.d("MessageRepository", "Synced ${messages.size} messages with $partnerId")
                    }

                    // Also cache the user info
                    try {
                        val user = firebaseService.getUserById(partnerId)
                        if (user != null) {
                            userDao.insertUser(user.toEntity())
                        }
                    } catch (e: Exception) {
                        Log.e("MessageRepository", "Error caching user $partnerId", e)
                    }
                } catch (e: Exception) {
                    Log.e("MessageRepository", "Error syncing conversation with $partnerId", e)
                }
            }

            Log.d("MessageRepository", "Finished syncing all conversations")
        } catch (e: Exception) {
            Log.e("MessageRepository", "Error syncing all conversations", e)
        }
    }

    /**
     * Get messages for a chat with proper offline support
     * FIXED: Filter by ownerUserId to ensure user data isolation
     */
    fun getMessagesForChat(partnerId: String): Flow<List<Message>> {
        val currentUserId = getCurrentUserId() ?: return flowOf(emptyList())

        // Local messages flow - FIXED: Filter by ownerUserId
        val localMessagesFlow = messageDao.getMessagesForChat(partnerId, currentUserId)
            .map { entities ->
                entities.map { it.toMessage(currentUserId) }
            }
            .catch { e ->
                Log.e("MessageRepository", "Error loading local messages", e)
                emit(emptyList())
            }

        // Remote messages flow with error handling
        val remoteMessagesFlow = firebaseService.listenToMessages(currentUserId, partnerId)
            .catch { e ->
                Log.e("MessageRepository", "Firebase listener error, continuing with local only", e)
                emit(emptyList())
            }

        // Combine local and remote, prioritizing local for immediate display
        return combine(localMessagesFlow, remoteMessagesFlow) { localMessages, remoteMessages ->
            Log.d("MessageRepository", "Combining: ${localMessages.size} local, ${remoteMessages.size} remote")

            if (remoteMessages.isEmpty()) {
                Log.d("MessageRepository", "Using local messages only: ${localMessages.size}")
                localMessages
            } else {
                val mergedMessages = mergeMessages(localMessages, remoteMessages, partnerId, currentUserId)
                Log.d("MessageRepository", "Merged messages: ${mergedMessages.size}")
                mergedMessages
            }
        }.onStart {
            // Emit local messages immediately - FIXED: Filter by ownerUserId
            val initialLocal = messageDao.getMessagesForChat(partnerId, currentUserId).first()
                .map { it.toMessage(currentUserId) }
            Log.d("MessageRepository", "Initial emit: ${initialLocal.size} local messages")
            emit(initialLocal)
        }.distinctUntilChanged()
    }

    /**
     * Merge local and remote messages, saving any new remote messages to local DB
     * FIXED: Include ownerUserId when saving
     */
    private suspend fun mergeMessages(
        localMessages: List<Message>,
        remoteMessages: List<Message>,
        partnerId: String,
        ownerUserId: String
    ): List<Message> {
        val localIds = localMessages.map { it.id }.toSet()
        val allMessages = localMessages.toMutableList()

        remoteMessages.forEach { remoteMsg ->
            if (remoteMsg.id !in localIds) {
                try {
                    // FIXED: Include ownerUserId
                    messageDao.insertMessage(remoteMsg.toEntity(partnerId, ownerUserId))
                    allMessages.add(remoteMsg)
                    Log.d("MessageRepository", "Saved new remote message: ${remoteMsg.id}")
                } catch (e: Exception) {
                    Log.e("MessageRepository", "Error saving remote message", e)
                }
            } else {
                val localMsg = localMessages.find { it.id == remoteMsg.id }
                if (localMsg != null && remoteMsg.status.ordinal > localMsg.status.ordinal) {
                    try {
                        messageDao.updateMessageStatus(remoteMsg.id, remoteMsg.status.name)
                        val index = allMessages.indexOfFirst { it.id == remoteMsg.id }
                        if (index >= 0) {
                            allMessages[index] = localMsg.copy(status = remoteMsg.status)
                        }
                    } catch (e: Exception) {
                        Log.e("MessageRepository", "Error updating message status", e)
                    }
                }
            }
        }

        return allMessages.sortedBy { it.timestamp }
    }

    /**
     * Send message with immediate local save
     * FIXED: Include ownerUserId for proper data isolation
     */
    suspend fun sendMessage(receiverId: String, text: String, isOnline: Boolean): Result<Unit> {
        val currentUserId = getCurrentUserId() ?: return Result.failure(Exception("Not logged in"))

        val messageId = UUID.randomUUID().toString()
        val message = Message(
            id = messageId,
            senderId = currentUserId,
            receiverId = receiverId,
            text = text,
            timestamp = System.currentTimeMillis(),
            status = if (isOnline) MessageStatus.SENT else MessageStatus.PENDING,
            isSentByCurrentUser = true
        )

        // Save to local database FIRST - FIXED: Include ownerUserId
        try {
            messageDao.insertMessage(message.toEntity(receiverId, currentUserId))
            Log.d("MessageRepository", "Message saved locally: $messageId with status ${message.status}")
        } catch (e: Exception) {
            Log.e("MessageRepository", "Failed to save message locally", e)
            return Result.failure(e)
        }

        return if (isOnline) {
            try {
                val result = firebaseService.sendMessage(message)
                if (result.isSuccess) {
                    messageDao.updateMessageStatus(messageId, MessageStatus.SENT.name)
                    Log.d("MessageRepository", "Message sent to Firebase: $messageId")
                    Result.success(Unit)
                } else {
                    messageDao.updateMessageStatus(messageId, MessageStatus.PENDING.name)
                    Log.e("MessageRepository", "Failed to send to Firebase")
                    Result.failure(result.exceptionOrNull() ?: Exception("Send failed"))
                }
            } catch (e: Exception) {
                Log.e("MessageRepository", "Error sending message", e)
                messageDao.updateMessageStatus(messageId, MessageStatus.PENDING.name)
                Result.failure(e)
            }
        } else {
            Log.d("MessageRepository", "Message queued for sync (offline)")
            Result.success(Unit)
        }
    }

    // Sync pending messages when coming online - FIXED: Filter by ownerUserId
    suspend fun syncPendingMessages(): Result<Unit> {
        val currentUserId = getCurrentUserId() ?: return Result.failure(Exception("Not logged in"))

        return try {
            val pendingMessages = messageDao.getPendingMessages(currentUserId)
            Log.d("MessageRepository", "Syncing ${pendingMessages.size} pending messages")

            pendingMessages.forEach { entity ->
                val message = entity.toMessage(currentUserId)
                val result = firebaseService.sendMessage(message)

                if (result.isSuccess) {
                    messageDao.updateMessageStatus(entity.id, MessageStatus.SENT.name)
                    Log.d("MessageRepository", "Synced message: ${entity.id}")
                } else {
                    Log.e("MessageRepository", "Failed to sync message: ${entity.id}")
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("MessageRepository", "Error syncing pending messages", e)
            Result.failure(e)
        }
    }

    // Mark messages as seen - FIXED: Include ownerUserId
    suspend fun markMessagesAsSeen(partnerId: String) {
        val currentUserId = getCurrentUserId() ?: return

        try {
            messageDao.markMessagesAsSeen(partnerId, currentUserId)
        } catch (e: Exception) {
            Log.e("MessageRepository", "Error marking messages as seen locally", e)
        }

        try {
            firebaseService.markMessagesAsSeen(currentUserId, partnerId)
        } catch (e: Exception) {
            Log.e("MessageRepository", "Error marking messages as seen in Firebase", e)
        }
    }

    // Search users
    fun searchUsers(query: String): Flow<List<User>> = flow {
        if (query.isBlank()) {
            emit(emptyList())
            return@flow
        }

        userDao.searchUsers(query).first().let { cached ->
            if (cached.isNotEmpty()) {
                emit(cached.map { it.toUser() })
            }
        }

        try {
            val remoteUsers = firebaseService.searchUsers(query)
            if (remoteUsers.isNotEmpty()) {
                userDao.insertUsers(remoteUsers.map { it.toEntity() })
                emit(remoteUsers)
            }
        } catch (e: Exception) {
            Log.e("MessageRepository", "Error searching users remotely", e)
        }
    }

    // Get user by ID with caching
    suspend fun getUserById(userId: String): User? {
        val cached = userDao.getUserById(userId)

        return try {
            val remoteUser = firebaseService.getUserById(userId)
            if (remoteUser != null) {
                userDao.insertUser(remoteUser.toEntity())
                remoteUser
            } else {
                cached?.toUser()
            }
        } catch (e: Exception) {
            Log.e("MessageRepository", "Error getting user by ID, using cache", e)
            cached?.toUser()
        }
    }

    // Clear all local data on logout
    suspend fun clearAllData() {
        val currentUserId = getCurrentUserId()
        try {
            if (currentUserId != null) {
                // Clear only current user's messages
                messageDao.clearMessagesForUser(currentUserId)
                Log.d("MessageRepository", "Cleared messages for user: $currentUserId")
            } else {
                // Fallback: clear everything
                database.clearAllTables()
                Log.d("MessageRepository", "All local data cleared")
            }
        } catch (e: Exception) {
            Log.e("MessageRepository", "Error clearing data", e)
            // Fallback to clearing all tables
            try {
                database.clearAllTables()
            } catch (e2: Exception) {
                Log.e("MessageRepository", "Error clearing all tables", e2)
            }
        }
    }

    // Extension functions for mapping - FIXED: Include ownerUserId
    private fun Message.toEntity(chatPartnerId: String, ownerUserId: String) = MessageEntity(
        id = id,
        senderId = senderId,
        receiverId = receiverId,
        text = text,
        timestamp = timestamp,
        status = status.name,
        isSentByCurrentUser = isSentByCurrentUser,
        chatPartnerId = chatPartnerId,
        ownerUserId = ownerUserId
    )

    private fun MessageEntity.toMessage(currentUserId: String) = Message(
        id = id,
        senderId = senderId,
        receiverId = receiverId,
        text = text,
        timestamp = timestamp,
        status = MessageStatus.valueOf(status),
        isSentByCurrentUser = senderId == currentUserId
    )

    private fun User.toEntity() = UserEntity(
        uid = uid,
        email = email,
        username = username,
        mobile = mobile,
        profileImageUrl = profileImageUrl
    )

    private fun UserEntity.toUser() = User(
        uid = uid,
        email = email,
        username = username,
        mobile = mobile,
        profileImageUrl = profileImageUrl
    )
}