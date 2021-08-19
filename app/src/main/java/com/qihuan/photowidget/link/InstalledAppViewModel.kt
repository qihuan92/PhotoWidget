package com.qihuan.photowidget.link

import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.ApplicationInfo
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.qihuan.photowidget.bean.InstalledAppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * InstalledAppViewModel
 * @author qi
 * @since 3/19/21
 */
@FlowPreview
class InstalledAppViewModel(application: Application) : AndroidViewModel(application) {
    private val packageManager by lazy { application.packageManager }
    val installedAppList by lazy { MutableLiveData<MutableList<InstalledAppInfo>>(mutableListOf()) }
    val uiState by lazy { ObservableField(InstalledAppActivity.UIState.LOADING) }
    val showSystemApps by lazy { MutableLiveData(false) }
    val queryKeyWord by lazy { MutableStateFlow<String?>(null) }

    init {
        viewModelScope.launch {
            queryKeyWord.sample(500)
                .filter { it != null }
                .collect { installedAppList.value = getInstalledPackages() }
        }
    }

    fun loadInstalledApp() {
        viewModelScope.launch {
            uiState.set(InstalledAppActivity.UIState.LOADING)
            installedAppList.value = getInstalledPackages()
            uiState.set(InstalledAppActivity.UIState.SHOW_CONTENT)
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private suspend fun getInstalledPackages(): MutableList<InstalledAppInfo> {
        return withContext(Dispatchers.IO) {
            packageManager.getInstalledPackages(0)
                .filter {
                    if (showSystemApps.value == false) {
                        return@filter it.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0
                    }
                    return@filter true
                }
                .map {
                    InstalledAppInfo(
                        it.applicationInfo.loadIcon(packageManager),
                        it.applicationInfo.loadLabel(packageManager).toString(),
                        it.packageName
                    )
                }.filter {
                    val value = queryKeyWord.value
                    if (!value.isNullOrBlank()) {
                        return@filter (it.appName ?: "").contains(value)
                                || it.packageName.contains(value)
                    }
                    return@filter true
                }.toMutableList()
        }
    }
}