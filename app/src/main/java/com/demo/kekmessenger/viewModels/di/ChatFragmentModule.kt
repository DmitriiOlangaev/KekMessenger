package com.demo.kekmessenger.viewModels.di

import com.demo.kekmessenger.exceptions.ApplicationException
import com.demo.kekmessenger.viewModels.Message
import com.demo.kekmessenger.viewModels.MessagesLoader
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow

@Module
interface ChatFragmentModule {
    companion object {
        @Provides
        fun messagesMutableStateFlow(): MutableStateFlow<List<Message>> = MutableStateFlow(listOf())

        @Provides
        fun errorMutableStateFlow(): MutableStateFlow<ApplicationException?> =
            MutableStateFlow(null)

        @Provides
        fun isLoadingMutableStateFlow(): MutableStateFlow<Boolean> = MutableStateFlow(false)

        @Provides
        @FetchMessagesCount
        fun fetchMessagesCount(): Int = 40

        @Provides
        fun loaderInnerStateMutableStateFlow(): MutableStateFlow<MessagesLoader.InnerState<List<Message>>> =
            MutableStateFlow(
                MessagesLoader.InnerState(
                    listOf(),
                    0,
                    isLoading = false,
                    isRefreshing = false,
                    isLoadingAll = false,
                    null
                )
            )

        @OptIn(ExperimentalCoroutinesApi::class)
        @Provides
        fun workDispatcher(): CoroutineDispatcher = Dispatchers.IO.limitedParallelism(1)

    }

//    @Binds
//    @IntoMap
//    @ViewModelKey(ChatViewModel::class)
//    fun bindViewModel(viewModel: ChatViewModel) : ViewModel
}