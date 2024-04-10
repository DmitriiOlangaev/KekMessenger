package com.demo.kekmessenger.data.messagesRepo

interface MessagesDataSource {

    suspend fun getChannels(): Result<List<String>>

    suspend fun getMessages(
        channel: String,
        lastKnownId: Int,
        fetchMessagesCount: Int
    ): Result<List<MessageTable>>
}