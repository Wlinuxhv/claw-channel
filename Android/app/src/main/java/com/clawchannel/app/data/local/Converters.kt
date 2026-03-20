package com.clawchannel.app.data.local

import androidx.room.TypeConverter
import com.clawchannel.app.domain.model.MessageStatus
import com.clawchannel.app.domain.model.MessageType

class Converters {
    
    @TypeConverter
    fun fromMessageType(value: MessageType): String {
        return value.name
    }
    
    @TypeConverter
    fun toMessageType(value: String): MessageType {
        return try {
            MessageType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            MessageType.TEXT
        }
    }
    
    @TypeConverter
    fun fromMessageStatus(value: MessageStatus): String {
        return value.name
    }
    
    @TypeConverter
    fun toMessageStatus(value: String): MessageStatus {
        return try {
            MessageStatus.valueOf(value)
        } catch (e: IllegalArgumentException) {
            MessageStatus.SENDING
        }
    }
}
