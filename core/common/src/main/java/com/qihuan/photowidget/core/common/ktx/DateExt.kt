package com.qihuan.photowidget.core.common.ktx

import java.text.SimpleDateFormat
import java.util.*

/**
 * DateExt
 * @author qi
 * @since 3/29/21
 */
fun Long?.toDateStr(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
    if (this == null) {
        return ""
    }
    var dateStr = ""
    try {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        dateStr = sdf.format(this)
    } catch (e: Exception) {
        logE("DateExt.kt", "时间转换异常", e)
    }
    return dateStr
}

fun getCurrentTime(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
    var dateStr = ""
    try {
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        dateStr = sdf.format(calendar.time)
    } catch (e: Exception) {
        logE("DateExt.kt", "时间转换异常", e)
    }
    return dateStr
}