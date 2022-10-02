package com.qihuan.photowidget.core.analysis

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.qihuan.photowidget.core.analysis.EventStatistics.trackLifecycle

class AppActivityLifecycleCallbacks : Application.ActivityLifecycleCallbacks {
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