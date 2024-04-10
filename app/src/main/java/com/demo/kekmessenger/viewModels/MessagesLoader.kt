package com.demo.kekmessenger.viewModels

import android.util.Log
import com.demo.kekmessenger.data.messagesRepo.MessageTable
import com.demo.kekmessenger.data.messagesRepo.MessagesRepository
import com.demo.kekmessenger.utils.UtilityFunctions.mapToApplicationException
import com.demo.kekmessenger.viewModels.di.FetchMessagesCount
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MessagesLoader @AssistedInject constructor(
    private val messagesRepository: MessagesRepository,
    @Assisted private val coroutineScope: CoroutineScope,
    private val workDispatcher: CoroutineDispatcher,
    @Assisted private val channel: String,
    @FetchMessagesCount private val fetchMessagesCount: Int,
    private val _state: MutableStateFlow<InnerState<List<Message>>>,
) : Loader<List<Message>> {
    private val TAG = "MessagesLoader"

    override val state: Flow<Loader.State<List<Message>>> = _state.map { innerState ->
        Loader.State(
            data = innerState.currentData,
            isLoading = innerState.isLoading,
            error = innerState.error?.mapToApplicationException()
        )
    }

    data class InnerState<Data>(
        val currentData: Data,
        val startId: Int,
        val isLoading: Boolean,
        val isRefreshing: Boolean,
        val isLoadingAll: Boolean,
        val error: Throwable?,
    )

    fun load(reversed: Boolean = false) {
        Log.d(TAG, "Start load with reversed = $reversed")
        launchWork { innerState ->
            launchLoad(innerState, reversed)
        }
    }

    fun loadAll(reversed: Boolean = false) {
        launchWork { innerState ->
            innerState.copy(isLoadingAll = true, isRefreshing = false, error = null)
        }
        load(reversed)
    }


    fun refresh() {
        launchWork { innerState ->
            innerState.copy(isRefreshing = true, isLoadingAll = false, error = null)
        }
        load()
    }


    private fun launchLoad(
        innerState: InnerState<List<Message>>,
        reversed: Boolean
    ): InnerState<List<Message>> {
        Log.d(TAG, "launchLoad innerState = $innerState, reversed = $reversed")
        if (innerState.isLoading) {
            return innerState
        }
        coroutineScope.launch(workDispatcher) {
            val lastKnownId =
                if (innerState.currentData.isEmpty()) innerState.startId
                else if (reversed) innerState.currentData.first().metaData.id
                else innerState.currentData.last().metaData.id
            val result =
                messagesRepository.getMessages(
                    channel,
                    lastKnownId,
                    (if (reversed) -1 else 1) * fetchMessagesCount
                )
            onResultLoaded(result, reversed)
        }
        return innerState.copy(isLoading = true, error = null)
    }

    private fun onResultLoaded(result: Result<List<MessageTable>>, reversed: Boolean) {
        Log.d(TAG, "onResultLoaded, result = $result")
        launchWork { innerState ->
            if (result.isFailure) {
                Log.d(
                    TAG,
                    "onResultLoaded result.isFailure ${
                        result.exceptionOrNull()!!.stackTraceToString()
                    }"
                )
                onResultLoadedFailure(innerState, result)
            } else {
                onResultLoadedSuccess(result, innerState, reversed)
            }
        }
    }

    private fun onResultLoadedSuccess(
        result: Result<List<MessageTable>>,
        innerState: InnerState<List<Message>>,
        reversed: Boolean
    ): InnerState<List<Message>> {
        val messages = result.getOrThrow().map { messageTable -> messageTable.toMessage() }
        return if (innerState.isRefreshing) {
            onIsRefreshing(messages, innerState, reversed)
        } else {
            onIsLoading(messages, innerState, reversed)
        }
    }

    private fun onIsRefreshing(
        messages: List<Message>,
        innerState: InnerState<List<Message>>,
        reversed: Boolean
    ): InnerState<List<Message>> {
//        load(reversed)
//        return launchLoad(
//            innerState.copy(
//                currentData = listOf(),
//                isLoading = false,
//                isRefreshing = false
//            ), reversed
//        )
        return innerState.copy(
            currentData = messages,
            isLoading = false,
            isRefreshing = false,
            error = null
        )
    }

    private fun onIsLoading(
        messages: List<Message>,
        innerState: InnerState<List<Message>>,
        reversed: Boolean
    ): InnerState<List<Message>> {
        val newData = if (reversed) {
            messages + innerState.currentData
        } else {
            innerState.currentData + messages
        }
        val isLoadingAll = innerState.isLoadingAll && messages.size == fetchMessagesCount
        if (isLoadingAll) {
            load(reversed)
        }
        return innerState.copy(
            currentData = newData,
            isLoading = false,
            isLoadingAll = isLoadingAll
        )
    }

    private fun onResultLoadedFailure(
        innerState: InnerState<List<Message>>,
        result: Result<List<MessageTable>>
    ) = innerState.copy(
        error = result.exceptionOrNull(),
        isLoading = false,
        isRefreshing = false,
        isLoadingAll = false
    )

    private fun launchWork(block: (InnerState<List<Message>>) -> InnerState<List<Message>>) {
        coroutineScope.launch(workDispatcher) {
            _state.value = block(_state.value)
        }
    }

    private fun MessageTable.toMessage(): Message =
        Message(MessageMetaData(this.id, this.from, this.channel, this.type, this.time), this.data)
}