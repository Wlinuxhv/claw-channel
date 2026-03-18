package com.clawchannel.app.data.repository

import com.clawchannel.app.data.remote.ApiService
import com.clawchannel.app.data.remote.LoginRequest
import com.clawchannel.app.data.remote.WebSocketManager
import com.clawchannel.app.domain.model.AuthTokens
import com.clawchannel.app.domain.model.Message
import com.clawchannel.app.domain.model.MessageStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val webSocketManager: WebSocketManager
) {
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState
    
    private var currentTokens: AuthTokens? = null
    
    suspend fun login(recommendationCode: String): Result<AuthTokens> {
        return try {
            val response = apiService.login(LoginRequest(recommendationCode))
            if (response.isSuccessful && response.body()?.data != null) {
                val tokens = response.body()!!.data!!
                currentTokens = tokens
                _authState.value = AuthState.Authenticated(tokens)
                
                // 连接 WebSocket
                webSocketManager.connect(tokens.accessToken)
                
                Result.success(tokens)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun logout() {
        webSocketManager.disconnect()
        currentTokens = null
        _authState.value = AuthState.Unauthenticated
    }
    
    fun sendMessage(message: String) {
        webSocketManager.sendMessage(message)
    }
    
    val connectionState = webSocketManager.connectionState
    val messages = webSocketManager.messages
    
    sealed class AuthState {
        object Unauthenticated : AuthState()
        data class Authenticated(val tokens: AuthTokens) : AuthState()
        data class Error(val message: String) : AuthState()
    }
}
