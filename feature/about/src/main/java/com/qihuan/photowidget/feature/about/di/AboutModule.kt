package com.qihuan.photowidget.feature.about.di

import com.qihuan.photowidget.feature.about.activity.AboutActivity
import com.qihuan.photowidget.feature.about.usecase.GetAppLinksUseCase
import com.qihuan.photowidget.feature.about.usecase.GetDeveloperLinksUseCase
import com.qihuan.photowidget.feature.about.usecase.GetLicensesUseCase
import org.koin.dsl.module

/**
 * 关于模块
 *
 * @author Qi
 * @since 2022/10/3
 */
val aboutModule = module {
    scope<AboutActivity> {
        scoped { GetAppLinksUseCase(get()) }
        scoped { GetDeveloperLinksUseCase(get()) }
        scoped { GetLicensesUseCase() }
    }
}
