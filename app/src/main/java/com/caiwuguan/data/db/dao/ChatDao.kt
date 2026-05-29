package com.caiwuguan.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.caiwuguan.data.db.entity.ChatConversationEntity
import com.caiwuguan.data.db.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    @Query("SELECT * FROM chat_conversations ORDER BY updatedAt DESC")
    fun getAllConversations(): Flow<List<ChatConversationEntity>>

    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesByConversation(conversationId: Long): Flow<List<ChatMessageEntity>>

    @Insert
    suspend fun insertConversation(conversation: ChatConversationEntity): Long

    @Insert
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_conversations WHERE id = :conversationId")
    suspend fun deleteConversation(conversationId: Long)

    @Query("UPDATE chat_conversations SET title = :title WHERE id = :id")
    suspend fun updateConversationTitle(id: Long, title: String)

    @Query("UPDATE chat_conversations SET updatedAt = :timestamp WHERE id = :id")
    suspend fun updateConversationTimestamp(id: Long, timestamp: Long = System.currentTimeMillis())
}
