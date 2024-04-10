package com.demo.kekmessenger.utils

import android.content.Context
import com.demo.kekmessenger.R
import com.demo.kekmessenger.exceptions.AccessStorageException
import com.demo.kekmessenger.exceptions.ApplicationException
import com.demo.kekmessenger.exceptions.ClientInternetException
import com.demo.kekmessenger.exceptions.ServerInternetException
import java.io.IOException

object UtilityFunctions {
    private fun Throwable.isJavaNetException(): Boolean =
        this::class.java.`package`?.toString()?.startsWith("package java.net") == true

    fun Throwable.mapToApplicationException(): ApplicationException? =
        when {
            isJavaNetException() -> if (this is java.net.ConnectException) ServerInternetException(
                this
            ) else ClientInternetException(this)

            this is ApplicationException -> this
            this is IOException -> AccessStorageException(this)
            else -> null
        }

    fun ApplicationException.errorMessage(context: Context): String =
        when (this) {
            is AccessStorageException -> context.resources.getString(R.string.access_error)
            is ClientInternetException -> context.resources.getString(R.string.client_internet_error)
            is ServerInternetException -> context.resources.getString(R.string.server_internet_error)
        }
}