package com.qihuan.photowidget.feature.settings.repository

import android.app.Application
import android.content.Context
import com.qihuan.photowidget.core.common.data.repository.SettingsRepository
import com.qihuan.photowidget.core.common.ktx.calculateFormatSizeRecursively
import com.qihuan.photowidget.core.common.ktx.logD
import com.qihuan.photowidget.core.model.INVALID_AUTO_REFRESH_INTERVAL
import com.qihuan.photowidget.core.model.KEY_AUTO_REFRESH_INTERVAL
import com.qihuan.photowidget.core.model.KEY_DEFAULT_WIDGET_RADIUS
import com.qihuan.photowidget.core.model.KEY_DEFAULT_WIDGET_RADIUS_UNIT
import com.qihuan.photowidget.core.model.KEY_DEFAULT_WIDGET_SCALE_TYPE
import com.qihuan.photowidget.core.model.PhotoScaleType
import com.qihuan.photowidget.core.model.RadiusUnit
import com.qihuan.photowidget.core.model.WidgetRadius
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * SettingsRepository
 * @author qi
 * @since 2021/11/8
 */
class SettingsRepositoryImpl(private val application: Application): SettingsRepository {

    private val sp by lazy { application.getSharedPreferences("settings", Context.MODE_PRIVATE) }

    override suspend fun clearCache() {
        withContext(Dispatchers.IO) {
            application.cacheDir.deleteRecursively()
        }
    }

    override suspend fun getCacheSize(): String {
        return withContext(Dispatchers.IO) {
            application.cacheDir.calculateFormatSizeRecursively()
        }
    }

    override fun saveAutoRefreshInterval(interval: Long) {
        sp.edit()
            .putLong(KEY_AUTO_REFRESH_INTERVAL, interval)
            .apply()
    }

    override fun clearAutoRefreshInterval() {
        sp.edit()
            .remove(KEY_AUTO_REFRESH_INTERVAL)
            .apply()
    }

    override suspend fun getAutoRefreshInterval(): Long {
        return withContext(Dispatchers.IO) {
            sp.getLong(KEY_AUTO_REFRESH_INTERVAL, INVALID_AUTO_REFRESH_INTERVAL)
        }
    }

    override fun saveWidgetDefaultRadius(radius: Float, unit: RadiusUnit) {
        logD("SettingsRepository", "保存微件默认圆角 ${radius}${unit.unitName}")
        sp.edit()
            .putFloat(KEY_DEFAULT_WIDGET_RADIUS, radius)
            .putString(KEY_DEFAULT_WIDGET_RADIUS_UNIT, unit.value)
            .apply()
    }

    override suspend fun getWidgetDefaultRadius(): WidgetRadius {
        return withContext(Dispatchers.IO) {
            val radius = sp.getFloat(KEY_DEFAULT_WIDGET_RADIUS, 0f)
            val unitValue = sp.getString(KEY_DEFAULT_WIDGET_RADIUS_UNIT, RadiusUnit.LENGTH.value)
                ?: RadiusUnit.LENGTH.value
            val unit = RadiusUnit.get(unitValue)
            WidgetRadius(radius, unit)
        }
    }

    override suspend fun getWidgetDefaultScaleType(): PhotoScaleType {
        return withContext(Dispatchers.IO) {
            val scaleTypeStr =
                sp.getString(KEY_DEFAULT_WIDGET_SCALE_TYPE, PhotoScaleType.CENTER_CROP.name)
                    ?: PhotoScaleType.CENTER_CROP.name
            enumValueOf(scaleTypeStr)
        }
    }

    override fun saveWidgetDefaultScaleType(item: PhotoScaleType) {
        sp.edit()
            .putString(KEY_DEFAULT_WIDGET_SCALE_TYPE, item.name)
            .apply()
    }
}