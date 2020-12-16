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
import com.qihuan.photowidget.bean.WidgetBean
import com.qihuan.photowidget.db.AppDatabase
import com.qihuan.photowidget.ktx.deleteDir
import com.qihuan.photowidget.ktx.dp
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

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val widgetInfoDao = AppDatabase.getDatabase(context).widgetInfoDao()
        // 删除一些缓存数据
        GlobalScope.launch {
            for (appWidgetId in appWidgetIds) {
                widgetInfoDao.deleteById(appWidgetId)

                val outFile = File(context.filesDir, "widget_${appWidgetId}")
                outFile.deleteDir()
            }
        }
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    widgetBean: WidgetBean
) {
    val widgetInfo = widgetBean.widgetInfo
    val widgetId = widgetInfo.widgetId

    val views = RemoteViews(context.packageName, R.layout.photo_widget)
    views.setRemoteAdapter(R.id.vf_picture, Intent(context, WidgetPhotoService::class.java).apply {
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
    })

    val horizontalPadding = widgetInfo.horizontalPadding.dp
    val verticalPadding = widgetInfo.verticalPadding.dp
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
    appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.vf_picture)
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