package com.demo.kekmessenger.data

import com.demo.kekmessenger.data.messagesRepo.MessageTable
import com.demo.kekmessenger.data.messagesRepo.MessageType
import com.demo.kekmessenger.exceptions.ServerInternetException
import com.demo.kekmessenger.network.DataClassesForParser

object RepoUtilities {

    suspend fun <T> repoGet(
        localResultSupplier: suspend () -> Result<T>,
        remoteResultSupplier: suspend () -> Result<T>,
        conditionToRemote: suspend (Result<T>) -> Boolean = { true },
        onRemoteSuccessResult: suspend (Result<T>) -> Unit = {}
    ): Result<T> {
        var localResult = localResultSupplier()
        if (localResult.isFailure || conditionToRemote(localResult)) {
            val remoteResult = remoteResultSupplier()
            if (remoteResult.isFailure) {
                localResult.exceptionOrNull()?.let {
                    remoteResult.exceptionOrNull()?.addSuppressed(it)
                }
            } else {
                onRemoteSuccessResult(remoteResult)
            }
            localResult = remoteResult
        }
        return localResult
    }

    fun DataClassesForParser.JsonMessage.toMessageTable(): MessageTable {
        val mt =
            if (this.data.image != null && this.data.text == null) MessageType.IMAGE else if (this.data.text != null) MessageType.TEXT else throw ServerInternetException(
                "Incorrect response $this"
            )
        return MessageTable(
            this.id,
            this.from,
            this.to,
            mt,
            this.data.image?.link ?: this.data.text!!.text,
            this.time
        )
    }
}