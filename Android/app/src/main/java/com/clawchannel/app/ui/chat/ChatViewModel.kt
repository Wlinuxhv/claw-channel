package com.clawchannel.app.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clawchannel.app.data.repository.AuthRepository
import com.clawchannel.app.domain.model.Message
import com.clawchannel.app.domain.model.MessageStatus
import com.clawchannel.app.domain.model.MessageType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState
    
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages
    
    init {
        observeMessages()
        observeConnection()
    }
    
    private fun observeMessages() {
        viewModelScope.launch {
            authRepository.messages.collect { rawMessages ->
                // 将原始消息转换为 Message 对象
                val newMessages = rawMessages.mapIndexed { index, msg ->
                    Message(
                        id = index.toLong(),
                        content = msg,
                        isFromAI = true,
                        status = MessageStatus.DELIVERED
                    )
                }
                _messages.value = newMessages
            }
        }
    }
    
    private fun observeConnection() {
        viewModelScope.launch {
            authRepository.connectionState.collect { state ->
                _uiState.value = _uiState.value.copy(connectionState = state)
            }
        }
    }
    
    fun sendMessage(content: String) {
        if (content.isBlank()) return
        
        // 添加用户消息到列表
        val userMessage = Message(
            id = System.currentTimeMillis(),
            content = content,
            isFromAI = false,
            status = MessageStatus.SENDING
        )
        
        val currentMessages = _messages.value.toMutableList()
        currentMessages.add(userMessage)
        _messages.value = currentMessages
        
        // 通过 WebSocket 发送
        authRepository.sendMessage(content)
        
        // 更新状态为已发送
        viewModelScope.launch {
            kotlinx.coroutines.delay(500)
            val updatedMessages = _messages.value.toMutableList()
            val index = updatedMessages.indexOfFirst { it.id == userMessage.id }
            if (index != -1) {
                updatedMessages[index] = userMessage.copy(status = MessageStatus.SENT)
                _messages.value = updatedMessages
            }
        }
    }
    
    fun logout() {
        authRepository.logout()
    }
}

data class ChatUiState(
    val connectionState: ConnectionState = ConnectionState.Disconnected
)

sealed class ConnectionState {
    object Connecting : ConnectionState()
    object Connected : ConnectionState()
    object Disconnected : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}
