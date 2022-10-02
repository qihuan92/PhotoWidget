package com.qihuan.photowidget.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.qihuan.photowidget.App
import com.qihuan.photowidget.bean.WidgetBean
import com.qihuan.photowidget.core.model.TipsType
import com.qihuan.photowidget.db.AppDatabase
import com.qihuan.photowidget.ktx.isIgnoringBatteryOptimizations
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * MainViewModel
 * @author qi
 * @since 3/29/21
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {
    val widgetPagingData by lazy { MutableLiveData<PagingData<WidgetBean>>() }
    val tipList by lazy { MutableLiveData<MutableList<TipsType>>(mutableListOf()) }
    private val widgetDao by lazy { AppDatabase.getDatabase(application).widgetDao() }

    init {
        loadWidgetList()
    }

    private fun loadWidgetList() {
        viewModelScope.launch {
            getWidgetList()
                .cachedIn(viewModelScope)
                .collect {
                    widgetPagingData.value = it
                }
        }
    }

    private fun getWidgetList() = Pager(
        config = PagingConfig(pageSize = 20),
        pagingSourceFactory = { widgetDao.selectAll() }
    ).flow

    fun loadIgnoreBatteryOptimizations() {
        val application = getApplication<App>()
        viewModelScope.launch {
            if (application.isIgnoringBatteryOptimizations()) {
                removeTip(TipsType.IGNORE_BATTERY_OPTIMIZATIONS)
            } else {
                addTip(TipsType.IGNORE_BATTERY_OPTIMIZATIONS)
            }
        }
    }

    fun loadAddWidgetTip(isWidgetEmpty: Boolean) {
        if (isWidgetEmpty) {
            addTip(TipsType.ADD_WIDGET)
        } else {
            removeTip(TipsType.ADD_WIDGET)
        }
    }

    private fun addTip(type: TipsType) {
        val list = tipList.value ?: mutableListOf()
        if (!list.contains(type)) {
            list.add(type)
            tipList.value = list
        }
    }

    private fun removeTip(type: TipsType) {
        val list = tipList.value ?: mutableListOf()
        if (list.contains(type)) {
            list.remove(type)
            tipList.value = list
        }
    }
}