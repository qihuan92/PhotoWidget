package com.qihuan.photowidget.worker

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.Build
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.qihuan.photowidget.R
import com.qihuan.photowidget.db.AppDatabase
import com.qihuan.photowidget.updateAppWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * ForceUpdateWidgetWorker
 * @author qi
 * @since 2021/10/19
 */
class ForceUpdateWidgetWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(appContext, workerParameters) {

    companion object {
        const val NOTIFICATION_ID = 1992
    }

    private val notificationManager = appContext.getSystemService(NotificationManager::class.java)

    override suspend fun doWork(): Result {
        val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
        val widgetDao = AppDatabase.getDatabase(applicationContext).widgetDao()
        val widgetList = withContext(Dispatchers.IO) {
            widgetDao.selectList()
        }
        widgetList.forEach {
            updateAppWidget(applicationContext, appWidgetManager, it)
        }
        return Result.success()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(NOTIFICATION_ID, createNotification())
    }

    private fun createNotification(): Notification {
        val channelId = getString(R.string.background_task_notification_channel_id)
        val channelName = getString(R.string.background_task_channel_name)
        val title = getString(R.string.force_refresh_widget_notification_title)
        val cancel = getString(R.string.force_refresh_widget_cancel_processing)

        // This PendingIntent can be used to cancel the Worker.
        val intent = WorkManager.getInstance(applicationContext).createCancelPendingIntent(id)

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setTicker(title)
            .setSmallIcon(R.drawable.ic_outline_image_24)
            .setOngoing(true)
            .setProgress(0, 0, true)
            .addAction(R.drawable.ic_round_delete_24, cancel, intent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(channelId, channelName).also {
                builder.setChannelId(it.id)
            }
        }
        return builder.build()
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, name: String): NotificationChannel {
        return NotificationChannel(
            channelId,
            name,
            NotificationManager.IMPORTANCE_LOW
        ).also { channel ->
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getString(@StringRes id: Int) = applicationContext.getString(id)
}