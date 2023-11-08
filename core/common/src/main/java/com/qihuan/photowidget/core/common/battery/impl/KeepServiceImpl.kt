package com.qihuan.photowidget.core.common.battery.impl

import android.content.Context
import com.qihuan.photowidget.core.common.battery.KeepService
import com.qihuan.photowidget.core.common.battery.isIgnoringBatteryOptimizations

/**
 * 电池管理
 *
 * @author Qi
 * @since 2022/10/3
 */
class KeepServiceImpl(private val context: Context) : KeepService {

    override fun isIgnoringBatteryOptimizations(): Boolean {
        return context.isIgnoringBatteryOptimizations()
    }
}