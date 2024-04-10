package com.demo.kekmessenger.ui.fragments.di

import com.demo.kekmessenger.ui.fragments.OpenImageFragment
import dagger.BindsInstance
import dagger.Subcomponent

@Subcomponent
@OpenImageFragmentScope
interface OpenImageFragmentComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create(@BindsInstance openImageFragment: OpenImageFragment): OpenImageFragmentComponent
    }

    fun inject(openImageFragment: OpenImageFragment)
}