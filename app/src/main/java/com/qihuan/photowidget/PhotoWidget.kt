package com.qihuan.photowidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.ImageView
import android.widget.RemoteViews
import androidx.core.net.toFile
import com.qihuan.photowidget.bean.*
import com.qihuan.photowidget.db.AppDatabase
import com.qihuan.photowidget.ktx.deleteDir
import com.qihuan.photowidget.ktx.dp
import com.qihuan.photowidget.ktx.logE
import com.qihuan.photowidget.ktx.toRoundedBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.random.Random

const val EXTRA_INTERVAL = "interval"
const val EXTRA_NAV = "nav"
const val NAV_WIDGET_PREV = "nav_widget_prev"
const val NAV_WIDGET_NEXT = "nav_widget_next"

suspend fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    widgetBean: WidgetBean
) {
    val imageList = widgetBean.imageList
    if (imageList.isNullOrEmpty()) {
        logE("PhotoWidget", "updateAppWidget() -> imageList.isNullOrEmpty")
        return
    }
    val isMultiImage = imageList.size > 1
    val widgetInfo = widgetBean.widgetInfo
    val widgetId = widgetInfo.widgetId
    val linkInfo = widgetBean.linkInfo
    val autoPlayInterval = widgetInfo.autoPlayInterval
    val topPadding = widgetInfo.topPadding.dp
    val bottomPadding = widgetInfo.bottomPadding.dp
    val leftPadding = widgetInfo.leftPadding.dp
    val rightPadding = widgetInfo.rightPadding.dp
    val scaleType = widgetInfo.photoScaleType.scaleType
    val widgetRadius = widgetInfo.widgetRadius
    val widgetTransparency = widgetInfo.widgetTransparency

    val remoteViews: RemoteViews
    if (isMultiImage) {
        remoteViews = createFlipperRemoteViews(context, autoPlayInterval.interval)
        val serviceIntent = Intent(context, WidgetPhotoService::class.java).apply {
            type = Random.nextInt().toString()
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        }
        remoteViews.setRemoteAdapter(R.id.vf_picture, serviceIntent)
    } else {
        remoteViews = RemoteViews(context.packageName, R.layout.photo_widget_single)
        remoteViews.removeAllViews(R.id.fl_picture_container)
        remoteViews.addView(R.id.fl_picture_container, context.createImageRemoteViews(scaleType))
        withContext(Dispatchers.IO) {
            val imageUri = imageList[0].imageUri
            val imageWidth = appWidgetManager.getWidgetImageWidth(widgetInfo)
            val imageHeight = appWidgetManager.getWidgetImageHeight(widgetInfo)
            remoteViews.loadImage(
                context,
                imageUri,
                scaleType,
                widgetRadius,
                widgetTransparency,
                imageWidth,
                imageHeight
            )
        }
    }

    remoteViews.setViewPadding(
        android.R.id.background,
        leftPadding,
        topPadding,
        rightPadding,
        bottomPadding
    )

    val centerPendingIntent: PendingIntent? = context.getWidgetOpenPendingIntent(widgetId, linkInfo)
    val leftPendingIntent: PendingIntent?
    val rightPendingIntent: PendingIntent?

    if (isMultiImage) {
        leftPendingIntent =
            context.getWidgetNavPendingIntent(widgetId, NAV_WIDGET_PREV, autoPlayInterval)
        rightPendingIntent =
            context.getWidgetNavPendingIntent(widgetId, NAV_WIDGET_NEXT, autoPlayInterval)
    } else {
        leftPendingIntent = centerPendingIntent
        rightPendingIntent = centerPendingIntent
    }

    try {
        remoteViews.setOnClickPendingIntent(R.id.area_center, centerPendingIntent)
        remoteViews.setOnClickPendingIntent(R.id.area_left, leftPendingIntent)
        remoteViews.setOnClickPendingIntent(R.id.area_right, rightPendingIntent)
    } catch (e: Exception) {
        logE("PhotoWidgetProvider", e.message, e)
    }

    appWidgetManager.updateAppWidget(widgetId, remoteViews)
    if (isMultiImage) {
        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.vf_picture)
    }
}

