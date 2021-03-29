package com.qihuan.photowidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.RemoteViews
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.graphics.drawable.toBitmap
import com.qihuan.photowidget.bean.WidgetBean
import com.qihuan.photowidget.db.AppDatabase
import com.qihuan.photowidget.ktx.deleteDir
import com.qihuan.photowidget.ktx.dp
import com.qihuan.photowidget.ktx.isOpenAppLink
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import kotlin.random.Random

const val EXTRA_INTERVAL = "interval"
const val EXTRA_NAV = "nav"
const val NAV_WIDGET_PREV = "nav_widget_prev"
const val NAV_WIDGET_NEXT = "nav_widget_next"

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [ConfigureActivity]
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
        val views = createRemoteViews(context, interval)
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
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    widgetBean: WidgetBean
) {
    val widgetInfo = widgetBean.widgetInfo
    val widgetId = widgetInfo.widgetId

    val autoPlayInterval = widgetInfo.autoPlayInterval
    val views = createRemoteViews(context, autoPlayInterval)
    views.setRemoteAdapter(R.id.vf_picture, Intent(context, WidgetPhotoService::class.java).apply {
        type = Random.nextInt().toString()
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
    })

    val horizontalPadding = widgetInfo.horizontalPadding.dp
    val verticalPadding = widgetInfo.verticalPadding.dp
    views.setViewPadding(
        R.id.root,
        horizontalPadding,
        verticalPadding,
        horizontalPadding,
        verticalPadding
    )

    if (!widgetInfo.openUrl.isNullOrBlank()) {
        val intent = if (widgetInfo.openUrl.isOpenAppLink()) {
            val info = widgetInfo.openUrl.split("/")
            context.packageManager.getLaunchIntentForPackage(info[2])
        } else {
            Intent(Intent.ACTION_VIEW, Uri.parse(widgetInfo.openUrl))
        }
        if (intent != null) {
            val pendingIntent =
                PendingIntent.getActivity(
                    context,
                    widgetId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            try {
                views.setOnClickPendingIntent(R.id.area_center, pendingIntent)
                if (widgetBean.imageList.size == 1) {
                    views.setOnClickPendingIntent(R.id.area_left, pendingIntent)
                    views.setOnClickPendingIntent(R.id.area_right, pendingIntent)
                }
            } catch (e: Exception) {
                Log.e("PhotoWidgetProvider", e.message, e)
            }
        }
    }

    if (widgetBean.imageList.size > 1) {
        views.setOnClickPendingIntent(
            R.id.area_left,
            getWidgetNavPendingIntent(context, widgetId, NAV_WIDGET_PREV, autoPlayInterval)
        )

        views.setOnClickPendingIntent(
            R.id.area_right,
            getWidgetNavPendingIntent(context, widgetId, NAV_WIDGET_NEXT, autoPlayInterval)
        )
    }

    appWidgetManager.updateAppWidget(widgetId, views)
    appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.vf_picture)
}

fun createRemoteViews(context: Context, interval: Int?): RemoteViews {
    if (interval == null || interval < 0) {
        return RemoteViews(context.packageName, R.layout.photo_widget)
    }
    val layoutId = context.resources.getIdentifier(
        "photo_widget_interval_${interval}",
        "layout",
        context.packageName
    )
    if (layoutId <= 0) {
        return RemoteViews(context.packageName, R.layout.photo_widget)
    }
    return RemoteViews(context.packageName, layoutId)
}

fun getWidgetNavPendingIntent(
    context: Context,
    widgetId: Int,
    navAction: String,
    interval: Int?
): PendingIntent {
    return PendingIntent.getBroadcast(
        context,
        0,
        getWidgetNavIntent(context, widgetId, navAction, interval),
        PendingIntent.FLAG_UPDATE_CURRENT
    )
}

fun getWidgetNavIntent(context: Context, widgetId: Int, navAction: String, interval: Int?): Intent {
    return Intent(context, PhotoWidgetProvider::class.java).apply {
        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        putExtra(EXTRA_NAV, navAction)
        putExtra(EXTRA_INTERVAL, interval)
    }
}

internal fun createWidgetBitmap(context: Context, uri: Uri, radius: Int): Bitmap {
    val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
    val roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(context.resources, bitmap)
    val radiusPx = radius.toFloat() * 2
    roundedBitmapDrawable.cornerRadius = radiusPx
    roundedBitmapDrawable.setAntiAlias(true)
    return roundedBitmapDrawable.toBitmap(bitmap.width, bitmap.height)
}