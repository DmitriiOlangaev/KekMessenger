package com.demo.kekmessenger.app.di

import android.content.Context
import com.demo.kekmessenger.ui.activities.di.MainActivityComponent
import com.demo.kekmessenger.viewModels.di.ViewModelBuilderModule
import dagger.BindsInstance
import dagger.Component
import dagger.Module

@Component(
    modules = [
        ViewModelBuilderModule::class,
        ApplicationModule::class,
        SubComponentsModule::class
    ]
)
@ApplicationScope
interface ApplicationComponent {
    @Component.Factory
    interface Factory {
        fun create(@BindsInstance applicationContext: Context): ApplicationComponent
    }

    fun mainActivityComponentFactory(): MainActivityComponent.Factory
}

@Module(
    subcomponents = [
        MainActivityComponent::class
    ]
)
object SubComponentsModule