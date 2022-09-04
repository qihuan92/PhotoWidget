package com.qihuan.photowidget.config

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.qihuan.photowidget.common.WidgetType

/**
 * ConfigureViewModelFactory
 * @author qi
 * @since 2021/10/21
 */
class ConfigureViewModelFactory(
    private val application: Application,
    private val appWidgetId: Int,
    private val widgetType: WidgetType
) : ViewModelProvider.AndroidViewModelFactory(application) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ConfigureViewModel(application, appWidgetId, widgetType) as T
    }
}