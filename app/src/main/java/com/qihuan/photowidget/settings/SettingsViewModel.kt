package com.qihuan.photowidget.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.qihuan.photowidget.App
import com.qihuan.photowidget.R
import com.qihuan.photowidget.bean.AutoRefreshInterval
import kotlinx.coroutines.launch

/**
 * SettingsViewModel
 * @author qi
 * @since 2021/11/8
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository by lazy { SettingsRepository(application) }
    val cacheSize = MutableLiveData("0.00KB")
    val autoRefreshInterval = MutableLiveData(AutoRefreshInterval.NONE)
    val autoRefreshDescription =
        MutableLiveData(application.getString(R.string.auto_refresh_widget_description))

    init {
        viewModelScope.launch {
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

    private suspend fun loadCacheSize() {
        cacheSize.value = repository.getCacheSize()
    }

    private suspend fun loadAutoRefreshInterval() {
        autoRefreshInterval.value = AutoRefreshInterval.get(repository.getAutoRefreshInterval())
    }

    fun updateAutoRefreshInterval(item: AutoRefreshInterval) {
        autoRefreshInterval.value = item
        repository.saveAutoRefreshInterval(item.value)
    }

    fun getAutoRefreshIntervalDescription(item: AutoRefreshInterval): String {
        val format =
            getApplication<App>().getString(R.string.auto_refresh_widget_interval_description)
        return String.format(format, item.description)
    }
}