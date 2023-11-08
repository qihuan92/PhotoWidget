package com.qihuan.photowidget.feature.settings.di

import com.qihuan.photowidget.feature.settings.repository.SettingsRepository
import com.qihuan.photowidget.feature.settings.viewmodel.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * 设置模块
 *
 * @author Qi
 * @since 2022/10/3
 */
val settingsModule = module {
    factory { SettingsRepository(get()) }
    viewModel { SettingsViewModel(get(), get(), get()) }
}