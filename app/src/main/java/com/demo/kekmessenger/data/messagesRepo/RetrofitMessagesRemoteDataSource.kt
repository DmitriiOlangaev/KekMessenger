package com.demo.kekmessenger.data.messagesRepo

import com.demo.kekmessenger.data.RepoUtilities.toMessageTable
import com.demo.kekmessenger.exceptions.ServerInternetException
import com.demo.kekmessenger.network.DataClassesForParser
import com.demo.kekmessenger.network.Parser
import com.demo.kekmessenger.network.ServerDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Response
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import javax.inject.Inject

class RetrofitMessagesRemoteDataSource @Inject constructor(
    private val serverDao: ServerDao,
    private val parser: Parser
) : MessagesRemoteDataSource {
    override suspend fun getChannels(): Result<List<String>> =
        getDataFromNetwork { serverDao.getChannels() }

    override suspend fun getMessages(
        channel: String,
        lastKnownId: Int,
        fetchMessagesCount: Int
    ): Result<List<MessageTable>> = getDataFromNetwork<DataClassesForParser.JsonMessage> {
        serverDao.getMessages(
            channel,
            lastKnownId,
            fetchMessagesCount
        )
    }.let { res ->
        if (res.isSuccess) {
            Result.success(res.getOrThrow().map { jsonMessage -> jsonMessage.toMessageTable() })
        } else {
            Result.failure(res.exceptionOrNull()!!)
        }
    }

    private suspend inline fun <reified T> getDataFromNetwork(crossinline retrofitResponseSupplier: suspend () -> Response<ResponseBody>): Result<List<T>> =
        Result.runCatching {
            withContext(Dispatchers.IO) {
                val retrofitResponse = retrofitResponseSupplier()
                if (retrofitResponse.isSuccessful) {
                    val jsonString = retrofitResponse.body()?.charStream()?.use { it.readText() }
                        ?: throw ServerInternetException()
                    withContext(Dispatchers.Default) {
                        val data = parser.fromJson<List<T>>(jsonString, object : ParameterizedType {
                            override fun getActualTypeArguments(): Array<Type> =
                                arrayOf(T::class.java)

                            override fun getRawType(): Type = List::class.java

                            override fun getOwnerType(): Type? = null

                        })!!
                        data
                    }
                } else {
                    throw ServerInternetException(retrofitResponse.message())
                }
            }
        }
}