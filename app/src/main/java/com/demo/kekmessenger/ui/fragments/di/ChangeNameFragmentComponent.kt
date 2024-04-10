package com.demo.kekmessenger.ui.fragments.di

import com.demo.kekmessenger.ui.fragments.ChangeNameFragment
import dagger.BindsInstance
import dagger.Subcomponent

@Subcomponent
@ChangeNameFragmentScope
interface ChangeNameFragmentComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create(@BindsInstance changeNameFragment: ChangeNameFragment): ChangeNameFragmentComponent
    }

    fun inject(changeNameFragment: ChangeNameFragment)
}