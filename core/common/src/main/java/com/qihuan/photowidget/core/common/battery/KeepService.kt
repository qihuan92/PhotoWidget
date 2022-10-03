package com.qihuan.photowidget.core.common.battery

import android.content.Context
import android.os.PowerManager

/**
 * 电池管理
 *
 * @author Qi
 * @since 2022/10/3
 */
interface KeepService {
    fun isIgnoringBatteryOptimizations(): Boolean
}

fun Context.isIgnoringBatteryOptimizations(): Boolean {
    val powerManager = getSystemService(PowerManager::class.java)
    return powerManager?.isIgnoringBatteryOptimizations(applicationContext.packageName) ?: false
}