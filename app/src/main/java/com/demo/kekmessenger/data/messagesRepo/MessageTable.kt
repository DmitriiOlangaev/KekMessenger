package com.demo.kekmessenger.data.messagesRepo

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageTable(
    @PrimaryKey
    val id: Int,
    val from: String,
    val channel: String,
    val type: MessageType,
    val data: String,
    val time: Long,
)
