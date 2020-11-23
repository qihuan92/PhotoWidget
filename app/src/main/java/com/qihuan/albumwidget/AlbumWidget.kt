package com.qihuan.albumwidget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.graphics.*
import android.provider.MediaStore
import android.widget.RemoteViews
import com.qihuan.albumwidget.bean.WidgetInfo
import com.qihuan.albumwidget.db.AppDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File


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
        val widgetInfoDao = AppDatabase.getDatabase(context).widgetInfoDao()
        GlobalScope.launch {
            // 更新微件
            for (appWidgetId in appWidgetIds) {
                val widgetInfo = widgetInfoDao.selectById(appWidgetId)
                if (widgetInfo != null) {
                    updateAppWidget(context, appWidgetManager, widgetInfo)
                }
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val widgetInfoDao = AppDatabase.getDatabase(context).widgetInfoDao()
        // 删除一些缓存数据
        GlobalScope.launch {
            for (appWidgetId in appWidgetIds) {
                widgetInfoDao.deleteById(appWidgetId)

                val outFile = File(context.filesDir, "widget_${appWidgetId}.png")
                if (outFile.exists()) {
                    outFile.delete()
                }
            }
        }
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    widgetInfo: WidgetInfo
) {
    val views = RemoteViews(context.packageName, R.layout.album_widget)
    val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, widgetInfo.uri)
    views.setImageViewBitmap(R.id.iv_picture, getRoundedBitmap(bitmap, widgetInfo.widgetRadius))

    val horizontalPadding = widgetInfo.horizontalPadding
    val verticalPadding = widgetInfo.verticalPadding
    views.setViewPadding(
        R.id.root,
        horizontalPadding,
        verticalPadding,
        horizontalPadding,
        verticalPadding
    )
    appWidgetManager.updateAppWidget(widgetInfo.widgetId, views)
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