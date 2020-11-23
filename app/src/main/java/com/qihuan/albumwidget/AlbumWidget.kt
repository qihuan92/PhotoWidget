package com.qihuan.albumwidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.graphics.*
import android.provider.MediaStore
import android.widget.RemoteViews


/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [AlbumWidgetConfigureActivity]
 */
class AlbumWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // 更新微件
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // 删除一些缓存数据
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    pictureInfo: PictureInfo
) {
    val views = RemoteViews(context.packageName, R.layout.album_widget)
    val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, pictureInfo.uri)
    views.setImageViewBitmap(R.id.iv_picture, getRoundedBitmap(bitmap, pictureInfo.widgetRadius))

    val horizontalPadding = pictureInfo.horizontalPadding
    val verticalPadding = pictureInfo.verticalPadding
    views.setViewPadding(
        R.id.root,
        horizontalPadding,
        verticalPadding,
        horizontalPadding,
        verticalPadding
    )
    appWidgetManager.updateAppWidget(appWidgetId, views)
}

private fun getRoundedBitmap(bitmap: Bitmap, radius: Int): Bitmap {
    val radiusPx = radius.toFloat() * 2
    val roundedBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)

    val canvas = Canvas(roundedBitmap)
    val paint = Paint()
    paint.isAntiAlias = true

    val rect = Rect(0, 0, bitmap.width, bitmap.height)
    val rectF = RectF(rect)

    canvas.drawARGB(0, 0, 0, 0)
    canvas.drawRoundRect(rectF, radiusPx, radiusPx, paint)
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

    canvas.drawBitmap(bitmap, rect, rect, paint)
    return roundedBitmap
}