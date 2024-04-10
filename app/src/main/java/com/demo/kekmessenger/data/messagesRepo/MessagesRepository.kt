package com.demo.kekmessenger.data.messagesRepo

import com.demo.kekmessenger.data.RepoUtilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class MessagesRepository @Inject constructor(
    private val messagesLocalDataSource: MessagesLocalDataSource,
    private val messagesRemoteDataSource: MessagesRemoteDataSource,
    private val coroutineScope: CoroutineScope
) {

    suspend fun getChannels(requireFromServer: Boolean): Result<List<String>> {
        if (requireFromServer) {
            return messagesRemoteDataSource.getChannels()
        }
        return RepoUtilities.repoGet(
            { messagesLocalDataSource.getChannels() },
            { messagesRemoteDataSource.getChannels() })
    }


    suspend fun getMessages(
        channel: String,
        lastKnownId: Int,
        fetchMessagesCount: Int
    ): Result<List<MessageTable>> =
        RepoUtilities.repoGet(
            {
                messagesLocalDataSource.getMessages(
                    channel,
                    lastKnownId,
                    fetchMessagesCount
                )
            },
            { messagesRemoteDataSource.getMessages(channel, lastKnownId, fetchMessagesCount) },
            { it.getOrThrow().isEmpty() },
            { coroutineScope.launch { messagesLocalDataSource.saveMessages(it.getOrThrow()) } })


    suspend fun clearCachedMessages(): Result<Unit> = messagesLocalDataSource.clearCachedMessages()
}