private fun Context.getWidgetOpenPendingIntent(widgetId: Int, linkInfo: LinkInfo?): PendingIntent? {
    if (linkInfo == null) {
        return null
    }
    var intent: Intent? = null
    if (linkInfo.type == LinkType.OPEN_APP) {
        intent = packageManager.getLaunchIntentForPackage(linkInfo.link)
    } else if (linkInfo.type == LinkType.OPEN_URL) {
        intent = Intent(Intent.ACTION_VIEW, Uri.parse(linkInfo.link))
    }
    if (intent != null) {
        return PendingIntent.getActivity(
            this,
            widgetId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
    return null
}

private fun Context.getWidgetNavPendingIntent(
    widgetId: Int,
    navAction: String,
    playInterval: PlayInterval
): PendingIntent {
    val navIntent = Intent(this, PhotoWidgetProvider::class.java).apply {
        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        putExtra(EXTRA_NAV, navAction)
        putExtra(EXTRA_INTERVAL, playInterval.interval)
    }
    return PendingIntent.getBroadcast(
        this,
        Random.nextInt(),
        navIntent,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
    )
}


fun createFlipperRemoteViews(context: Context, interval: Int): RemoteViews {
    val defRemoteViews = RemoteViews(context.packageName, R.layout.photo_widget)
    if (interval < 0) {
        return defRemoteViews
    }
    val layoutId = context.resources.getIdentifier(
        "photo_widget_interval_${interval}",
        "layout",
        context.packageName
    )
    if (layoutId <= 0) {
        return defRemoteViews
    }
    return RemoteViews(context.packageName, layoutId)
}

fun Context.createImageRemoteViews(scaleType: ImageView.ScaleType): RemoteViews {
    return if (scaleType == ImageView.ScaleType.FIT_CENTER) {
        RemoteViews(packageName, R.layout.layout_widget_image)
    } else {
        RemoteViews(packageName, R.layout.layout_widget_image_fitxy)
    }
}

fun AppWidgetManager.getWidgetImageWidth(widgetInfo: WidgetInfo): Int {
    val width = getAppWidgetOptions(widgetInfo.widgetId)
        .getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
    val imageWidth = width - widgetInfo.leftPadding - widgetInfo.rightPadding
    return imageWidth.toInt()
}

fun AppWidgetManager.getWidgetImageHeight(widgetInfo: WidgetInfo): Int {
    val height = getAppWidgetOptions(widgetInfo.widgetId)
        .getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)
    val imageHeight = height - widgetInfo.topPadding - widgetInfo.bottomPadding
    return imageHeight.toInt()
}

fun RemoteViews.loadImage(
    context: Context,
    uri: Uri,
    scaleType: ImageView.ScaleType,
    radius: Float,
    transparency: Float,
    width: Int,
    height: Int
) {
    if (!uri.toFile().exists()) {
        setImageViewResource(R.id.iv_picture, R.drawable.shape_photo_404)
        return
    }

    val imageBitmap = uri.toRoundedBitmap(
        context, radius.dp, scaleType, width.toFloat().dp, height.toFloat().dp
    )

    setImageViewBitmap(R.id.iv_picture, imageBitmap)

    val alpha = (255 * (1f - transparency / 100f)).toInt()
    setInt(R.id.iv_picture, "setImageAlpha", alpha)
}

suspend fun deleteWidget(context: Context, widgetId: Int) {
    val widgetDao = AppDatabase.getDatabase(context).widgetDao()
    widgetDao.deleteByWidgetId(widgetId)
    val outFile = File(context.filesDir, "widget_${widgetId}")
    outFile.deleteDir()
}

suspend fun deleteWidgets(context: Context, widgetIds: IntArray) {
    val widgetDao = AppDatabase.getDatabase(context).widgetDao()
    for (widgetId in widgetIds) {
        widgetDao.deleteByWidgetId(widgetId)
        val outFile = File(context.filesDir, "widget_${widgetId}")
        outFile.deleteDir()
    }
}