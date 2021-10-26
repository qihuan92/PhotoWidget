package com.qihuan.photowidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.qihuan.photowidget.db.AppDatabase
import com.qihuan.photowidget.ktx.goAsync
import com.qihuan.photowidget.ktx.logD
import com.qihuan.photowidget.ktx.logE
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
        } else if (intent.action == ACTION_OPEN_ALBUM) {
            openAlbum(intent)
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

    private fun openAlbum(intent: Intent) {
        // todo
        logE("PhotoWidgetProvider", "openAlbum() ${intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)}")
        logE("PhotoWidgetProvider", "openAlbum() ${intent.getParcelableExtra<Uri>(EXTRA_IMAGE_URI)}")
        logE("PhotoWidgetProvider", "openAlbum() ${intent.getIntExtra("position", 0)}")
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