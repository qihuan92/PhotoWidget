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
import com.qihuan.photowidget.bean.TipsType
import com.qihuan.photowidget.bean.WidgetBean
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
                    tipList.value = getTipList()
                }
        }
    }

    private fun getWidgetList() = Pager(
        config = PagingConfig(pageSize = 20),
        pagingSourceFactory = { widgetDao.selectAll() }
    ).flow

    fun refreshTipList() {
        viewModelScope.launch {
            tipList.value = getTipList()
        }
    }

    private suspend fun getTipList(): MutableList<TipsType> {
        val widgetCount = widgetDao.selectWidgetCount()
        val tipList = mutableListOf<TipsType>()
        if (!getApplication<App>().isIgnoringBatteryOptimizations()) {
            tipList.add(TipsType.IGNORE_BATTERY_OPTIMIZATIONS)
        }
        if (widgetCount == 0) {
            tipList.add(TipsType.ADD_WIDGET)
        }
        return tipList
    }
}