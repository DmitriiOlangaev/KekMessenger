package com.demo.kekmessenger.viewModels

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demo.kekmessenger.data.imagesRepo.ImagesRepository
import com.demo.kekmessenger.data.messagesRepo.MessageType
import com.demo.kekmessenger.data.preferencesRepo.UserPreferencesRepository
import com.demo.kekmessenger.exceptions.ApplicationException
import com.demo.kekmessenger.utils.UtilityFunctions.mapToApplicationException
import com.demo.kekmessenger.viewModels.di.MessagesLoaderFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel @AssistedInject constructor(
    messagesLoaderFactory: MessagesLoaderFactory,
    private val messageSender: MessageSender,
    private val imagesRepository: ImagesRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    @Assisted private val channel: String,
    private val _error: MutableStateFlow<ApplicationException?>,
    private val _messages: MutableStateFlow<List<Message>>,
    private val _isLoadingMessages: MutableStateFlow<Boolean>
) : ViewModel() {
    val error: StateFlow<ApplicationException?> = _error
    val name: StateFlow<String> =
        userPreferencesRepository.userPreferencesFlow.map { userPreferences ->
            when (userPreferences.name) {
                "" -> "No name"
                else -> userPreferences.name
            }
        }.stateIn(viewModelScope, SharingStarted.Lazily, "")
    val messages: StateFlow<List<Message>> = _messages
    val isLoadingMessages: StateFlow<Boolean> = _isLoadingMessages
    private val messagesLoader = messagesLoaderFactory.create(channel, viewModelScope)

    init {
        startCollectingUserPreferences()
        startCollectingMessages()
        load(false)
    }

    fun load(reversed: Boolean) {
        messagesLoader.load(reversed)
    }

    fun loadAll(reversed: Boolean) {
        messagesLoader.loadAll(reversed)
    }

    fun refresh() {
        messagesLoader.refresh()
    }

    fun errorHandled() {
        viewModelScope.launch {
            _error.value = null
        }
    }

    suspend fun load(key: String): Result<Drawable> {
        return imagesRepository.getImage("thumb/$key")
    }


    fun send(text: String) {
        send(
            Message(
                MessageMetaData(
                    0,
                    name.value,
                    channel,
                    MessageType.TEXT,
                    0
                ), text
            )
        )
    }

    fun send(image: Bitmap) {
        send(
            Message(
                MessageMetaData(
                    0,
                    name.value,
                    channel,
                    MessageType.IMAGE,
                    0
                ), System.currentTimeMillis().toString() + ".png"
            ),
            image
        )
    }

    private fun send(message: Message, image: Bitmap? = null) {
        viewModelScope.launch {
            val result = messageSender.send(message, image)
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.mapToApplicationException()
            }
        }
    }


    private fun startCollectingUserPreferences() {
        viewModelScope.launch(Dispatchers.IO) {
            userPreferencesRepository.error.collect {
                _error.value = it?.mapToApplicationException()
            }
        }
    }

    private fun startCollectingMessages() {
        viewModelScope.launch(Dispatchers.Default) {
            messagesLoader.state.collect {
                _isLoadingMessages.value = it.isLoading
                _error.value = it.error
                _messages.value = it.data
            }
        }
    }


}