package com.demo.kekmessenger.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ServerDao {
    @POST("messages")
    suspend fun postMessage(
        @Body requestBody: RequestBody
    ): Response<ResponseBody>

    @Multipart
    @POST("messages")
    suspend fun uploadImage(
        @Part jsonPart: MultipartBody.Part,
        @Part imagePart: MultipartBody.Part
    ): Response<ResponseBody>

    @GET("channels")
    suspend fun getChannels(): Response<ResponseBody>

    @GET("channel/{channel}")
    suspend fun getMessages(
        @Path("channel") channel: String,
        @Query("lastKnownId") lastKnownId: Int,
        @Query("limit") limit: Int,
        @Query("reverse") reverse: Boolean = false
    ): Response<ResponseBody>
}