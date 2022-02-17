package com.qihuan.photowidget.ktx

import android.util.Log
import com.microsoft.appcenter.Flags
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes

/**
 * LogExt
 * @author qi
 * @since 4/7/21
 */
fun logD(tag: String, msg: String?) {
    Log.d(tag, msg.orEmpty())
}

fun logI(tag: String, msg: String?) {
    Log.i(tag, msg.orEmpty())
    Analytics.trackEvent(
        "LOG_INFO",
        mapOf(tag to msg)
    )
}

fun logE(tag: String, msg: String?, throwable: Throwable? = null) {
    if (throwable != null) {
        Log.e(tag, msg, throwable)
        Crashes.trackError(throwable, mapOf(tag to msg), null)
    } else {
        Log.e(tag, msg.orEmpty())
        Analytics.trackEvent(
            "LOG_ERROR",
            mapOf(tag to msg),
            Flags.CRITICAL
        )
    }
}