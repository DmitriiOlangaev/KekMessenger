package com.demo.kekmessenger.data.messagesRepo

interface MessagesLocalDataSource : MessagesDataSource {

    suspend fun saveMessages(data: List<MessageTable>): Result<Unit>
    suspend fun clearCachedMessages(): Result<Unit>

}