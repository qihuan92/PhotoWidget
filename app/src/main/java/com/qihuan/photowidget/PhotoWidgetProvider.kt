package com.qihuan.photowidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.RemoteViews
import androidx.core.net.toFile
import com.qihuan.photowidget.bean.*
import com.qihuan.photowidget.db.AppDatabase
import com.qihuan.photowidget.ktx.deleteDir
import com.qihuan.photowidget.ktx.dp
import com.qihuan.photowidget.ktx.getRoundedBitmap
import com.qihuan.photowidget.ktx.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.random.Random

const val EXTRA_INTERVAL = "interval"
const val EXTRA_NAV = "nav"
const val NAV_WIDGET_PREV = "nav_widget_prev"
const val NAV_WIDGET_NEXT = "nav_widget_next"

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

suspend fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    widgetBean: WidgetBean
) {
    val imageList = widgetBean.imageList
    val widgetInfo = widgetBean.widgetInfo
    val widgetId = widgetInfo.widgetId
    val linkInfo = widgetInfo.linkInfo
    val autoPlayInterval = widgetInfo.autoPlayInterval
    val horizontalPadding = widgetInfo.horizontalPadding.dp
    val verticalPadding = widgetInfo.verticalPadding.dp

    val views = if (imageList.size > 1) {
        createRemoteViews(context, autoPlayInterval.interval).apply {
            setRemoteAdapter(
                R.id.vf_picture,
                Intent(context, WidgetPhotoService::class.java).apply {
                    type = Random.nextInt().toString()
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                })
        }
    } else {
        RemoteViews(context.packageName, R.layout.photo_widget_single).apply {
            removeAllViews(R.id.fl_picture_container)
            val widgetImageView = withContext(Dispatchers.IO) {
                getWidgetImageView(context, appWidgetManager, widgetInfo, imageList[0])
            }
            addView(R.id.fl_picture_container, widgetImageView)
        }
    }

    views.setViewPadding(
        R.id.root, horizontalPadding, verticalPadding, horizontalPadding, verticalPadding
    )

    var centerPendingIntent: PendingIntent? = null
    val leftPendingIntent: PendingIntent?
    val rightPendingIntent: PendingIntent?

    if (linkInfo != null) {
        var intent: Intent? = null
        if (linkInfo.type == LinkType.OPEN_APP) {
            intent = context.packageManager.getLaunchIntentForPackage(linkInfo.getPackageName())
        } else if (linkInfo.type == LinkType.OPEN_URL) {
            intent = Intent(Intent.ACTION_VIEW, Uri.parse(linkInfo.link))
        }
        if (intent != null) {
            centerPendingIntent =
                PendingIntent.getActivity(
                    context,
                    widgetId,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
        }
    }

    if (imageList.size > 1) {
        leftPendingIntent =
            getWidgetNavPendingIntent(context, widgetId, NAV_WIDGET_PREV, autoPlayInterval)
        rightPendingIntent =
            getWidgetNavPendingIntent(context, widgetId, NAV_WIDGET_NEXT, autoPlayInterval)
    } else {
        leftPendingIntent = centerPendingIntent
        rightPendingIntent = centerPendingIntent
    }

    try {
        views.setOnClickPendingIntent(R.id.area_center, centerPendingIntent)
        views.setOnClickPendingIntent(R.id.area_left, leftPendingIntent)
        views.setOnClickPendingIntent(R.id.area_right, rightPendingIntent)
    } catch (e: Exception) {
        logE("PhotoWidgetProvider", e.message, e)
    }

    appWidgetManager.updateAppWidget(widgetId, views)
    if (imageList.size > 1) {
        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.vf_picture)
    }
}

fun createRemoteViews(context: Context, interval: Int): RemoteViews {
    if (interval < 0) {
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
    playInterval: PlayInterval
): PendingIntent {
    return PendingIntent.getBroadcast(
        context,
        Random.nextInt(),
        getWidgetNavIntent(context, widgetId, navAction, playInterval.interval),
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

fun getWidgetImageView(
    context: Context,
    appWidgetManager: AppWidgetManager,
    widgetInfo: WidgetInfo,
    widgetImage: WidgetImage
): RemoteViews {
    val widgetId = widgetInfo.widgetId
    val scaleType = widgetInfo.photoScaleType.scaleType
    val radius = widgetInfo.widgetRadius
    val transparency = widgetInfo.widgetTransparency

    var remoteViews = if (scaleType == ImageView.ScaleType.FIT_CENTER) {
        RemoteViews(context.packageName, R.layout.layout_widget_image)
    } else {
        RemoteViews(context.packageName, R.layout.layout_widget_image_fitxy)
    }
    val uri = widgetImage.imageUri
    if (uri.toFile().exists()) {
        val width = appWidgetManager.getAppWidgetOptions(widgetId)
            .getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
        val height = appWidgetManager.getAppWidgetOptions(widgetId)
            .getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)

        if (width == 0 || height == 0) {
            remoteViews = RemoteViews(context.packageName, R.layout.layout_widget_image)
        }

        remoteViews.setImageViewBitmap(
            R.id.iv_picture,
            uri.getRoundedBitmap(
                context,
                radius.dp,
                scaleType,
                width.toFloat().dp,
                height.toFloat().dp
            )
        )
        val alpha = (255 * (1f - transparency / 100f)).toInt()
        remoteViews.setInt(R.id.iv_picture, "setImageAlpha", alpha)
    } else {
        remoteViews.setImageViewResource(R.id.iv_picture, R.drawable.shape_photo_404)
    }
    return remoteViews
}