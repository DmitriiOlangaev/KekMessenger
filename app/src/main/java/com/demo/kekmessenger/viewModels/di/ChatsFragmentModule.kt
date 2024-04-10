package com.demo.kekmessenger.viewModels.di

import androidx.lifecycle.ViewModel
import com.demo.kekmessenger.exceptions.ApplicationException
import com.demo.kekmessenger.viewModels.ChatsViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import kotlinx.coroutines.flow.MutableStateFlow

@Module
interface ChatsFragmentModule {
    companion object {
        @Provides
        fun channelsMutableStateFlow(): MutableStateFlow<List<String>> = MutableStateFlow(listOf())

        @Provides
        fun errorMutableStateFlow(): MutableStateFlow<ApplicationException?> =
            MutableStateFlow(null)
    }

    @Binds
    @IntoMap
    @ViewModelKey(ChatsViewModel::class)
    fun bindViewModel(viewModel: ChatsViewModel): ViewModel
}