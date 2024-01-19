package com.qihuan.photowidget.di

import com.qihuan.photowidget.AppInfoImpl
import com.qihuan.photowidget.core.common.AppInfo
import com.qihuan.photowidget.core.common.JobManager
import com.qihuan.photowidget.feature.widget.worker.JobManagerImpl
import org.koin.dsl.module

val appModule = module {
    single<AppInfo> { AppInfoImpl(get()) }
    single<JobManager> { com.qihuan.photowidget.feature.widget.worker.JobManagerImpl(get()) }
}