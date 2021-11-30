package com.qihuan.photowidget.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.qihuan.photowidget.App
import com.qihuan.photowidget.R
import com.qihuan.photowidget.common.AutoRefreshInterval
import com.qihuan.photowidget.common.WorkTags
import com.qihuan.photowidget.ktx.isIgnoringBatteryOptimizations
import com.qihuan.photowidget.worker.ForceUpdateWidgetWorker
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * SettingsViewModel
 * @author qi
 * @since 2021/11/8
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository by lazy { SettingsRepository(application) }
    val cacheSize = MutableLiveData("0.00KB")
    val autoRefreshInterval = MutableLiveData(AutoRefreshInterval.NONE)
    val isIgnoreBatteryOptimizations = MutableLiveData(false)

    init {
        viewModelScope.launch {
            loadIgnoreBatteryOptimizations()
            loadAutoRefreshInterval()
            loadCacheSize()
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

    fun updateAutoRefreshInterval(item: AutoRefreshInterval) {
        autoRefreshInterval.value = item
        repository.saveAutoRefreshInterval(item.value)
        startOrCancelRefreshTask(item)
    }

    fun getAutoRefreshIntervalDescription(item: AutoRefreshInterval): String {
        val format =
            getApplication<App>().getString(R.string.auto_refresh_widget_interval_description)
        return String.format(format, item.description)
    }

    private fun startOrCancelRefreshTask(item: AutoRefreshInterval) {
        if (item == AutoRefreshInterval.NONE) {
            WorkManager.getInstance(getApplication())
                .cancelAllWorkByTag(WorkTags.PERIODIC_REFRESH_WIDGET)
            return
        }

        val workRequest =
            PeriodicWorkRequestBuilder<ForceUpdateWidgetWorker>(item.value, TimeUnit.MILLISECONDS)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .addTag(WorkTags.PERIODIC_REFRESH_WIDGET)
                .build()

        WorkManager.getInstance(getApplication())
            .enqueueUniquePeriodicWork(
                WorkTags.PERIODIC_REFRESH_WIDGET,
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
    }
}