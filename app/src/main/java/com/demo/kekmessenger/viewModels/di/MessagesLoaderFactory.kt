package com.demo.kekmessenger.viewModels.di

import com.demo.kekmessenger.viewModels.MessagesLoader
import dagger.assisted.AssistedFactory
import kotlinx.coroutines.CoroutineScope

@AssistedFactory
interface MessagesLoaderFactory {
    fun create(channel: String, coroutineScope: CoroutineScope): MessagesLoader
}