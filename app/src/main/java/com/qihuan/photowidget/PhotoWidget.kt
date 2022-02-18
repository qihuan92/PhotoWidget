package com.qihuan.photowidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.ImageView
import android.widget.RemoteViews
import androidx.core.net.toFile
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.AppWidgetTarget
import com.qihuan.photowidget.analysis.EventStatistics
import com.qihuan.photowidget.bean.LinkInfo
import com.qihuan.photowidget.bean.WidgetBean
import com.qihuan.photowidget.bean.WidgetInfo
import com.qihuan.photowidget.common.LinkType
import com.qihuan.photowidget.common.PlayInterval
import com.qihuan.photowidget.common.WidgetFrameType
import com.qihuan.photowidget.common.WidgetType
import com.qihuan.photowidget.db.AppDatabase
import com.qihuan.photowidget.ktx.dp
import com.qihuan.photowidget.ktx.logE
import com.qihuan.photowidget.ktx.providerUri
import com.qihuan.photowidget.ktx.toRoundedBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.random.Random

const val EXTRA_INTERVAL = "interval"
const val EXTRA_NAV = "nav"
const val NAV_WIDGET_PREV = "nav_widget_prev"
const val NAV_WIDGET_NEXT = "nav_widget_next"

val MUTABLE_FLAG = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    PendingIntent.FLAG_MUTABLE
} else {
    PendingIntent.FLAG_UPDATE_CURRENT
}

suspend fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    widgetBean: WidgetBean
) {
    try {
        EventStatistics.track(
            EventStatistics.WIDGET_SAVE, mapOf(
                "LinkType" to widgetBean.linkInfo?.type?.value,
                "LinkUri" to widgetBean.linkInfo?.link,
                "WidgetType" to widgetBean.widgetInfo.widgetType.code,
                "WidgetPaddingLeft" to widgetBean.widgetInfo.leftPadding.toString(),
                "WidgetPaddingTop" to widgetBean.widgetInfo.topPadding.toString(),
                "WidgetPaddingRight" to widgetBean.widgetInfo.rightPadding.toString(),
                "WidgetPaddingBottom" to widgetBean.widgetInfo.bottomPadding.toString(),
                "WidgetRadius" to widgetBean.widgetInfo.widgetRadius.toString() + widgetBean.widgetInfo.widgetRadiusUnit.unitName,
                "WidgetTransparency" to widgetBean.widgetInfo.widgetTransparency.toString(),
                "WidgetAutoPlayInterval" to widgetBean.widgetInfo.autoPlayInterval.interval.toString(),
                "WidgetPhotoScaleType" to widgetBean.widgetInfo.photoScaleType.description,
                "WidgetImageSize" to widgetBean.imageList.size.toString(),
                "WidgetFrameType" to widgetBean.frame?.type?.name,
            )
        )
    } catch (e: Exception) {
        logE("PhotoWidget::updateAppWidget", "TrackError:" + e.message, e)
    }

    val imageList = widgetBean.imageList
    if (imageList.isNullOrEmpty()) {
        logE("PhotoWidget", "updateAppWidget() -> imageList.isNullOrEmpty")
        return
    }
    val isMultiImage = imageList.size > 1
    val widgetInfo = widgetBean.widgetInfo
    val widgetId = widgetInfo.widgetId
    val linkInfo = widgetBean.linkInfo
    val widgetFrame = widgetBean.frame
    val autoPlayInterval = widgetInfo.autoPlayInterval
    val topPadding = widgetInfo.topPadding.dp
    val bottomPadding = widgetInfo.bottomPadding.dp
    val leftPadding = widgetInfo.leftPadding.dp
    val rightPadding = widgetInfo.rightPadding.dp
    val frameWidth = widgetFrame?.width?.dp ?: 0
    val scaleType = widgetInfo.photoScaleType.scaleType
    val widgetRadius = widgetInfo.widgetRadius
    val widgetRadiusUnit = widgetInfo.widgetRadiusUnit
    val widgetTransparency = widgetInfo.widgetTransparency

    val remoteViews: RemoteViews
    if (widgetInfo.widgetType == WidgetType.GIF) {
        remoteViews = RemoteViews(context.packageName, R.layout.gif_photo_widget)
        val serviceIntent = Intent(context, GifWidgetPhotoService::class.java)
        serviceIntent.type = Random.nextInt().toString()
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        remoteViews.setRemoteAdapter(R.id.vf_picture, serviceIntent)

        // Set widget link
        val linkIntent = createLinkIntent(context, linkInfo, imageList.first().imageUri)
        val linkPendingIntent =
            PendingIntent.getActivity(context, widgetId, linkIntent, MUTABLE_FLAG)
        remoteViews.setOnClickPendingIntent(android.R.id.background, linkPendingIntent)
    } else {
        if (isMultiImage) {
            // Create flipper remote views
            remoteViews = createFlipperRemoteViews(context, autoPlayInterval.interval)
            val serviceIntent = Intent(context, WidgetPhotoService::class.java)
            serviceIntent.type = Random.nextInt().toString()
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            remoteViews.setRemoteAdapter(R.id.vf_picture, serviceIntent)

            // Set widget link
            val linkIntent = createLinkIntent(context, linkInfo, null)
            val linkPendingIntent =
                PendingIntent.getActivity(context, widgetId, linkIntent, MUTABLE_FLAG)
            remoteViews.setPendingIntentTemplate(R.id.vf_picture, linkPendingIntent)

            // Set page actions
            val leftPendingIntent =
                createWidgetNavPendingIntent(context, widgetId, NAV_WIDGET_PREV, autoPlayInterval)
            remoteViews.setOnClickPendingIntent(R.id.area_left, leftPendingIntent)
            val rightPendingIntent =
                createWidgetNavPendingIntent(context, widgetId, NAV_WIDGET_NEXT, autoPlayInterval)
            remoteViews.setOnClickPendingIntent(R.id.area_right, rightPendingIntent)
        } else {
            // Create single image remote views
            remoteViews = createImageRemoteViews(context, scaleType)
            ImageView(context).scaleType

            // Load image
            val imageUri = imageList.first().imageUri
            if (imageUri.toFile().exists()) {
                val imageWidth = appWidgetManager.getWidgetImageWidth(widgetInfo).toFloat().dp
                val imageHeight = appWidgetManager.getWidgetImageHeight(widgetInfo).toFloat().dp
                val imageBitmap = withContext(Dispatchers.IO) {
                    imageUri.toRoundedBitmap(
                        context,
                        widgetRadius,
                        widgetRadiusUnit,
                        scaleType,
                        imageWidth,
                        imageHeight
                    )
                }
                remoteViews.setImageViewBitmap(R.id.iv_picture, imageBitmap)
            } else {
                remoteViews.setImageViewResource(R.id.iv_picture, R.drawable.shape_photo_404)
            }

            // Set widget alpha
            val alpha = (255 * (1f - widgetTransparency / 100f)).toInt()
            remoteViews.setInt(R.id.iv_picture, "setImageAlpha", alpha)

            // Set widget link
            val linkIntent = createLinkIntent(context, linkInfo, imageUri)
            val linkPendingIntent =
                PendingIntent.getActivity(context, widgetId, linkIntent, MUTABLE_FLAG)
            remoteViews.setOnClickPendingIntent(R.id.iv_picture, linkPendingIntent)
        }
    }

    // Set widget padding
    remoteViews.setViewPadding(
        R.id.fl_picture_container,
        frameWidth + leftPadding,
        frameWidth + topPadding,
        frameWidth + rightPadding,
        frameWidth + bottomPadding
    )

    // 处理边框相关逻辑
    remoteViews.setImageViewResource(R.id.iv_widget_background, R.drawable.app_widget_background)
    if (widgetFrame != null && widgetFrame.type != WidgetFrameType.NONE) {
        remoteViews.setViewVisibility(R.id.iv_widget_background, View.VISIBLE)
        if (widgetFrame.type == WidgetFrameType.BUILD_IN || widgetFrame.type == WidgetFrameType.IMAGE) {
            Glide.with(context)
                .asBitmap()
                .load(widgetFrame.frameUri)
                .into(AppWidgetTarget(context, R.id.iv_widget_background, remoteViews, widgetId))
        } else if (widgetFrame.type == WidgetFrameType.COLOR) {
            remoteViews.setInt(
                R.id.iv_widget_background,
                "setColorFilter",
                Color.parseColor(widgetFrame.frameColor)
            )
        }
    } else {
        remoteViews.setViewVisibility(R.id.iv_widget_background, View.GONE)
        remoteViews.setInt(R.id.iv_widget_background, "setColorFilter", Color.TRANSPARENT)
    }

    appWidgetManager.updateAppWidget(widgetId, remoteViews)
    if (isMultiImage) {
        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.vf_picture)
    }
}

