package com.demo.kekmessenger.ui.activities.di

import com.demo.kekmessenger.ui.activities.MainActivity
import com.demo.kekmessenger.ui.fragments.di.ChangeNameFragmentComponent
import com.demo.kekmessenger.ui.fragments.di.ChatFragmentComponent
import com.demo.kekmessenger.ui.fragments.di.ChatsFragmentComponent
import com.demo.kekmessenger.ui.fragments.di.OpenImageFragmentComponent
import com.demo.kekmessenger.viewModels.di.MainActivityModule
import dagger.BindsInstance
import dagger.Module
import dagger.Subcomponent

@Subcomponent(
    modules = [
        MainActivityModule::class,
        SubcomponentsModule::class
    ]
)
@MainActivityScope
interface MainActivityComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create(@BindsInstance mainActivity: MainActivity): MainActivityComponent
    }

    fun chatsFragmentFactory(): ChatsFragmentComponent.Factory

    fun chatFragmentFactory(): ChatFragmentComponent.Factory

    fun changeNameFragmentFactory(): ChangeNameFragmentComponent.Factory

    fun openImageFragmentFactory(): OpenImageFragmentComponent.Factory

    fun inject(mainActivity: MainActivity)
}

@Module(
    subcomponents = [
        ChatsFragmentComponent::class,
        ChatFragmentComponent::class,
        ChangeNameFragmentComponent::class,
        OpenImageFragmentComponent::class
    ]
)
object SubcomponentsModule