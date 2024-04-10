package com.demo.kekmessenger.viewModels.di

import androidx.lifecycle.ViewModel
import com.demo.kekmessenger.viewModels.MainViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface MainActivityModule {
    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    fun bindViewModel(viewModel: MainViewModel): ViewModel
}