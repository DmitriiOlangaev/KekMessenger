package com.demo.kekmessenger.viewModels.di

import com.demo.kekmessenger.viewModels.ChatViewModel
import dagger.assisted.AssistedFactory

@AssistedFactory
interface ChatViewModelFactory {
    fun create(channel: String): ChatViewModel
}