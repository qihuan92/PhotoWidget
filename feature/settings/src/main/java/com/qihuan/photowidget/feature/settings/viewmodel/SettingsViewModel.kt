package com.qihuan.photowidget.feature.settings.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qihuan.photowidget.core.common.JobManager
import com.qihuan.photowidget.core.common.JobManager.Companion.JOB_ID_REFRESH_WIDGET_PERIODIC
import com.qihuan.photowidget.core.common.battery.KeepService
import com.qihuan.photowidget.core.model.AutoRefreshInterval
import com.qihuan.photowidget.core.model.PhotoScaleType
import com.qihuan.photowidget.core.model.RadiusUnit
import com.qihuan.photowidget.feature.settings.repository.SettingsRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

/**
 * SettingsViewModel
 * @author qi
 * @since 2021/11/8
 */
@OptIn(FlowPreview::class)
class SettingsViewModel(
    private val repository: SettingsRepository,
    private val jobManager: JobManager,
    private val keepService: KeepService,
) : ViewModel() {

    val cacheSize = MutableLiveData("0.00KB")
    val autoRefreshInterval = MutableLiveData(AutoRefreshInterval.NONE)
    val isIgnoreBatteryOptimizations = MutableLiveData(false)

    val widgetRadius = MutableStateFlow(0f)
    val widgetRadiusUnit = MutableLiveData(RadiusUnit.LENGTH)
    val widgetScaleType = MutableLiveData(PhotoScaleType.CENTER_CROP)

    init {
        viewModelScope.launch {
            loadIgnoreBatteryOptimizations()
            loadAutoRefreshInterval()
            loadCacheSize()
            loadWidgetDefaultConfig()

            // Save the widget default radius.
            widgetRadius.debounce(500).collect {
                repository.saveWidgetDefaultRadius(it, widgetRadiusUnit.value ?: RadiusUnit.LENGTH)
            }
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            repository.clearCache()
            loadCacheSize()
        }
    }

    fun loadIgnoreBatteryOptimizations() {
        isIgnoreBatteryOptimizations.value = keepService.isIgnoringBatteryOptimizations()
    }

    private suspend fun loadCacheSize() {
        cacheSize.value = repository.getCacheSize()
    }

    private suspend fun loadAutoRefreshInterval() {
        autoRefreshInterval.value = AutoRefreshInterval.get(repository.getAutoRefreshInterval())
    }

    private suspend fun loadWidgetDefaultConfig() {
        val (radius, unit) = repository.getWidgetDefaultRadius()
        widgetRadius.value = radius
        widgetRadiusUnit.value = unit

        val scaleType = repository.getWidgetDefaultScaleType()
        widgetScaleType.value = scaleType
    }

    fun updateAutoRefreshInterval(item: AutoRefreshInterval) {
        autoRefreshInterval.value = item
        repository.saveAutoRefreshInterval(item.value)
        startOrCancelRefreshTask(item)
    }

    private fun startOrCancelRefreshTask(item: AutoRefreshInterval) {
        jobManager.cancelJob(JOB_ID_REFRESH_WIDGET_PERIODIC)
        if (item == AutoRefreshInterval.NONE) {
            return
        }
        jobManager.schedulePeriodicUpdateWidgetJob(item.value)
    }

    fun updateRadiusUnit(item: RadiusUnit) {
        if (widgetRadiusUnit.value == item) {
            return
        }
        widgetRadius.value = 0f
        widgetRadiusUnit.value = item
    }

    fun updatePhotoScaleType(item: PhotoScaleType) {
        if (widgetScaleType.value == item) {
            return
        }
        widgetScaleType.value = item
        repository.saveWidgetDefaultScaleType(item)
    }
}