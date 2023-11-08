package com.qihuan.photowidget.feature.settings.repository

import android.app.Application
import android.content.Context
import com.qihuan.photowidget.core.common.ktx.calculateFormatSizeRecursively
import com.qihuan.photowidget.core.common.ktx.logD
import com.qihuan.photowidget.core.model.*
import com.qihuan.photowidget.feature.settings.model.WidgetRadius
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * SettingsRepository
 * @author qi
 * @since 2021/11/8
 */
class SettingsRepository(private val application: Application) {

    private val sp by lazy { application.getSharedPreferences("settings", Context.MODE_PRIVATE) }

    suspend fun clearCache() {
        withContext(Dispatchers.IO) {
            application.cacheDir.deleteRecursively()
        }
    }

    suspend fun getCacheSize(): String {
        return withContext(Dispatchers.IO) {
            application.cacheDir.calculateFormatSizeRecursively()
        }
    }

    fun saveAutoRefreshInterval(interval: Long) {
        sp.edit()
            .putLong(KEY_AUTO_REFRESH_INTERVAL, interval)
            .apply()
    }

    fun clearAutoRefreshInterval() {
        sp.edit()
            .remove(KEY_AUTO_REFRESH_INTERVAL)
            .apply()
    }

    suspend fun getAutoRefreshInterval(): Long {
        return withContext(Dispatchers.IO) {
            sp.getLong(KEY_AUTO_REFRESH_INTERVAL, INVALID_AUTO_REFRESH_INTERVAL)
        }
    }

    fun saveWidgetDefaultRadius(radius: Float, unit: RadiusUnit) {
        logD("SettingsRepository", "保存微件默认圆角 ${radius}${unit.unitName}")
        sp.edit()
            .putFloat(KEY_DEFAULT_WIDGET_RADIUS, radius)
            .putString(KEY_DEFAULT_WIDGET_RADIUS_UNIT, unit.value)
            .apply()
    }

    suspend fun getWidgetDefaultRadius(): WidgetRadius {
        return withContext(Dispatchers.IO) {
            val radius = sp.getFloat(KEY_DEFAULT_WIDGET_RADIUS, 0f)
            val unitValue = sp.getString(KEY_DEFAULT_WIDGET_RADIUS_UNIT, RadiusUnit.LENGTH.value)
                ?: RadiusUnit.LENGTH.value
            val unit = RadiusUnit.get(unitValue)
            WidgetRadius(radius, unit)
        }
    }

    suspend fun getWidgetDefaultScaleType(): PhotoScaleType {
        return withContext(Dispatchers.IO) {
            val scaleTypeStr =
                sp.getString(KEY_DEFAULT_WIDGET_SCALE_TYPE, PhotoScaleType.CENTER_CROP.name)
                    ?: PhotoScaleType.CENTER_CROP.name
            enumValueOf(scaleTypeStr)
        }
    }

    fun saveWidgetDefaultScaleType(item: PhotoScaleType) {
        sp.edit()
            .putString(KEY_DEFAULT_WIDGET_SCALE_TYPE, item.name)
            .apply()
    }
}