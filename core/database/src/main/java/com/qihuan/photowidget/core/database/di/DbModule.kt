package com.qihuan.photowidget.core.database.di

import com.qihuan.photowidget.core.database.AppDatabase
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

/**
 * 数据库模块
 *
 * @author Qi
 * @since 2022/10/2
 */
val dbModule = module {
    single { AppDatabase.getDatabase(androidApplication()) }

    single { get<AppDatabase>().widgetInfoDao() }
    single { get<AppDatabase>().widgetDao() }
    single { get<AppDatabase>().linkInfoDao() }
    single { get<AppDatabase>().widgetFrameResourceDao() }
}