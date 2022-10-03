package com.qihuan.photowidget.core.common.ktx

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContract

fun Context.isIgnoringBatteryOptimizations(): Boolean {
    val powerManager = getSystemService(PowerManager::class.java)
    return powerManager?.isIgnoringBatteryOptimizations(applicationContext.packageName) ?: false
}

class IgnoringBatteryOptimizationsContract : ActivityResultContract<String?, Boolean>() {
    private var context: Context? = null

    @SuppressLint("BatteryLife")
    override fun createIntent(context: Context, input: String?): Intent {
        this.context = context
        return if (input == null || context.isIgnoringBatteryOptimizations()) {
            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        } else {
            Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:$input")
            }
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
        return context?.isIgnoringBatteryOptimizations() ?: false
    }
}