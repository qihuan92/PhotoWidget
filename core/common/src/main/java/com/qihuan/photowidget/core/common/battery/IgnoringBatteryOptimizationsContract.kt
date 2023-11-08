package com.qihuan.photowidget.core.common.battery

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContract
import com.qihuan.photowidget.core.common.battery.impl.KeepServiceImpl

class IgnoringBatteryOptimizationsContract : ActivityResultContract<String?, Boolean>() {
    private var context: Context? = null
    private lateinit var keepService: KeepService

    @SuppressLint("BatteryLife")
    override fun createIntent(context: Context, input: String?): Intent {
        this.context = context
        keepService = KeepServiceImpl(context)
        return if (input == null || keepService.isIgnoringBatteryOptimizations()) {
            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        } else {
            Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:$input")
            }
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
        return keepService.isIgnoringBatteryOptimizations()
    }
}