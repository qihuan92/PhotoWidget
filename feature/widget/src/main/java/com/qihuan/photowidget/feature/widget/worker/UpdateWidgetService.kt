package com.qihuan.photowidget.feature.widget.worker

import android.app.job.JobParameters
import android.appwidget.AppWidgetManager
import com.qihuan.photowidget.core.common.ktx.logD
import com.qihuan.photowidget.core.common.ktx.logE
import com.qihuan.photowidget.core.database.AppDatabase
import com.qihuan.photowidget.feature.widget.updateAppWidget

/**
 * UpdateWidgetService
 *
 * @author qi
 * @since 2022/3/2
 */
class UpdateWidgetService : CoroutineJobService() {

    companion object {
        private const val TAG = "UpdateWidgetService"
    }

    override suspend fun startJob(params: JobParameters?) {
        try {
            val widgetIds = params?.extras?.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS)
            logD(TAG, "Update widget start, widgetIds=${widgetIds?.joinToString().orEmpty()}.")
            val widgetDao = AppDatabase.getDatabase(application).widgetDao()
            val widgets = if (widgetIds != null && widgetIds.isNotEmpty()) {
                widgetDao.selectListByIds(widgetIds)
            } else {
                widgetDao.selectList()
            }
            if (widgets.isNotEmpty()) {
                widgets.forEach {
                    updateAppWidget(it)
                }
                logD(TAG, "Update widget success.")
            } else {
                logD(TAG, "Widget list is empty.")
            }
        } catch (e: Throwable) {
            logE(TAG, "Update widget error: " + e.message, e)
        }
    }
}