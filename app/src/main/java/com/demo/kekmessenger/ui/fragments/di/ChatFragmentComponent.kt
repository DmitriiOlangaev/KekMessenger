package com.demo.kekmessenger.ui.fragments.di

import com.demo.kekmessenger.ui.fragments.ChatFragment
import com.demo.kekmessenger.viewModels.di.ChatFragmentModule
import dagger.BindsInstance
import dagger.Subcomponent

@Subcomponent(modules = [ChatFragmentModule::class])
@ChatFragmentScope
interface ChatFragmentComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create(@BindsInstance chatFragment: ChatFragment): ChatFragmentComponent
    }

    fun inject(chatFragment: ChatFragment)
}