package com.qihuan.photowidget.core.common.di

import com.qihuan.photowidget.core.common.battery.KeepService
import com.qihuan.photowidget.core.common.battery.impl.KeepServiceImpl
import org.koin.dsl.module

val commonModule = module {
    single<KeepService> { KeepServiceImpl(get()) }
}