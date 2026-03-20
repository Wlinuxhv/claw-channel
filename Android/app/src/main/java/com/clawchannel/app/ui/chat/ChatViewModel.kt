package com.clawchannel.app.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.clawchannel.app.data.remote.WebSocketManager
import com.clawchannel.app.data.remote.WebSocketManager.ConnectionState
import com.clawchannel.app.data.repository.MessageRepository
import com.clawchannel.app.domain.model.Message
import com.clawchannel.app.domain.model.MessageStatus
import com.clawchannel.app.domain.model.MessageType
import com.clawchannel.app.util.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    application: Application,
    private val messageRepository: MessageRepository,
    private val webSocketManager: WebSocketManager
) : AndroidViewModel(application) {
    
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState
    
    // 从 Repository 获取消息（Flow）
    val messages = messageRepository.messages
    
    init {
        observeConnection()
    }
    
    private fun observeConnection() {
        viewModelScope.launch {
            webSocketManager.connectionState.collect { state ->
                _uiState.value = _uiState.value.copy(connectionState = state)
            }
        }
    }
    
    /**
     * 发送文本消息
     */
    fun sendMessage(content: String) {
        if (content.isBlank()) return
        
        viewModelScope.launch {
            try {
                messageRepository.sendMessage(content)
            } catch (e: Exception) {
                // 发送失败，更新状态
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }
    
    /**
     * 发送图片消息
     */
    fun sendImageMessage(filePath: String, fileSize: Long) {
        viewModelScope.launch {
            try {
                messageRepository.sendImageMessage(filePath, fileSize)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }
    
    /**
     * 发送文件消息
     */
    fun sendFileMessage(filePath: String, fileName: String, fileSize: Long) {
        viewModelScope.launch {
            try {
                messageRepository.sendFileMessage(filePath, fileName, fileSize)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }
    
    /**
     * 接收来自 AI 的消息
     */
    fun receiveMessage(content: String, type: MessageType = MessageType.TEXT) {
        viewModelScope.launch {
            messageRepository.receiveMessage(content, type)
            
            // 显示通知（如果应用不在前台）
            // 注意：实际应用中需要检测应用是否在前台
            // 这里简化处理，每次收到消息都显示通知
            // showNotification(content)
        }
    }
    
    /**
     * 显示消息通知
     */
    private fun showNotification(message: String) {
        try {
            val context = getApplication<Application>().applicationContext
            NotificationHelper.showMessageNotification(
                context = context,
                messageId = System.currentTimeMillis().toInt(),
                title = "🦞 AI 助手",
                message = message
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 重发消息
     */
    fun resendMessage(messageId: Long) {
        viewModelScope.launch {
            messageRepository.resendMessage(messageId)
        }
    }
    
    /**
     * 清除所有消息
     */
    fun clearMessages() {
        viewModelScope.launch {
            messageRepository.clearAllMessages()
        }
    }
    
    /**
     * 退出登录
     */
    fun logout() {
        webSocketManager.disconnect()
    }
}

data class ChatUiState(
    val connectionState: ConnectionState = ConnectionState.Disconnected,
    val errorMessage: String? = null
)
