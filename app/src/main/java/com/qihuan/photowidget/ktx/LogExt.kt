package com.qihuan.photowidget.ktx

import com.tencent.bugly.crashreport.BuglyLog

/**
 * LogExt
 * @author qi
 * @since 4/7/21
 */
fun logI(tag: String, msg: String?) {
    BuglyLog.i(tag, msg)
}

fun logE(tag: String, msg: String?, throwable: Throwable? = null) {
    if (throwable != null) {
        BuglyLog.e(tag, msg, throwable)
    } else {
        BuglyLog.e(tag, msg)
    }
}