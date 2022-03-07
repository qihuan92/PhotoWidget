package com.qihuan.photowidget.analysis

import android.app.Activity
import android.app.Application
import android.os.Build
import android.provider.Settings
import com.microsoft.appcenter.Flags
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import com.qihuan.photowidget.bean.WidgetBean
import com.qihuan.photowidget.ktx.getCurrentTime
import com.qihuan.photowidget.ktx.isIgnoringBatteryOptimizations
import com.qihuan.photowidget.ktx.logE

/**
 * EventStatistics
 * @author qi
 * @since 2022/2/18
 */
object EventStatistics {
    const val LOG_DEBUG = "LOG_DEBUG"
    const val LOG_INFO = "LOG_INFO"
    const val LOG_ERROR = "LOG_ERROR"

    const val APPLICATION_ON_CREATE = "APPLICATION_ON_CREATE"
    const val ACTIVITY_ON_CREATE = "ACTIVITY_ON_CREATE"
    const val ACTIVITY_ON_DESTROY = "ACTIVITY_ON_DESTROY"

    private const val WIDGET_SAVE = "WIDGET_SAVE"

    fun Application.trackLifecycle(lifecycleName: String) {
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
            logE("Application::trackLifecycle", "TrackAppLifecycleError:" + e.message, e)
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

    fun trackSaveWidget(widgetBean: WidgetBean) {
        try {
            track(
                WIDGET_SAVE, mapOf(
                    "LinkType" to widgetBean.linkInfo?.type?.value.toString(),
                    "LinkUri" to widgetBean.linkInfo?.link.toString(),
                    "WidgetType" to widgetBean.widgetInfo.widgetType.code,
                    "WidgetPaddingLeft" to widgetBean.widgetInfo.leftPadding.toString(),
                    "WidgetPaddingTop" to widgetBean.widgetInfo.topPadding.toString(),
                    "WidgetPaddingRight" to widgetBean.widgetInfo.rightPadding.toString(),
                    "WidgetPaddingBottom" to widgetBean.widgetInfo.bottomPadding.toString(),
                    "WidgetRadius" to widgetBean.widgetInfo.widgetRadius.toString() + widgetBean.widgetInfo.widgetRadiusUnit.unitName,
                    "WidgetTransparency" to widgetBean.widgetInfo.widgetTransparency.toString(),
                    "WidgetAutoPlayInterval" to widgetBean.widgetInfo.autoPlayInterval.interval.toString(),
                    "WidgetPhotoScaleType" to widgetBean.widgetInfo.photoScaleType.description,
                    "WidgetImageSize" to widgetBean.imageList.size.toString(),
                    "WidgetFrameType" to widgetBean.frame?.type?.name.toString(),
                    "WidgetFrameUri" to widgetBean.frame?.frameUri.toString(),
                    "WidgetFrameColor" to widgetBean.frame?.frameColor.toString(),
                )
            )
        } catch (e: Exception) {
            logE("SaveWidget", "TrackError:" + e.message, e)
        }
    }
}