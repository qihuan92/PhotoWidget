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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.AppWidgetTarget
import com.qihuan.photowidget.core.analysis.EventStatistics
import com.qihuan.photowidget.core.database.AppDatabase
import com.qihuan.photowidget.core.database.model.LinkInfo
import com.qihuan.photowidget.core.database.model.WidgetBean
import com.qihuan.photowidget.core.database.model.WidgetInfo
import com.qihuan.photowidget.core.model.LinkType
import com.qihuan.photowidget.core.model.WidgetFrameType
import com.qihuan.photowidget.core.model.WidgetType
import com.qihuan.photowidget.ktx.dp
import com.qihuan.photowidget.ktx.logE
import com.qihuan.photowidget.ktx.providerUri
import com.qihuan.photowidget.ktx.setBackgroundColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.random.Random

const val EXTRA_NAV = "nav"
const val NAV_WIDGET_PREV = "nav_widget_prev"
const val NAV_WIDGET_NEXT = "nav_widget_next"
private const val WIDGET_SAVE = "WIDGET_SAVE"

val FLAG_MUTABLE_COMPAT = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    PendingIntent.FLAG_MUTABLE
} else {
    PendingIntent.FLAG_UPDATE_CURRENT
}

fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    widgetBean: WidgetBean
) {
    trackSaveWidget(widgetBean)

    val imageList = widgetBean.imageList
    if (imageList.isEmpty()) {
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

    val remoteViews = RemoteViews(context.packageName, R.layout.widget_photo)
    remoteViews.removeAllViews(R.id.fl_picture_container)

    if (widgetInfo.widgetType == WidgetType.GIF) {
        val gifRemoteViews = RemoteViews(context.packageName, R.layout.layout_widget_flipper_gif)
        remoteViews.addView(R.id.fl_picture_container, gifRemoteViews)
        val serviceIntent = Intent(context, GifWidgetPhotoService::class.java)
        serviceIntent.type = Random.nextInt().toString()
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        remoteViews.setRemoteAdapter(R.id.vf_picture, serviceIntent)

        // Set widget link
        val linkIntent = createLinkIntent(context, linkInfo, imageList.first().imageUri)
        val linkPendingIntent =
            PendingIntent.getActivity(context, widgetId, linkIntent, FLAG_MUTABLE_COMPAT)
        remoteViews.setOnClickPendingIntent(android.R.id.background, linkPendingIntent)
    } else {
        // Create flipper remote views
        val flipperRemoteViews = createFlipperRemoteViews(context, autoPlayInterval.interval)
        remoteViews.addView(R.id.fl_picture_container, flipperRemoteViews)
        val serviceIntent = Intent(context, WidgetPhotoService::class.java)
        serviceIntent.type = Random.nextInt().toString()
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        remoteViews.setRemoteAdapter(R.id.vf_picture, serviceIntent)

        // Set widget link
        val linkIntent = createLinkIntent(context, linkInfo, null)
        val linkPendingIntent =
            PendingIntent.getActivity(context, widgetId, linkIntent, FLAG_MUTABLE_COMPAT)
        remoteViews.setPendingIntentTemplate(R.id.vf_picture, linkPendingIntent)

        if (isMultiImage) {
            remoteViews.setViewVisibility(R.id.photo_widget_info, View.VISIBLE)
            // Set page actions
            remoteViews.setOnClickPendingIntent(
                R.id.area_left,
                createWidgetNavPendingIntent(context, widgetId, NAV_WIDGET_PREV)
            )
            remoteViews.setOnClickPendingIntent(
                R.id.area_right,
                createWidgetNavPendingIntent(context, widgetId, NAV_WIDGET_NEXT)
            )
        } else {
            remoteViews.setViewVisibility(R.id.photo_widget_info, View.GONE)
        }
    }

    // Set widget padding.
    remoteViews.setViewPadding(
        R.id.fl_picture_container,
        frameWidth + leftPadding,
        frameWidth + topPadding,
        frameWidth + rightPadding,
        frameWidth + bottomPadding
    )

    // Set widget photo frame.
    if (widgetFrame != null && widgetFrame.type != WidgetFrameType.NONE) {
        remoteViews.setViewVisibility(R.id.iv_widget_background, View.VISIBLE)
        if (widgetFrame.type == WidgetFrameType.BUILD_IN || widgetFrame.type == WidgetFrameType.IMAGE) {
            remoteViews.setBackgroundColor(R.id.iv_widget_background, Color.TRANSPARENT)
            Glide.with(context)
                .asBitmap()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .load(widgetFrame.frameUri)
                .into(AppWidgetTarget(context, R.id.iv_widget_background, remoteViews, widgetId))
        } else if (widgetFrame.type == WidgetFrameType.COLOR) {
            remoteViews.setBackgroundColor(
                R.id.iv_widget_background,
                Color.parseColor(widgetFrame.frameColor)
            )
        }
    } else {
        remoteViews.setViewVisibility(R.id.iv_widget_background, View.GONE)
        remoteViews.setBackgroundColor(R.id.iv_widget_background, Color.TRANSPARENT)
    }

    try {
        appWidgetManager.updateAppWidget(widgetId, remoteViews)
        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.vf_picture)
    } catch (e: Exception) {
        logE("PhotoWidget", "updateAppWidget() -> Error:" + e.message, e)
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
): PendingIntent {
    val navIntent = Intent(context, PhotoWidgetProvider::class.java).apply {
        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        putExtra(EXTRA_NAV, navAction)
    }
    return PendingIntent.getBroadcast(context, Random.nextInt(), navIntent, FLAG_MUTABLE_COMPAT)
}

fun createFlipperRemoteViews(context: Context, interval: Int): RemoteViews {
    val defaultRemoteViews = RemoteViews(context.packageName, R.layout.layout_widget_flipper)
    if (interval < 0) {
        return defaultRemoteViews
    }
    val layoutName = "layout_widget_flipper_interval_${interval}"
    val layoutId = context.resources.getIdentifier(layoutName, "layout", context.packageName)
    if (layoutId <= 0) {
        logE("PhotoWidget", "Inflate flipper interval layout fail!")
        return defaultRemoteViews
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

suspend fun deleteWidgets(context: Context, widgetIds: IntArray) {
    val widgetDao = AppDatabase.getDatabase(context).widgetDao()
    for (widgetId in widgetIds) {
        widgetDao.deleteByWidgetId(widgetId)
        val outFile = File(context.filesDir, "widget_${widgetId}")
        withContext(Dispatchers.IO) {
            outFile.deleteRecursively()
        }
    }
}

private fun trackSaveWidget(widgetBean: WidgetBean) {
    try {
        EventStatistics.track(
            WIDGET_SAVE, mapOf(
                "LinkType" to widgetBean.linkInfo?.type?.value.toString(),
                "LinkUri" to widgetBean.linkInfo?.link.toString(),
                "WidgetType" to widgetBean.widgetInfo.widgetType.code,
                "WidgetPaddingLeft" to widgetBean.widgetInfo.leftPadding.toString(),
                "WidgetPaddingTop" to widgetBean.widgetInfo.topPadding.toString(),
                "WidgetPaddingRight" to widgetBean.widgetInfo.rightPadding.toString(),
                "WidgetPaddingBottom" to widgetBean.widgetInfo.bottomPadding.toString(),
                "WidgetRadius" to widgetBean.widgetInfo.widgetRadius.toString() + widgetBean.widgetInfo.widgetRadiusUnit.unitName,
                "WidgetTransparency" to widgetBean.widgetInfo.widgetTransparency.toString(),
                "WidgetAutoPlayInterval" to widgetBean.widgetInfo.autoPlayInterval.interval.toString(),
                "WidgetPhotoScaleType" to widgetBean.widgetInfo.photoScaleType.name,
                "WidgetImageSize" to widgetBean.imageList.size.toString(),
                "WidgetFrameType" to widgetBean.frame?.type?.name.toString(),
                "WidgetFrameUri" to widgetBean.frame?.frameUri.toString(),
                "WidgetFrameColor" to widgetBean.frame?.frameColor.toString(),
            )
        )
    } catch (e: Exception) {
        logE("SaveWidget", "TrackError:" + e.message, e)
    }
}