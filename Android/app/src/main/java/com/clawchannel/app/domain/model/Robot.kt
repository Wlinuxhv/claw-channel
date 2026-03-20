package com.clawchannel.app.domain.model

data class Robot(
    val id: Long,
    val name: String,
    val avatar: String? = null,
    val status: RobotStatus = RobotStatus.OFFLINE,
    val lastMessage: String? = null,
    val lastMessageTime: Long? = null,
    val unreadCount: Int = 0,
    val description: String? = null,
    val capabilities: List<String> = emptyList()
)

enum class RobotStatus {
    ONLINE,    // 在线
    BUSY,      // 忙碌
    OFFLINE    // 离线
}