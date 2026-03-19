package com.clawchannel.app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val recommendationCode: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class LoginViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    private val _navigateToChat = MutableStateFlow(false)
    val navigateToChat: StateFlow<Boolean> = _navigateToChat
    
    fun onRecommendationCodeChange(code: String) {
        _uiState.value = _uiState.value.copy(recommendationCode = code)
    }
    
    fun login() {
        val code = _uiState.value.recommendationCode
        if (code.isBlank() || code.length != 8) {
            _uiState.value = _uiState.value.copy(
                error = "推荐码必须是 8 位"
            )
            return
        }
        
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            error = null
        )
        
        viewModelScope.launch {
            try {
                kotlinx.coroutines.delay(1000)
                _navigateToChat.value = true
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "登录失败"
                )
            }
        }
    }
    
    fun onNavigateToChatConsumed() {
        _navigateToChat.value = false
        _uiState.value = _uiState.value.copy(isLoading = false)
    }
}
