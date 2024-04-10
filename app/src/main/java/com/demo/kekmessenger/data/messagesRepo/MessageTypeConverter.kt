package com.demo.kekmessenger.data.messagesRepo

import androidx.room.TypeConverter

object MessageTypeConverter {
    @TypeConverter
    fun fromString(value: String): MessageType {
        return enumValueOf(value)
    }

    @TypeConverter
    fun fromMessagesTypes(type: MessageType): String {
        return type.name
    }
}
