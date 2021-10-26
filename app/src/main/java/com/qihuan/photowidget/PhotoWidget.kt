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
import com.qihuan.photowidget.bean.LinkType
import com.qihuan.photowidget.bean.PlayInterval
import com.qihuan.photowidget.bean.WidgetBean
import com.qihuan.photowidget.bean.WidgetInfo
import com.qihuan.photowidget.db.AppDatabase
import com.qihuan.photowidget.ktx.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.random.Random

const val EXTRA_INTERVAL = "interval"
const val EXTRA_NAV = "nav"
const val EXTRA_IMAGE_URI = "image_uri"
const val NAV_WIDGET_PREV = "nav_widget_prev"
const val NAV_WIDGET_NEXT = "nav_widget_next"
const val ACTION_OPEN_ALBUM = "${BuildConfig.APPLICATION_ID}.OPEN_ALBUM_ACTION"

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
    val widgetRadius = widgetInfo.widgetRadius.dp
    val widgetTransparency = widgetInfo.widgetTransparency

    val remoteViews: RemoteViews
    if (isMultiImage) {
        // Create flipper remote views
        remoteViews = createFlipperRemoteViews(context, autoPlayInterval.interval)
        val serviceIntent = Intent(context, WidgetPhotoService::class.java)
        serviceIntent.type = Random.nextInt().toString()
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        remoteViews.setRemoteAdapter(R.id.vf_picture, serviceIntent)
    } else {
        // Create single image remote views
        remoteViews = RemoteViews(context.packageName, R.layout.photo_widget_single)
        remoteViews.removeAllViews(R.id.fl_picture_container)
        remoteViews.addView(R.id.fl_picture_container, createImageRemoteViews(context, scaleType))

        // Load image
        val imageUri = imageList.first().imageUri
        if (imageUri.toFile().exists()) {
            val imageWidth = appWidgetManager.getWidgetImageWidth(widgetInfo).toFloat().dp
            val imageHeight = appWidgetManager.getWidgetImageHeight(widgetInfo).toFloat().dp
            val imageBitmap = withContext(Dispatchers.IO) {
                imageUri.toRoundedBitmap(context, widgetRadius, scaleType, imageWidth, imageHeight)
            }
            remoteViews.setImageViewBitmap(R.id.iv_picture, imageBitmap)
        } else {
            remoteViews.setImageViewResource(R.id.iv_picture, R.drawable.shape_photo_404)
        }
    }

    // Set widget alpha
    val alpha = (255 * (1f - widgetTransparency / 100f)).toInt()
    remoteViews.setInt(R.id.iv_picture, "setImageAlpha", alpha)

    // Set widget padding
    remoteViews.setViewPadding(
        android.R.id.background,
        leftPadding,
        topPadding,
        rightPadding,
        bottomPadding
    )

    val centerPendingIntent: PendingIntent? = createWidgetOpenPendingIntent(context, widgetBean)
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
        if (isMultiImage && linkInfo != null && linkInfo.type == LinkType.OPEN_ALBUM) {
            remoteViews.setOnClickPendingIntent(R.id.area_center, null)
            remoteViews.setPendingIntentTemplate(R.id.vf_picture, centerPendingIntent)
        } else {
            remoteViews.setOnClickPendingIntent(R.id.area_center, centerPendingIntent)
        }
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

fun createOpenAlbumIntent(context: Context, imageUri: Uri): Intent =
    Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(imageUri.providerUri(context), "image/*")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

private fun createWidgetOpenPendingIntent(
    context: Context,
    widgetBean: WidgetBean,
): PendingIntent? {
    val widgetId = widgetBean.widgetInfo.widgetId
    val linkInfo = widgetBean.linkInfo ?: return null
    val imageList = widgetBean.imageList

    if (imageList.isNotEmpty() && linkInfo.type == LinkType.OPEN_ALBUM) {
        return if (imageList.size == 1) {
            PendingIntent.getActivity(
                context,
                widgetId,
                createOpenAlbumIntent(context, imageList.first().imageUri),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        } else {
            PendingIntent.getBroadcast(
                context,
                widgetId + 1,
                Intent(context, PhotoWidgetProvider::class.java).apply {
                    action = ACTION_OPEN_ALBUM
                    data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                },
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }

    var intent: Intent? = null
    if (linkInfo.type == LinkType.OPEN_APP) {
        intent = context.packageManager.getLaunchIntentForPackage(linkInfo.link)
    } else if (linkInfo.type == LinkType.OPEN_URL) {
        intent = Intent(Intent.ACTION_VIEW, Uri.parse(linkInfo.link))
    }
    if (intent != null) {
        return PendingIntent.getActivity(
            context,
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

fun createImageRemoteViews(context: Context, scaleType: ImageView.ScaleType): RemoteViews {
    return if (scaleType == ImageView.ScaleType.FIT_CENTER) {
        RemoteViews(context.packageName, R.layout.layout_widget_image)
    } else {
        RemoteViews(context.packageName, R.layout.layout_widget_image_fitxy)
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