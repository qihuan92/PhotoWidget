package com.qihuan.photowidget.core.common.data.repository

import com.qihuan.photowidget.core.model.PhotoScaleType
import com.qihuan.photowidget.core.model.RadiusUnit
import com.qihuan.photowidget.core.model.WidgetRadius

/**
 * SettingsRepository
 * @author qi
 * @since 2024/1/18
 */
interface SettingsRepository {
    suspend fun clearCache()
    suspend fun getCacheSize(): String
    fun saveAutoRefreshInterval(interval: Long)
    fun clearAutoRefreshInterval()
    suspend fun getAutoRefreshInterval(): Long
    fun saveWidgetDefaultRadius(radius: Float, unit: RadiusUnit)
    suspend fun getWidgetDefaultRadius(): WidgetRadius
    suspend fun getWidgetDefaultScaleType(): PhotoScaleType
    fun saveWidgetDefaultScaleType(item: PhotoScaleType)
}