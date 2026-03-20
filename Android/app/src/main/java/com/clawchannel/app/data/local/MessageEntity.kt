package com.clawchannel.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: Long = 0,
    val sessionId: String = "",
    val content: String = "",
    val type: String = "TEXT", // TEXT, IMAGE, VOICE, FILE
    val status: String = "SENDING", // SENDING, SENT, DELIVERED, FAILED
    val isFromMe: Boolean = true,
    val timestamp: Long = System.currentTimeMillis(),
    val filePath: String? = null,
    val fileSize: Long? = null,
    val replyTo: Long? = null // 回复的消息 ID
)
