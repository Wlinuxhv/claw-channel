package com.clawchannel.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    
    @Query("SELECT * FROM messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesBySession(sessionId: String): Flow<List<MessageEntity>>
    
    @Query("SELECT * FROM messages WHERE id = :id")
    suspend fun getMessageById(id: Long): MessageEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<MessageEntity>)
    
    @Update
    suspend fun update(message: MessageEntity)
    
    @Delete
    suspend fun delete(message: MessageEntity)
    
    @Query("DELETE FROM messages WHERE sessionId = :sessionId")
    suspend fun deleteAllForSession(sessionId: String)
    
    @Query("DELETE FROM messages")
    suspend fun deleteAll()
    
    @Query("SELECT COUNT(*) FROM messages WHERE sessionId = :sessionId")
    fun getCountForSession(sessionId: String): Flow<Int>
    
    @Query("SELECT * FROM messages WHERE sessionId = :sessionId AND status = 'SENDING' ORDER BY timestamp ASC")
    suspend fun getPendingMessages(sessionId: String): List<MessageEntity>
    
    @Query("UPDATE messages SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)
}
