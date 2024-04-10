package com.demo.kekmessenger.viewModels

import android.graphics.Bitmap
import com.demo.kekmessenger.data.messagesRepo.MessageType
import com.demo.kekmessenger.exceptions.ServerInternetException
import com.demo.kekmessenger.network.DataClassesForParser
import com.demo.kekmessenger.network.Parser
import com.demo.kekmessenger.network.ServerDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.nio.ByteBuffer
import javax.inject.Inject

class MessageSender @Inject constructor(
    private val serverDao: ServerDao,
    private val parser: Parser,
    private val coroutineScope: CoroutineScope,
    private val workDispatcher: CoroutineDispatcher
) {

    private val mutex = Mutex()
    suspend fun send(message: Message, image: Bitmap? = null): Result<Unit> = Result.run {
        val jsonMessageData: DataClassesForParser.JsonMessageData = createJsonMessageData(message)
        val jsonMessage = createJsonMessage(message, jsonMessageData)
        val requestBody = parser.toJson(jsonMessage, DataClassesForParser.JsonMessage::class.java)
            .toRequestBody("application/json".toMediaType())
        val response = when (message.metaData.type) {
            MessageType.IMAGE -> {
                val imageRequestBody = image!!.toByteArray().let {
                    it.toRequestBody(
                        "image/${message.data.toFileType()}".toMediaType(),
                        0,
                        it.size
                    )
                }
                val jsonPart = MultipartBody.Part.createFormData("msg", "", requestBody)
                val imagePart =
                    MultipartBody.Part.createFormData("pic", message.data, imageRequestBody)
                coroutineScope.async(workDispatcher) {
                    mutex.withLock {
                        serverDao.uploadImage(jsonPart, imagePart)
                    }
                }.await()
            }

            MessageType.TEXT -> {
                coroutineScope.async(workDispatcher) {
                    mutex.withLock {
                        serverDao.postMessage(requestBody)
                    }
                }.await()
            }
        }
        if (response.isSuccessful) {
            success(Unit)
        } else {
            failure(ServerInternetException(response.message()))
        }
    }

    private fun createJsonMessage(
        message: Message,
        jsonMessageData: DataClassesForParser.JsonMessageData
    ) = DataClassesForParser.JsonMessage(
        message.metaData.id,
        message.metaData.sender,
        message.metaData.receiver,
        jsonMessageData,
        message.metaData.time
    )

    private fun createJsonMessageData(message: Message) =
        when (message.metaData.type) {
            MessageType.IMAGE -> {
                DataClassesForParser.JsonMessageData(
                    null,
                    DataClassesForParser.JsonMessageImage(message.data)
                )
            }

            MessageType.TEXT -> {
                DataClassesForParser.JsonMessageData(
                    DataClassesForParser.JsonMessageText(message.data),
                    null
                )
            }
        }

    private fun Bitmap.toByteArray(): ByteArray {
        val buffer = ByteBuffer.allocate(this.byteCount)
        this.copyPixelsToBuffer(buffer)
        buffer.rewind()
        return buffer.array()
    }

    private fun String.toFileType(): String = this.substringAfterLast(".", "png")
}