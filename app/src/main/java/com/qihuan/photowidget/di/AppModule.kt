package com.qihuan.photowidget.di

import com.qihuan.photowidget.AppInfoImpl
import com.qihuan.photowidget.core.common.AppInfo
import org.koin.dsl.module

val appModule = module {
    single<AppInfo> { AppInfoImpl(get()) }
}