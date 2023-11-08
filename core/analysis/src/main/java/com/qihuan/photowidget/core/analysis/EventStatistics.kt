package com.qihuan.photowidget.core.analysis

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.Flags
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import java.text.SimpleDateFormat
import java.util.*

/**
 * EventStatistics
 * @author qi
 * @since 2022/2/18
 */
object EventStatistics {
    private const val TAG = "EventStatistics"

    const val LOG_DEBUG = "LOG_DEBUG"
    const val LOG_INFO = "LOG_INFO"
    const val LOG_ERROR = "LOG_ERROR"

    private const val APPLICATION_ON_CREATE = "APPLICATION_ON_CREATE"
    const val ACTIVITY_ON_CREATE = "ACTIVITY_ON_CREATE"
    const val ACTIVITY_ON_DESTROY = "ACTIVITY_ON_DESTROY"

    @JvmStatic
    fun init(application: Application) {
        AppCenter.start(
            application,
            BuildConfig.APP_CENTER_SECRET,
            Analytics::class.java,
            Crashes::class.java
        )

        application.registerActivityLifecycleCallbacks(AppActivityLifecycleCallbacks())
        application.trackLifecycle(APPLICATION_ON_CREATE)
    }

    private fun Application.trackLifecycle(lifecycleName: String) {
        try {
            track(
                lifecycleName, mapOf(
                    "Manufacturer" to Build.MANUFACTURER,
                    "Product" to Build.PRODUCT,
                    "Brand" to Build.BRAND,
                    "Model" to Build.MODEL,
                    "Device" to Build.DEVICE,
                    "Version" to Build.VERSION.RELEASE,
                    "AndroidID" to Settings.System.getString(
                        contentResolver,
                        Settings.Secure.ANDROID_ID
                    ),
                    "Date" to getCurrentTime("yyyy-MM-dd"),
                    "IsIgnoringBatteryOptimizations" to isIgnoringBatteryOptimizations().toString(),
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "TrackAppLifecycleError:" + e.message, e)
        }
    }

    fun Activity.trackLifecycle(lifecycleName: String) {
        track(
            lifecycleName, mapOf(
                "Activity" to javaClass.name,
                "Date" to getCurrentTime("yyyy-MM-dd"),
            )
        )
    }

    fun track(name: String, properties: Map<String, String?>? = null) {
        Analytics.trackEvent(name, properties)
    }

    fun trackCritical(name: String, properties: Map<String, String?>? = null) {
        Analytics.trackEvent(name, properties, Flags.CRITICAL)
    }

    fun trackError(throwable: Throwable, properties: Map<String, String?>? = null) {
        Crashes.trackError(throwable, properties, null)
    }

    private fun getCurrentTime(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
        var dateStr = ""
        try {
            val calendar = Calendar.getInstance()
            val sdf = SimpleDateFormat(pattern, Locale.getDefault())
            dateStr = sdf.format(calendar.time)
        } catch (e: Exception) {
            Log.e(TAG, "时间转换异常", e)
        }
        return dateStr
    }

    private fun Context.isIgnoringBatteryOptimizations(): Boolean {
        val powerManager = getSystemService(PowerManager::class.java)
        return powerManager?.isIgnoringBatteryOptimizations(applicationContext.packageName) ?: false
    }
}