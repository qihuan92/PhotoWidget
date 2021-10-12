package com.qihuan.photowidget

import android.app.Application
import com.tencent.bugly.crashreport.CrashReport

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
        CrashReport.initCrashReport(this)
        CrashReport.setIsDevelopmentDevice(this, BuildConfig.DEBUG)
    }
}