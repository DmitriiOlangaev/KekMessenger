package com.demo.kekmessenger.app

import android.app.Application
import com.demo.kekmessenger.app.di.ApplicationComponent
import com.demo.kekmessenger.app.di.DaggerApplicationComponent

open class KekMessengerApp : Application() {
    val applicationComponent: ApplicationComponent by lazy {
        initializeComponent()
    }

    open fun initializeComponent(): ApplicationComponent =
        DaggerApplicationComponent.factory().create(this)
}