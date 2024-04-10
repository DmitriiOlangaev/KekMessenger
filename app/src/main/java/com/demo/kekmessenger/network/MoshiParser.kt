package com.demo.kekmessenger.network

import com.squareup.moshi.Moshi
import java.lang.reflect.Type
import javax.inject.Inject

class MoshiParser @Inject constructor(private val moshi: Moshi) : Parser {

    override fun <T> fromJson(jsonString: String, type: Type): T? {
        val adapter = moshi.adapter<T>(type)
        return adapter.fromJson(jsonString)
    }

    override fun <T> toJson(value: T, type: Type): String {
        val adapter = moshi.adapter<T>(type)
        return adapter.toJson(value)
    }
}