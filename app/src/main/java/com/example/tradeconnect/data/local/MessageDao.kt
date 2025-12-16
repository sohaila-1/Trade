package com.example.tradeconnect.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages WHERE chatPartnerId = :partnerId AND ownerUserId = :ownerUserId ORDER BY timestamp ASC")
    fun getMessagesForChat(partnerId: String, ownerUserId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Query("SELECT * FROM messages WHERE status = 'PENDING' AND ownerUserId = :ownerUserId")
    suspend fun getPendingMessages(ownerUserId: String): List<MessageEntity>

    @Query("UPDATE messages SET status = :status WHERE id = :messageId")
    suspend fun updateMessageStatus(messageId: String, status: String)

    @Query("DELETE FROM messages WHERE chatPartnerId = :partnerId AND ownerUserId = :ownerUserId")
    suspend fun deleteMessagesForChat(partnerId: String, ownerUserId: String)

    @Query("""
        SELECT * FROM messages 
        WHERE ownerUserId = :ownerUserId
        AND id IN (
            SELECT id FROM messages 
            WHERE ownerUserId = :ownerUserId
            AND chatPartnerId IN (
                SELECT DISTINCT chatPartnerId FROM messages WHERE ownerUserId = :ownerUserId
            )
            GROUP BY chatPartnerId
            HAVING timestamp = MAX(timestamp)
        )
        ORDER BY timestamp DESC
    """)
    fun getLastMessagesForAllChats(ownerUserId: String): Flow<List<MessageEntity>>

    // Get unread message count for a specific chat
    @Query("""
        SELECT COUNT(*) FROM messages 
        WHERE chatPartnerId = :partnerId 
        AND ownerUserId = :ownerUserId
        AND senderId = :partnerId 
        AND status != 'SEEN'
    """)
    suspend fun getUnreadCount(partnerId: String, ownerUserId: String): Int

    // Mark all messages from a partner as seen
    @Query("""
        UPDATE messages 
        SET status = 'SEEN' 
        WHERE chatPartnerId = :partnerId 
        AND ownerUserId = :ownerUserId
        AND senderId = :partnerId
    """)
    suspend fun markMessagesAsSeen(partnerId: String, ownerUserId: String)

    // Clear all messages for a specific user (on logout)
    @Query("DELETE FROM messages WHERE ownerUserId = :ownerUserId")
    suspend fun clearMessagesForUser(ownerUserId: String)

    // Clear all messages (fallback)
    @Query("DELETE FROM messages")
    suspend fun clearAllMessages()
}