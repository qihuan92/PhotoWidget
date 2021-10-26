package com.qihuan.photowidget.config

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * ConfigureViewModelFactory
 * @author qi
 * @since 2021/10/21
 */
class ConfigureViewModelFactory(
    private val application: Application,
    private val appWidgetId: Int
) : ViewModelProvider.AndroidViewModelFactory(application) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ConfigureViewModel(application, appWidgetId) as T
    }
}