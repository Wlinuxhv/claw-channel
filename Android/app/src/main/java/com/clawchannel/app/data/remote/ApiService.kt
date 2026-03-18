package com.clawchannel.app.data.remote

import com.clawchannel.app.domain.model.AuthTokens
import com.clawchannel.app.domain.model.RecommendationCode
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthTokens>>
    
    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<ApiResponse<AuthTokens>>
    
    @GET("api/health")
    suspend fun healthCheck(): Response<ApiResponse<HealthStatus>>
    
    @POST("api/admin/login")
    suspend fun adminLogin(@Body request: AdminLoginRequest): Response<ApiResponse<AdminToken>>
    
    @POST("api/admin/recommendation-codes")
    @Headers("Authorization: Bearer {admin_token}")
    suspend fun generateRecommendationCode(): Response<ApiResponse<RecommendationCode>>
}

data class LoginRequest(
    val recommendation_code: String
)

data class RefreshTokenRequest(
    val refresh_token: String
)

data class AdminLoginRequest(
    val password: String
)

data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T?
)

data class HealthStatus(
    val clients: Int,
    val status: String,
    val timestamp: Long
)

data class AdminToken(
    val admin_token: String,
    val expires_in: Long
)
