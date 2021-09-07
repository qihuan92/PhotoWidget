package com.qihuan.photowidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.ImageView
import android.widget.RemoteViews
import androidx.core.net.toFile
import com.qihuan.photowidget.bean.LinkInfo
import com.qihuan.photowidget.bean.LinkType
import com.qihuan.photowidget.bean.PlayInterval
import com.qihuan.photowidget.bean.WidgetBean
import com.qihuan.photowidget.ktx.dp
import com.qihuan.photowidget.ktx.getRoundedBitmap
import com.qihuan.photowidget.ktx.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    val isMultiImage = imageList.size > 1
    val widgetInfo = widgetBean.widgetInfo
    val widgetId = widgetInfo.widgetId
    val linkInfo = widgetInfo.linkInfo
    val autoPlayInterval = widgetInfo.autoPlayInterval
    val horizontalPadding = widgetInfo.horizontalPadding.dp
    val verticalPadding = widgetInfo.verticalPadding.dp
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
            val width = appWidgetManager.getAppWidgetOptions(widgetId)
                .getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
            val height = appWidgetManager.getAppWidgetOptions(widgetId)
                .getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)
            remoteViews.loadImage(
                context,
                imageUri,
                scaleType,
                widgetRadius,
                widgetTransparency,
                width,
                height
            )
        }
    }

    remoteViews.setViewPadding(
        R.id.root, horizontalPadding, verticalPadding, horizontalPadding, verticalPadding
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
        intent = packageManager.getLaunchIntentForPackage(linkInfo.getPackageName())
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
        PendingIntent.FLAG_UPDATE_CURRENT
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

    val imageBitmap = uri.getRoundedBitmap(
        context, radius.dp, scaleType, width.toFloat().dp, height.toFloat().dp
    )

    setImageViewBitmap(R.id.iv_picture, imageBitmap)

    val alpha = (255 * (1f - transparency / 100f)).toInt()
    setInt(R.id.iv_picture, "setImageAlpha", alpha)
}