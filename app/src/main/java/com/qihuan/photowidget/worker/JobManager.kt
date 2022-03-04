package com.qihuan.photowidget.worker

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.core.os.persistableBundleOf
import com.qihuan.photowidget.ktx.logD
import com.qihuan.photowidget.ktx.logE

/**
 * JobManager
 * @author qi
 * @since 2022/3/3
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
object JobManager {

    private const val TAG = "JobManager"
    private const val JOB_OVERRIDE_DEADLINE = 1000L
    const val JOB_ID_REFRESH_WIDGET_ONE_TIME = 100
    const val JOB_ID_REFRESH_WIDGET_PERIODIC = 101
    const val JOB_ID_DELETE_WIDGET = 200

    fun cancelJob(context: Context, jobId: Int) {
        val jobScheduler = context.getSystemService(JobScheduler::class.java)
        jobScheduler.cancel(jobId)
    }

    fun cancelAllJob(context: Context) {
        val jobScheduler = context.getSystemService(JobScheduler::class.java)
        jobScheduler.cancelAll()
    }

    fun scheduleUpdateWidgetJob(
        context: Context,
        widgetIds: IntArray? = null,
    ) {
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

    fun schedulePeriodicUpdateWidgetJob(
        context: Context,
        intervalMillis: Long,
        widgetIds: IntArray? = null
    ) {
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

    fun scheduleDeleteWidgetJob(context: Context, widgetIds: IntArray? = null) {
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