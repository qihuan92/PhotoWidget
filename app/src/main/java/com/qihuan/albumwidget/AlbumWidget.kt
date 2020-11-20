package com.qihuan.albumwidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.graphics.ImageDecoder
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
    val source = ImageDecoder.createSource(context.contentResolver, pictureInfo.uri)
    val bitmap = ImageDecoder.decodeBitmap(source)
    views.setImageViewBitmap(R.id.iv_picture, bitmap)

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