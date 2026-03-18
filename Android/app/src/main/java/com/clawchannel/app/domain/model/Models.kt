package com.clawchannel.app.domain.model

data class User(
    val id: Long = 0,
    val recommendationCode: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

data class Message(
    val id: Long = 0,
    val userId: Long = 0,
    val content: String = "",
    val type: MessageType = MessageType.TEXT,
    val status: MessageStatus = MessageStatus.SENDING,
    val isFromAI: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val filePath: String? = null,
    val fileSize: Long? = null
)

enum class MessageType {
    TEXT,
    IMAGE,
    VOICE,
    FILE
}

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    FAILED
}

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val tokenType: String = "Bearer"
)

data class RecommendationCode(
    val id: Long = 0,
    val code: String = "",
    val isUsed: Boolean = false,
    val createdAt: Long = 0
)
