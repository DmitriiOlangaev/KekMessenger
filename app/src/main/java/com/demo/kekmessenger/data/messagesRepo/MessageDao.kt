package com.demo.kekmessenger.data.messagesRepo

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MessageDao {
    @Query(
        "SELECT * FROM messages WHERE channel = :channel " +
                "AND CASE WHEN :fetchMessagesCount < 0 THEN id < :lastKnownId ELSE id > :lastKnownId END " +
                "ORDER BY id ASC LIMIT abs(:fetchMessagesCount)"
    )
    suspend fun getMessages(
        channel: String,
        lastKnownId: Int,
        fetchMessagesCount: Int
    ): List<MessageTable>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMessages(messages: List<MessageTable>)

    @Query("DELETE FROM messages WHERE channel = :channel")
    suspend fun deleteAllMessagesFromChannel(channel: String)

    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()

    @Query("SELECT DISTINCT channel FROM messages")
    suspend fun getChannels(): List<String>
}