package com.qihuan.photowidget.ktx

import android.util.Log
import com.qihuan.photowidget.BuildConfig
import com.tencent.bugly.crashreport.BuglyLog

/**
 * LogExt
 * @author qi
 * @since 4/7/21
 */
fun logD(tag: String, msg: String?) {
    if (BuildConfig.DEBUG) {
        Log.d(tag, msg.orEmpty())
    } else {
        BuglyLog.d(tag, msg)
    }
}

fun logI(tag: String, msg: String?) {
    if (BuildConfig.DEBUG) {
        Log.i(tag, msg.orEmpty())
    } else {
        BuglyLog.i(tag, msg)
    }
}

fun logE(tag: String, msg: String?, throwable: Throwable? = null) {
    if (throwable != null) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, msg, throwable)
        } else {
            BuglyLog.e(tag, msg, throwable)
        }
    } else {
        if (BuildConfig.DEBUG) {
            Log.e(tag, msg.orEmpty())
        } else {
            BuglyLog.e(tag, msg)
        }
    }
}