package com.qihuan.photowidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.qihuan.photowidget.db.AppDatabase
import com.qihuan.photowidget.ktx.deleteDir
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

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
        val widgetDao = AppDatabase.getDatabase(context).widgetDao()
        GlobalScope.launch {
            // 更新微件
            for (appWidgetId in appWidgetIds) {
                val widgetBean = widgetDao.selectById(appWidgetId)
                if (widgetBean != null) {
                    updateAppWidget(context, appWidgetManager, widgetBean)
                }
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
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
        val interval = intent.getIntExtra(EXTRA_INTERVAL, -1)
        val views = createFlipperRemoteViews(context, interval)
        when (navAction) {
            NAV_WIDGET_NEXT -> views.showNext(R.id.vf_picture)
            NAV_WIDGET_PREV -> views.showPrevious(R.id.vf_picture)
        }
        AppWidgetManager.getInstance(context).updateAppWidget(widgetId, views)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val widgetDao = AppDatabase.getDatabase(context).widgetDao()
        // 删除一些缓存数据
        GlobalScope.launch {
            for (appWidgetId in appWidgetIds) {
                widgetDao.deleteByWidgetId(appWidgetId)

                val outFile = File(context.filesDir, "widget_${appWidgetId}")
                outFile.deleteDir()
            }
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        val widgetDao = AppDatabase.getDatabase(context).widgetDao()
        GlobalScope.launch {
            val widgetBean = widgetDao.selectById(appWidgetId)
            if (widgetBean != null) {
                if (widgetBean.imageList.size > 1) {
                    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.vf_picture)
                } else {
                    updateAppWidget(context, appWidgetManager, widgetBean)
                }
            }
        }
    }
}