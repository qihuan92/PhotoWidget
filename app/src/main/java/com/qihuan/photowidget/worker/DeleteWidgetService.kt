package com.qihuan.photowidget.worker

import android.app.job.JobParameters
import android.appwidget.AppWidgetManager
import com.qihuan.photowidget.deleteWidgets
import com.qihuan.photowidget.ktx.logD
import com.qihuan.photowidget.ktx.logE

/**
 * DeleteWidgetService
 *
 * @author qi
 * @since 2022/3/2
 */
class DeleteWidgetService : CoroutineJobService() {

    companion object {
        private const val TAG = "DeleteWidgetService"
    }

    override suspend fun startJob(params: JobParameters?): JobStatus {
        return try {
            val widgetIds = params?.extras?.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS)
            if (widgetIds != null && widgetIds.isNotEmpty()) {
                deleteWidgets(applicationContext, widgetIds)
                logD(TAG, "Delete widget success.")
            } else {
                logD(TAG, "Delete widget ids is empty.")
            }
            JobStatus.Success
        } catch (e: Throwable) {
            logE(TAG, "Delete widget error: " + e.message, e)
            JobStatus.Failure(e)
        }
    }
}