package com.example.tradeconnect.data.repository

import android.util.Log
import com.example.tradeconnect.data.local.AppDatabase
import com.example.tradeconnect.data.local.MessageEntity
import com.example.tradeconnect.data.local.UserEntity
import com.example.tradeconnect.data.model.ChatPreview
import com.example.tradeconnect.data.model.Message
import com.example.tradeconnect.data.model.MessageStatus
import com.example.tradeconnect.data.model.User
import com.example.tradeconnect.data.remote.FirebaseMessagingService
import kotlinx.coroutines.flow.*
import java.util.UUID

class MessageRepository(
    private val database: AppDatabase,
    private val firebaseService: FirebaseMessagingService
) {

    private val messageDao = database.messageDao()
    private val userDao = database.userDao()

    fun getCurrentUserId(): String? = firebaseService.getCurrentUserId()

    // Get chat previews with Firebase sync
    fun getChatPreviews(): Flow<List<ChatPreview>> = flow {
        val currentUserId = getCurrentUserId()
        if (currentUserId == null) {
            emit(emptyList())
            return@flow
        }

        // Start by emitting local data immediately (for offline support)
        messageDao.getLastMessagesForAllChats()
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
                    val user = getUserById(partnerId) ?: User(uid = partnerId, username = "Unknown")

                    // Count unread messages
                    val unreadCount = messageDao.getUnreadCount(partnerId)

                    ChatPreview(
                        user = user,
                        lastMessage = lastMessage.text,
                        lastMessageTime = lastMessage.timestamp,
                        unreadCount = unreadCount
                    )
                }.sortedByDescending { it.lastMessageTime }

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

            // Save to local database
            messages.forEach { message ->
                messageDao.insertMessage(message.toEntity(partnerId))
            }

            Log.d("MessageRepository", "Synced ${messages.size} messages for $partnerId")
        } catch (e: Exception) {
            Log.e("MessageRepository", "Error syncing conversation history", e)
        }
    }

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

                    // Save to local database
                    if (messages.isNotEmpty()) {
                        messages.forEach { message ->
                            messageDao.insertMessage(message.toEntity(partnerId))
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

    fun getMessagesForChat(partnerId: String): Flow<List<Message>> {
        val currentUserId = getCurrentUserId() ?: return flowOf(emptyList())

        // Local messages flow - this is the source of truth for offline
        val localMessagesFlow = messageDao.getMessagesForChat(partnerId)
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
                // Emit empty to signal we should use local only
                emit(emptyList())
            }

        // Combine local and remote, prioritizing local for immediate display
        return combine(localMessagesFlow, remoteMessagesFlow) { localMessages, remoteMessages ->
            Log.d("MessageRepository", "Combining: ${localMessages.size} local, ${remoteMessages.size} remote")

            if (remoteMessages.isEmpty()) {
                // No remote messages (offline or error) - use local only
                Log.d("MessageRepository", "Using local messages only: ${localMessages.size}")
                localMessages
            } else {
                // Merge local and remote, saving new remote messages to local DB
                val mergedMessages = mergeMessages(localMessages, remoteMessages, partnerId)
                Log.d("MessageRepository", "Merged messages: ${mergedMessages.size}")
                mergedMessages
            }
        }.onStart {
            // Emit local messages immediately before combine kicks in
            // This ensures we show something right away
            val initialLocal = messageDao.getMessagesForChat(partnerId).first()
                .map { it.toMessage(currentUserId) }
            Log.d("MessageRepository", "Initial emit: ${initialLocal.size} local messages")
            emit(initialLocal)
        }.distinctUntilChanged()
    }

    private suspend fun mergeMessages(
        localMessages: List<Message>,
        remoteMessages: List<Message>,
        partnerId: String
    ): List<Message> {
        val localIds = localMessages.map { it.id }.toSet()
        val allMessages = localMessages.toMutableList()

        // Add any remote messages not in local and save them
        remoteMessages.forEach { remoteMsg ->
            if (remoteMsg.id !in localIds) {
                // New message from remote - save to local DB
                try {
                    messageDao.insertMessage(remoteMsg.toEntity(partnerId))
                    allMessages.add(remoteMsg)
                    Log.d("MessageRepository", "Saved new remote message: ${remoteMsg.id}")
                } catch (e: Exception) {
                    Log.e("MessageRepository", "Error saving remote message", e)
                }
            } else {
                // Message exists locally - check if remote has updated status
                val localMsg = localMessages.find { it.id == remoteMsg.id }
                if (localMsg != null && remoteMsg.status.ordinal > localMsg.status.ordinal) {
                    // Remote has newer status, update local
                    try {
                        messageDao.updateMessageStatus(remoteMsg.id, remoteMsg.status.name)
                        // Update in our list too
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

        // Save to local database FIRST for immediate UI update
        // Room Flow will automatically emit this new message
        try {
            messageDao.insertMessage(message.toEntity(receiverId))
            Log.d("MessageRepository", "Message saved locally: $messageId with status ${message.status}")
        } catch (e: Exception) {
            Log.e("MessageRepository", "Failed to save message locally", e)
            return Result.failure(e)
        }

        return if (isOnline) {
            // Try to send to Firebase
            try {
                val result = firebaseService.sendMessage(message)
                if (result.isSuccess) {
                    // Update status in local DB
                    messageDao.updateMessageStatus(messageId, MessageStatus.SENT.name)
                    Log.d("MessageRepository", "Message sent to Firebase: $messageId")
                    Result.success(Unit)
                } else {
                    // Failed to send, keep as PENDING for retry
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
            // Offline - will be synced later
            Log.d("MessageRepository", "Message queued for sync (offline)")
            Result.success(Unit)
        }
    }

    // Sync pending messages when coming online
    suspend fun syncPendingMessages(): Result<Unit> {
        return try {
            val pendingMessages = messageDao.getPendingMessages()
            Log.d("MessageRepository", "Syncing ${pendingMessages.size} pending messages")

            pendingMessages.forEach { entity ->
                val message = entity.toMessage(entity.senderId)
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

    // Mark messages as seen
    suspend fun markMessagesAsSeen(partnerId: String) {
        val currentUserId = getCurrentUserId() ?: return

        // Update local first (works offline)
        try {
            messageDao.markMessagesAsSeen(partnerId)
        } catch (e: Exception) {
            Log.e("MessageRepository", "Error marking messages as seen locally", e)
        }

        // Then try Firebase
        try {
            firebaseService.markMessagesAsSeen(currentUserId, partnerId)
        } catch (e: Exception) {
            Log.e("MessageRepository", "Error marking messages as seen in Firebase", e)
            // Local is already updated, so this is fine
        }
    }

    // Search users
    fun searchUsers(query: String): Flow<List<User>> = flow {
        if (query.isBlank()) {
            emit(emptyList())
            return@flow
        }

        // First emit cached results
        userDao.searchUsers(query).first().let { cached ->
            if (cached.isNotEmpty()) {
                emit(cached.map { it.toUser() })
            }
        }

        // Then fetch from Firebase and update cache
        try {
            val remoteUsers = firebaseService.searchUsers(query)
            if (remoteUsers.isNotEmpty()) {
                userDao.insertUsers(remoteUsers.map { it.toEntity() })
                emit(remoteUsers)
            }
        } catch (e: Exception) {
            Log.e("MessageRepository", "Error searching users remotely", e)
            // If Firebase fails, just use cached results
        }
    }

    // Get user by ID with caching
    suspend fun getUserById(userId: String): User? {
        // Check cache first
        val cached = userDao.getUserById(userId)

        // Return cached immediately if available, then try to update
        return try {
            val remoteUser = firebaseService.getUserById(userId)
            if (remoteUser != null) {
                // Update cache
                userDao.insertUser(remoteUser.toEntity())
                remoteUser
            } else {
                cached?.toUser()
            }
        } catch (e: Exception) {
            Log.e("MessageRepository", "Error getting user by ID, using cache", e)
            // Return cached version if Firebase fails
            cached?.toUser()
        }
    }

    // Clear all local data on logout
    suspend fun clearAllData() {
        try {
            database.clearAllTables()
            Log.d("MessageRepository", "All local data cleared")
        } catch (e: Exception) {
            Log.e("MessageRepository", "Error clearing data", e)
        }
    }

    // Extension functions for mapping
    private fun Message.toEntity(chatPartnerId: String) = MessageEntity(
        id = id,
        senderId = senderId,
        receiverId = receiverId,
        text = text,
        timestamp = timestamp,
        status = status.name,
        isSentByCurrentUser = isSentByCurrentUser,
        chatPartnerId = chatPartnerId
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