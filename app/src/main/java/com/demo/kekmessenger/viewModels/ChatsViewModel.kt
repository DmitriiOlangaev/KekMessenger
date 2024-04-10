package com.demo.kekmessenger.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demo.kekmessenger.data.messagesRepo.MessagesRepository
import com.demo.kekmessenger.exceptions.ApplicationException
import com.demo.kekmessenger.utils.UtilityFunctions.mapToApplicationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChatsViewModel @Inject constructor(
    private val messagesRepository: MessagesRepository,
    private val _channels: MutableStateFlow<List<String>>,
    private val _error: MutableStateFlow<ApplicationException?>
) :
    ViewModel() {
    val channels: StateFlow<List<String>> = _channels
    val error: Flow<ApplicationException?> = _error

    init {
        getChannels(true)
    }


    fun getChannels(requireFromServer: Boolean = false) {
        viewModelScope.launch(Dispatchers.Default) {
            val result = messagesRepository.getChannels(requireFromServer)
            if (result.isSuccess) {
                _channels.value = result.getOrThrow()
            } else {
                _error.value = result.exceptionOrNull()?.mapToApplicationException()
            }
        }
    }
}