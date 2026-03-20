package com.clawchannel.app.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.clawchannel.app.data.local.TokenStore
import com.clawchannel.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val recommendationCode: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isCheckingToken: Boolean = true  // 正在检查本地 token
)

class LoginViewModel(
    application: Application,
    private val authRepository: AuthRepository
) : AndroidViewModel(application) {
    
    private val tokenStore = TokenStore(application)
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    private val _navigateToChat = MutableStateFlow(false)
    val navigateToChat: StateFlow<Boolean> = _navigateToChat
    
    private val _navigateToAdmin = MutableStateFlow(false)
    val navigateToAdmin: StateFlow<Boolean> = _navigateToAdmin
    
    init {
        // 检查本地是否已有 token（自动登录）
        checkExistingToken()
    }
    
    private fun checkExistingToken() {
        viewModelScope.launch {
            val token = tokenStore.getAccessToken()
            if (token != null) {
                // 有本地 token，验证是否有效
                // 这里简化处理，直接导航到聊天界面
                _navigateToChat.value = true
            } else {
                // 没有 token，显示登录界面
                _uiState.value = _uiState.value.copy(isCheckingToken = false)
            }
        }
    }
    
    fun onRecommendationCodeChange(code: String) {
        _uiState.value = _uiState.value.copy(
            recommendationCode = code,
            error = null
        )
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
            val result = authRepository.login(code)
            
            result.fold(
                onSuccess = { tokens ->
                    // 保存 token 到本地
                    tokenStore.saveTokens(tokens.accessToken, tokens.refreshToken)
                    _navigateToChat.value = true
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "登录失败，请检查推荐码是否正确"
                    )
                }
            )
        }
    }
    
    fun onNavigateToChatConsumed() {
        _navigateToChat.value = false
        _uiState.value = _uiState.value.copy(isLoading = false, isCheckingToken = false)
    }
}