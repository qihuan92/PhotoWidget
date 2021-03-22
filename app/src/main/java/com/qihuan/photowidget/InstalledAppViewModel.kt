package com.qihuan.photowidget

import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.ApplicationInfo
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.qihuan.photowidget.bean.InstalledAppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * InstalledAppViewModel
 * @author qi
 * @since 3/19/21
 */
class InstalledAppViewModel(application: Application) : AndroidViewModel(application) {
    private val packageManager by lazy { application.packageManager }
    val installedAppList by lazy { MutableLiveData<MutableList<InstalledAppInfo>>(mutableListOf()) }

    init {
        loadInstalledApp()
    }

    private fun loadInstalledApp() {
        viewModelScope.launch {
            installedAppList.value = getInstalledPackages()
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private suspend fun getInstalledPackages(): MutableList<InstalledAppInfo> {
        return withContext(Dispatchers.IO) {
            packageManager.getInstalledPackages(0)
                .filter {
                    it.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0
                }
                .map {
                    InstalledAppInfo(
                        it.applicationInfo.loadIcon(packageManager),
                        it.applicationInfo.loadLabel(packageManager).toString(),
                        it.packageName
                    )
                }.toMutableList()
        }
    }
}