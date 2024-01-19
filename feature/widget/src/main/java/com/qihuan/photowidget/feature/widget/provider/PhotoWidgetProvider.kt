package com.qihuan.photowidget.feature.widget.provider

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.qihuan.photowidget.core.common.JobManager
import com.qihuan.photowidget.core.common.ktx.logD
import com.qihuan.photowidget.core.model.BroadcastAction
import com.qihuan.photowidget.feature.widget.EXTRA_NAV
import com.qihuan.photowidget.feature.widget.NAV_WIDGET_NEXT
import com.qihuan.photowidget.feature.widget.NAV_WIDGET_PREV
import com.qihuan.photowidget.feature.widget.R
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [com.qihuan.photowidget.config.ConfigureActivity]
 */
open class PhotoWidgetProvider : AppWidgetProvider(), KoinComponent {
    private val jobManager: JobManager by inject()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        logD("PhotoWidgetProvider", "onUpdate() appWidgetIds=${appWidgetIds.joinToString()}")
        jobManager.scheduleUpdateWidgetJob(appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        logD("PhotoWidgetProvider", "onReceive() intent.action=${intent.action}")
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val navAction = intent.getStringExtra(EXTRA_NAV)
            if (!navAction.isNullOrEmpty()) {
                navWidget(context, intent, navAction)
            }
        }
        super.onReceive(context, intent)
    }

    private fun navWidget(context: Context, intent: Intent, navAction: String) {
        val widgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_photo)
        when (navAction) {
            NAV_WIDGET_NEXT -> remoteViews.showNext(R.id.vf_picture)
            NAV_WIDGET_PREV -> remoteViews.showPrevious(R.id.vf_picture)
        }
        AppWidgetManager.getInstance(context).updateAppWidget(widgetId, remoteViews)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        logD("PhotoWidgetProvider", "onDeleted() appWidgetIds=${appWidgetIds.joinToString()}")
        jobManager.scheduleDeleteWidgetJob(appWidgetIds)
        LocalBroadcastManager.getInstance(context).sendBroadcast(
            Intent(BroadcastAction.APPWIDGET_DELETED)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        )
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        logD("PhotoWidgetProvider", "onAppWidgetOptionsChanged() appWidgetId=$appWidgetId")
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.vf_picture)
    }
}