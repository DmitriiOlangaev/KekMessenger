package com.demo.kekmessenger.network

import java.lang.reflect.Type

interface Parser {
    fun <T> fromJson(jsonString: String, type: Type): T?

    fun <T> toJson(value: T, type: Type): String
}