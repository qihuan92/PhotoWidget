package com.qihuan.photowidget.ktx

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContract
import com.qihuan.photowidget.App

fun Context.isIgnoringBatteryOptimizations(): Boolean {
    val powerManager = getSystemService(PowerManager::class.java)
    return powerManager?.isIgnoringBatteryOptimizations(applicationContext.packageName) ?: false
}

fun isIgnoringBatteryOptimizations(): Boolean {
    return App.context.isIgnoringBatteryOptimizations()
}

class IgnoringBatteryOptimizationsContract : ActivityResultContract<String?, Boolean>() {
    @SuppressLint("BatteryLife")
    override fun createIntent(context: Context, input: String?): Intent {
        return if (input == null || context.isIgnoringBatteryOptimizations()) {
            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        } else {
            Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:$input")
            }
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
        return isIgnoringBatteryOptimizations()
    }
}