package com.demo.kekmessenger.viewModels

import com.demo.kekmessenger.data.messagesRepo.MessageType

data class MessageMetaData(
    val id: Int,
    val sender: String,
    val receiver: String,
    val type: MessageType,
    val time: Long
)
