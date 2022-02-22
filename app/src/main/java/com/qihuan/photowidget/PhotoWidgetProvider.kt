package com.qihuan.photowidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.RemoteViews
import com.qihuan.photowidget.db.AppDatabase
import com.qihuan.photowidget.ktx.goAsync
import com.qihuan.photowidget.ktx.logD
import kotlinx.coroutines.GlobalScope

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [com.qihuan.photowidget.config.ConfigureActivity]
 */
open class PhotoWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        logD("PhotoWidgetProvider", "onAppWidgetOptionsChanged() appWidgetIds=$appWidgetIds")
        val widgetDao = AppDatabase.getDatabase(context).widgetDao()
        goAsync(GlobalScope) {
            for (appWidgetId in appWidgetIds) {
                val widgetBean = widgetDao.selectById(appWidgetId)
                if (widgetBean != null) {
                    updateAppWidget(context, appWidgetManager, widgetBean)
                }
            }
        }
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
        logD("PhotoWidgetProvider", "onDeleted() appWidgetIds=$appWidgetIds")
        goAsync(GlobalScope) {
            deleteWidgets(context, appWidgetIds)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return
        }
        logD("PhotoWidgetProvider", "onAppWidgetOptionsChanged() appWidgetId=$appWidgetId")
        val widgetDao = AppDatabase.getDatabase(context).widgetDao()
        goAsync(GlobalScope) {
            val widgetBean = widgetDao.selectById(appWidgetId)
            if (widgetBean != null) {
                updateAppWidget(context, appWidgetManager, widgetBean)
            }
        }
    }
}