package com.qihuan.photowidget.core.common

/**
 * 任务管理
 *
 * @author Qi
 * @since 2022/10/3
 */
interface JobManager {
    companion object {
        const val JOB_OVERRIDE_DEADLINE = 1000L
        const val JOB_ID_REFRESH_WIDGET_ONE_TIME = 100
        const val JOB_ID_REFRESH_WIDGET_PERIODIC = 101
        const val JOB_ID_DELETE_WIDGET = 200
    }

    fun cancelJob(jobId: Int)
    fun cancelAllJob()
    fun scheduleUpdateWidgetJob(widgetIds: IntArray? = null)
    fun schedulePeriodicUpdateWidgetJob(intervalMillis: Long, widgetIds: IntArray? = null)
    fun scheduleDeleteWidgetJob(widgetIds: IntArray? = null)
}