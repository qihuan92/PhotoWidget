package com.qihuan.photowidget.ktx

import android.content.Context
import android.os.PowerManager

/**
 * KeepServiceExt
 * @author qi
 * @since 2021/10/11
 */
fun Context.isIgnoringBatteryOptimizations(): Boolean {
    val powerManager = getSystemService(PowerManager::class.java)
    return powerManager?.isIgnoringBatteryOptimizations(packageName) ?: false
}