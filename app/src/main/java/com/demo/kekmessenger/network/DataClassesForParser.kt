package com.demo.kekmessenger.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

object DataClassesForParser {

    @JsonClass(generateAdapter = true)
    data class JsonMessage(
        @Json(name = "id") val id: Int,
        @Json(name = "from") val from: String,
        @Json(name = "to") val to: String,
        @Json(name = "data") val data: JsonMessageData,
        @Json(name = "time") val time: Long
    )

    @JsonClass(generateAdapter = true)
    data class JsonMessageData(
        @Json(name = "Text") val text: JsonMessageText?,
        @Json(name = "Image") val image: JsonMessageImage?
    )

    @JsonClass(generateAdapter = true)
    data class JsonMessageText(
        @Json(name = "text") val text: String
    )

    @JsonClass(generateAdapter = true)
    data class JsonMessageImage(
        @Json(name = "link") val link: String
    )
}