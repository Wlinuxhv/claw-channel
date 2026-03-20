package com.clawchannel.app.data.repository

import android.content.Context
import com.clawchannel.app.data.local.ClawChannelDatabase
import com.clawchannel.app.data.local.MessageEntity
import com.clawchannel.app.data.remote.WebSocketManager
import com.clawchannel.app.domain.model.Message
import com.clawchannel.app.domain.model.MessageStatus
import com.clawchannel.app.domain.model.MessageType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class MessageRepository(
    context: Context,
    private val webSocketManager: WebSocketManager
) {
    
    private val database = ClawChannelDatabase.getDatabase(context)
    private val messageDao = database.messageDao()
    
    // 当前会话 ID
    private var currentSessionId: String = UUID.randomUUID().toString()
    
    // 获取消息列表（从本地数据库）
    val messages: Flow<List<Message>> = messageDao.getMessagesBySession(currentSessionId)
        .map { entities -> entities.map { it.toDomainModel() } }
    
    // 获取消息数量
    val messageCount: Flow<Int> = messageDao.getCountForSession(currentSessionId)
    
    /**
     * 发送消息
     */
    suspend fun sendMessage(content: String, type: MessageType = MessageType.TEXT): Message {
        val message = Message(
            id = System.currentTimeMillis(),
            sessionId = currentSessionId,
            content = content,
            type = type,
            status = MessageStatus.SENDING,
            isFromMe = true,
            timestamp = System.currentTimeMillis()
        )
        
        // 保存到本地数据库
        saveMessage(message)
        
        // 通过 WebSocket 发送
        webSocketManager.sendMessage(content)
        
        return message
    }
    
    /**
     * 发送图片消息
     */
    suspend fun sendImageMessage(filePath: String, fileSize: Long): Message {
        val message = Message(
            id = System.currentTimeMillis(),
            sessionId = currentSessionId,
            content = "[图片]",
            type = MessageType.IMAGE,
            status = MessageStatus.SENDING,
            isFromMe = true,
            timestamp = System.currentTimeMillis(),
            filePath = filePath,
            fileSize = fileSize
        )
        
        saveMessage(message)
        webSocketManager.sendMessage("{\"type\":\"image\",\"path\":\"$filePath\"}")
        
        return message
    }
    
    /**
     * 发送文件消息
     */
    suspend fun sendFileMessage(filePath: String, fileName: String, fileSize: Long): Message {
        val message = Message(
            id = System.currentTimeMillis(),
            sessionId = currentSessionId,
            content = "[文件] $fileName",
            type = MessageType.FILE,
            status = MessageStatus.SENDING,
            isFromMe = true,
            timestamp = System.currentTimeMillis(),
            filePath = filePath,
            fileSize = fileSize
        )
        
        saveMessage(message)
        webSocketManager.sendMessage("{\"type\":\"file\",\"path\":\"$filePath\",\"name\":\"$fileName\"}")
        
        return message
    }
    
    /**
     * 保存消息到本地数据库
     */
    suspend fun saveMessage(message: Message) {
        messageDao.insert(message.toEntity())
    }
    
    /**
     * 更新消息状态
     */
    suspend fun updateMessageStatus(messageId: Long, status: MessageStatus) {
        messageDao.updateStatus(messageId, status.name)
    }
    
    /**
     * 接收来自 AI 的消息
     */
    suspend fun receiveMessage(content: String, type: MessageType = MessageType.TEXT) {
        val message = Message(
            id = System.currentTimeMillis(),
            sessionId = currentSessionId,
            content = content,
            type = type,
            status = MessageStatus.DELIVERED,
            isFromMe = false,
            timestamp = System.currentTimeMillis()
        )
        
        saveMessage(message)
    }
    
    /**
     * 清除所有消息
     */
    suspend fun clearAllMessages() {
        messageDao.deleteAll()
    }
    
    /**
     * 清除会话消息
     */
    suspend fun clearSessionMessages() {
        messageDao.deleteAllForSession(currentSessionId)
    }
    
    /**
     * 获取待发送的消息
     */
    suspend fun getPendingMessages(): List<Message> {
        return messageDao.getPendingMessages(currentSessionId)
            .map { it.toDomainModel() }
    }
    
    /**
     * 重发消息
     */
    suspend fun resendMessage(messageId: Long) {
        val message = messageDao.getMessageById(messageId) ?: return
        val updated = message.copy(status = "SENDING")
        messageDao.update(updated)
        
        webSocketManager.sendMessage(updated.content)
    }
}

// Entity 和 Domain Model 转换
fun Message.toEntity(): MessageEntity {
    return MessageEntity(
        id = id,
        sessionId = sessionId,
        content = content,
        type = type.name,
        status = status.name,
        isFromMe = isFromMe,
        timestamp = timestamp,
        filePath = filePath,
        fileSize = fileSize,
        replyTo = null
    )
}

fun MessageEntity.toDomainModel(): Message {
    return Message(
        id = id,
        sessionId = sessionId,
        content = content,
        type = MessageType.valueOf(type),
        status = MessageStatus.valueOf(status),
        isFromMe = isFromMe,
        timestamp = timestamp,
        filePath = filePath,
        fileSize = fileSize
    )
}
