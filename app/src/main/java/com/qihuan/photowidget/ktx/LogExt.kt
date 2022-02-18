package com.qihuan.photowidget.ktx

import android.util.Log
import com.microsoft.appcenter.BuildConfig
import com.qihuan.photowidget.analysis.EventStatistics

/**
 * LogExt
 * @author qi
 * @since 4/7/21
 */
fun logD(tag: String, msg: String?) {
    Log.d(tag, msg.orEmpty())
    if (BuildConfig.DEBUG) {
        EventStatistics.track(EventStatistics.LOG_DEBUG, mapOf(tag to msg))
    }
}

fun logI(tag: String, msg: String?) {
    Log.i(tag, msg.orEmpty())
    EventStatistics.track(EventStatistics.LOG_INFO, mapOf(tag to msg))
}

fun logE(tag: String, msg: String?, throwable: Throwable? = null) {
    if (throwable != null) {
        Log.e(tag, msg, throwable)
        EventStatistics.trackError(throwable, mapOf(tag to msg))
    } else {
        Log.e(tag, msg.orEmpty())
        EventStatistics.trackCritical(EventStatistics.LOG_ERROR, mapOf(tag to msg))
    }
}