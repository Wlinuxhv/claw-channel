package com.clawchannel.app.domain.model

data class User(
    val id: Long = 0,
    val recommendationCode: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

data class Message(
    val id: Long = 0,
    val sessionId: String = "",
    val content: String = "",
    val type: MessageType = MessageType.TEXT,
    val status: MessageStatus = MessageStatus.SENDING,
    val isFromMe: Boolean = true,  // true = 我发送的，false = AI 回复的
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
    val isUsed: Boolean = false,  // 改名以符合 Kotlin 命名规范
    val createdAt: Long = 0  // 改为 Long 类型（时间戳）
)
