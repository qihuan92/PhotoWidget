package com.qihuan.photowidget

import android.app.Application
import com.qihuan.photowidget.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin


/**
 * App
 * @author qi
 * @since 4/2/21
 */
class App : Application() {

    companion object {
        lateinit var context: Application
    }

    override fun onCreate() {
        super.onCreate()
        context = this

        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(appModule)
        }
    }
}