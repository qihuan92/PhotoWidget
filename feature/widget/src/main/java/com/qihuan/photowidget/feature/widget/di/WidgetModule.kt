package com.qihuan.photowidget.feature.widget.di

import com.qihuan.photowidget.feature.widget.data.repository.WidgetFrameRepository
import com.qihuan.photowidget.feature.widget.domain.usecase.SaveWidgetUseCase
import com.qihuan.photowidget.feature.widget.viewmodel.ConfigureViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val widgetModule = module {
    factory { WidgetFrameRepository(get()) }
    factory { SaveWidgetUseCase(get(), get()) }

    viewModel { parameters ->
        ConfigureViewModel(
            parameters.get(),
            parameters.get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
}