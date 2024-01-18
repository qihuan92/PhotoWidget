package com.qihuan.photowidget

import android.app.Application
import android.content.Context
import com.qihuan.photowidget.core.common.di.commonModule
import com.qihuan.photowidget.core.database.di.dbModule
import com.qihuan.photowidget.di.appModule
import com.qihuan.photowidget.feature.about.di.aboutModule
import com.qihuan.photowidget.feature.settings.di.settingsModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin


/**
 * App
 * @author qi
 * @since 4/2/21
 */
class App : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)

        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(
                appModule,
                commonModule,
                dbModule,
                aboutModule,
                settingsModule,
            )
        }
    }
}