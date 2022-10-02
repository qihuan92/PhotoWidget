package com.qihuan.photowidget.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.qihuan.photowidget.App
import com.qihuan.photowidget.R
import com.qihuan.photowidget.core.model.AutoRefreshInterval
import com.qihuan.photowidget.core.model.PhotoScaleType
import com.qihuan.photowidget.core.model.RadiusUnit
import com.qihuan.photowidget.ktx.isIgnoringBatteryOptimizations
import com.qihuan.photowidget.worker.JobManager
import com.qihuan.photowidget.worker.JobManager.JOB_ID_REFRESH_WIDGET_PERIODIC
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
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository by lazy { SettingsRepository(application) }
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
        isIgnoreBatteryOptimizations.value = getApplication<App>().isIgnoringBatteryOptimizations()
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

    fun getAutoRefreshIntervalDescription(item: AutoRefreshInterval): String {
        return getApplication<App>().run {
            getString(R.string.auto_refresh_widget_interval_description, getString(item.text))
        }
    }

    private fun startOrCancelRefreshTask(item: AutoRefreshInterval) {
        JobManager.cancelJob(getApplication(), JOB_ID_REFRESH_WIDGET_PERIODIC)
        if (item == AutoRefreshInterval.NONE) {
            return
        }
        JobManager.schedulePeriodicUpdateWidgetJob(getApplication(), item.value)
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