fun createLinkIntent(context: Context, linkInfo: LinkInfo?, imageUri: Uri?): Intent {
    if (linkInfo == null) {
        return Intent()
    }
    return when (linkInfo.type) {
        LinkType.OPEN_APP -> context.packageManager.getLaunchIntentForPackage(linkInfo.link)
            ?: Intent()
        LinkType.OPEN_URL -> Intent(Intent.ACTION_VIEW, Uri.parse(linkInfo.link))
        LinkType.OPEN_ALBUM -> if (imageUri != null) {
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(imageUri.providerUri(context), "image/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } else {
            Intent()
        }
        LinkType.OPEN_FILE -> Intent(Intent.ACTION_VIEW, Uri.parse(linkInfo.link)).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}

private fun createWidgetNavPendingIntent(
    context: Context,
    widgetId: Int,
    navAction: String,
    playInterval: PlayInterval
): PendingIntent {
    val navIntent = Intent(context, PhotoWidgetProvider::class.java).apply {
        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        putExtra(EXTRA_NAV, navAction)
        putExtra(EXTRA_INTERVAL, playInterval.interval)
    }
    return PendingIntent.getBroadcast(
        context,
        Random.nextInt(),
        navIntent,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
    )
}

fun createFlipperRemoteViews(context: Context, interval: Int): RemoteViews {
    val defRemoteViews = RemoteViews(context.packageName, R.layout.widget_photo)
    if (interval < 0) {
        return defRemoteViews
    }
    val layoutId = context.resources.getIdentifier(
        "widget_photo_interval_${interval}",
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
        RemoteViews(context.packageName, R.layout.widget_photo_single)
    } else {
        RemoteViews(context.packageName, R.layout.widget_photo_single_fitxy)
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
    outFile.deleteRecursively()
}

suspend fun deleteWidgets(context: Context, widgetIds: IntArray) {
    val widgetDao = AppDatabase.getDatabase(context).widgetDao()
    for (widgetId in widgetIds) {
        widgetDao.deleteByWidgetId(widgetId)
        val outFile = File(context.filesDir, "widget_${widgetId}")
        outFile.deleteRecursively()
    }
}