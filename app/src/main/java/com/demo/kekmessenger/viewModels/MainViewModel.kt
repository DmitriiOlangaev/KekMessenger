package com.demo.kekmessenger.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.demo.kekmessenger.data.imagesRepo.ImagesRepository
import com.demo.kekmessenger.data.messagesRepo.MessagesRepository
import com.demo.kekmessenger.data.preferencesRepo.UserPreferences.Theme
import com.demo.kekmessenger.data.preferencesRepo.UserPreferencesRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val imagesRepository: ImagesRepository,
    private val messagesRepository: MessagesRepository
) : ViewModel() {
    fun clearAll() {
        clearCachedImages()
        clearCachedMessages()
    }


    fun clearCachedImages() {
        viewModelScope.launch {
            imagesRepository.clearCachedImages()
        }
    }

    fun clearCachedMessages() {
        viewModelScope.launch { messagesRepository.clearCachedMessages() }
    }

    fun changeName(newName: String) {
        viewModelScope.launch {
            userPreferencesRepository.changeName(newName)
        }
    }

    fun changeTheme(newTheme: Theme) {
        viewModelScope.launch {
            userPreferencesRepository.changeTheme(newTheme)
        }
    }


}