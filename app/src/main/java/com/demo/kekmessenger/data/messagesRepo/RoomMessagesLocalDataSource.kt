package com.demo.kekmessenger.data.messagesRepo

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RoomMessagesLocalDataSource @Inject constructor(
    private val messageDao: MessageDao,
) : MessagesLocalDataSource {


    override suspend fun getMessages(
        channel: String,
        lastKnownId: Int,
        fetchMessagesCount: Int
    ): Result<List<MessageTable>> = Result.runCatching {
        withContext(Dispatchers.IO) {
            messageDao.getMessages(
                channel,
                lastKnownId,
                fetchMessagesCount
            )
        }
    }


    @OptIn(DelicateCoroutinesApi::class)
    override suspend fun saveMessages(data: List<MessageTable>): Result<Unit> =
        Result.runCatching {
            GlobalScope.async(Dispatchers.IO) {
                messageDao.insertAllMessages(data)
            }.await()
        }

    @OptIn(DelicateCoroutinesApi::class)
    override suspend fun clearCachedMessages(): Result<Unit> =
        Result.runCatching {
            GlobalScope.async(Dispatchers.IO) {
                messageDao.deleteAllMessages()
            }.await()
        }

    override suspend fun getChannels(): Result<List<String>> =
        Result.runCatching { messageDao.getChannels() }

}