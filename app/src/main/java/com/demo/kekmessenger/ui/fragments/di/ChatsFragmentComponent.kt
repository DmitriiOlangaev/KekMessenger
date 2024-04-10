package com.demo.kekmessenger.ui.fragments.di

import com.demo.kekmessenger.ui.fragments.ChatsFragment
import com.demo.kekmessenger.viewModels.di.ChatsFragmentModule
import dagger.BindsInstance
import dagger.Subcomponent

@Subcomponent(modules = [ChatsFragmentModule::class])
@ChatsFragmentScope
interface ChatsFragmentComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create(@BindsInstance chatsFragment: ChatsFragment): ChatsFragmentComponent
    }

    fun inject(chatsFragment: ChatsFragment)
}