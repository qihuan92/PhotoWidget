package com.qihuan.photowidget.feature.widget.worker

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.core.os.persistableBundleOf
import com.qihuan.photowidget.core.common.JobManager
import com.qihuan.photowidget.core.common.JobManager.Companion.JOB_ID_DELETE_WIDGET
import com.qihuan.photowidget.core.common.JobManager.Companion.JOB_ID_REFRESH_WIDGET_ONE_TIME
import com.qihuan.photowidget.core.common.JobManager.Companion.JOB_ID_REFRESH_WIDGET_PERIODIC
import com.qihuan.photowidget.core.common.JobManager.Companion.JOB_OVERRIDE_DEADLINE
import com.qihuan.photowidget.core.common.ktx.logD
import com.qihuan.photowidget.core.common.ktx.logE

/**
 * JobManager
 * @author qi
 * @since 2022/3/3
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class JobManagerImpl(private val context: Context) : JobManager {

    companion object {
        private const val TAG = "JobManager"
    }

    override fun cancelJob(jobId: Int) {
        val jobScheduler = context.getSystemService(JobScheduler::class.java)
        jobScheduler.cancel(jobId)
    }

    override fun cancelAllJob() {
        val jobScheduler = context.getSystemService(JobScheduler::class.java)
        jobScheduler.cancelAll()
    }

    override fun scheduleUpdateWidgetJob(widgetIds: IntArray?) {
        val serviceComponentName = ComponentName(context, UpdateWidgetService::class.java)
        val jobInfo = JobInfo.Builder(JOB_ID_REFRESH_WIDGET_ONE_TIME, serviceComponentName)
            .setExtras(persistableBundleOf(AppWidgetManager.EXTRA_APPWIDGET_IDS to widgetIds))
            .setOverrideDeadline(JOB_OVERRIDE_DEADLINE)
            .build()

        val jobScheduler = context.getSystemService(JobScheduler::class.java)

        try {
            val result = jobScheduler.schedule(jobInfo)
            logD(TAG, "scheduleUpdateWidgetJob() success: ${result.isSuccess()}")
        } catch (e: Exception) {
            logE(TAG, "scheduleUpdateWidgetJob() error:" + e.message, e)
        }
    }

    override fun schedulePeriodicUpdateWidgetJob(intervalMillis: Long, widgetIds: IntArray?) {
        val serviceComponentName = ComponentName(context, UpdateWidgetService::class.java)
        val jobInfo = JobInfo.Builder(JOB_ID_REFRESH_WIDGET_PERIODIC, serviceComponentName)
            .setExtras(persistableBundleOf(AppWidgetManager.EXTRA_APPWIDGET_IDS to widgetIds))
            .setPersisted(true)
            .setPeriodic(intervalMillis)
            .build()

        val jobScheduler = context.getSystemService(JobScheduler::class.java)

        try {
            val result = jobScheduler.schedule(jobInfo)
            logD(TAG, "schedulePeriodicUpdateWidgetJob() success: ${result.isSuccess()}")
        } catch (e: Exception) {
            logE(TAG, "schedulePeriodicUpdateWidgetJob() error:" + e.message, e)
        }
    }

    override fun scheduleDeleteWidgetJob(widgetIds: IntArray?) {
        val serviceComponentName = ComponentName(context, DeleteWidgetService::class.java)
        val jobInfo = JobInfo.Builder(JOB_ID_DELETE_WIDGET, serviceComponentName)
            .setExtras(persistableBundleOf(AppWidgetManager.EXTRA_APPWIDGET_IDS to widgetIds))
            .setOverrideDeadline(JOB_OVERRIDE_DEADLINE)
            .build()

        val jobScheduler = context.getSystemService(JobScheduler::class.java)

        try {
            val result = jobScheduler.schedule(jobInfo)
            logD(TAG, "scheduleDeleteWidgetJob() success: ${result.isSuccess()}")
        } catch (e: Exception) {
            logE(TAG, "scheduleDeleteWidgetJob() error:" + e.message, e)
        }
    }

    private fun Int.isSuccess(): Boolean {
        return this == JobScheduler.RESULT_SUCCESS
    }
}