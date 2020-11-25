package com.qihuan.photowidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.RemoteViews
import com.qihuan.photowidget.bean.WidgetInfo
import com.qihuan.photowidget.db.AppDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File


/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [PhotoWidgetConfigureActivity]
 */
class PhotoWidgetProvider : AppWidgetProvider() {
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
    val widgetId = widgetInfo.widgetId

    val views = RemoteViews(context.packageName, R.layout.photo_widget)
    views.setImageViewBitmap(
        R.id.iv_picture,
        createWidgetBitmap(context, widgetInfo.uri, widgetInfo.widgetRadius)
    )

    val horizontalPadding = widgetInfo.horizontalPadding
    val verticalPadding = widgetInfo.verticalPadding
    views.setViewPadding(
        R.id.root,
        horizontalPadding,
        verticalPadding,
        horizontalPadding,
        verticalPadding
    )

    val intent = Intent(context, PhotoWidgetConfigureActivity::class.java).apply {
        val extras = Bundle().apply {
            putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        }
        putExtras(extras)
    }
    views.setOnClickPendingIntent(
        R.id.iv_info,
        PendingIntent.getActivity(context, widgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    )

    appWidgetManager.updateAppWidget(widgetId, views)
}

internal fun createWidgetBitmap(context: Context, uri: Uri, radius: Int): Bitmap {
    val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
    return getRoundedBitmap(bitmap, radius)
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