package com.demo.kekmessenger.app.di

import javax.inject.Qualifier

@Qualifier
annotation class Host(val hostMode: HostMode)
