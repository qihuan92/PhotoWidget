package com.qihuan.photowidget

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import com.qihuan.photowidget.analysis.EventStatistics
import com.qihuan.photowidget.analysis.EventStatistics.trackLifecycle


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
        AppCenter.start(
            this,
            BuildConfig.APP_CENTER_SECRET,
            Analytics::class.java,
            Crashes::class.java
        )

        registerActivityLifecycleCallbacks(PhotoWidgetActivityLifecycleCallbacks())
        trackLifecycle(EventStatistics.APPLICATION_ON_CREATE)
    }
}

class PhotoWidgetActivityLifecycleCallbacks : Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        activity.trackLifecycle(EventStatistics.ACTIVITY_ON_CREATE)
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
        activity.trackLifecycle(EventStatistics.ACTIVITY_ON_DESTROY)
    }
}