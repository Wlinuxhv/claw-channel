package com.clawchannel.app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clawchannel.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState
    
    private val _navigateToChat = MutableStateFlow(false)
    val navigateToChat: StateFlow<Boolean> = _navigateToChat
    
    fun onRecommendationCodeChange(code: String) {
        _uiState.value = _uiState.value.copy(recommendationCode = code)
    }
    
    fun login() {
        val code = _uiState.value.recommendationCode
        if (code.isBlank() || code.length != 8) {
            _uiState.value = _uiState.value.copy(
                error = "请输入 8 位推荐码"
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            authRepository.login(code).fold(
                onSuccess = { tokens ->
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _navigateToChat.value = true
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "登录失败"
                    )
                }
            )
        }
    }
    
    fun onNavigateToChatConsumed() {
        _navigateToChat.value = false
    }
}

data class LoginUiState(
    val recommendationCode: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
