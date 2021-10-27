package com.qihuan.photowidget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.qihuan.photowidget.bean.LinkInfo
import com.qihuan.photowidget.bean.WidgetInfo
import com.qihuan.photowidget.db.AppDatabase
import com.qihuan.photowidget.ktx.toBitmapsSync

/**
 * GifWidgetPhotoService
 * @author qi
 * @since 2021-10-27
 */

class GifWidgetPhotoService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return GifWidgetPhotoViewFactory(applicationContext, intent)
    }
}

class GifWidgetPhotoViewFactory(
    private val context: Context,
    private val intent: Intent?
) : RemoteViewsService.RemoteViewsFactory {

    private val widgetDao by lazy { AppDatabase.getDatabase(context).widgetDao() }
    private var widgetInfo: WidgetInfo? = null
    private var linkInfo: LinkInfo? = null
    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private val bitmapList by lazy { mutableListOf<Bitmap>() }

    override fun onCreate() {
        widgetId = intent?.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
    }

    override fun onDataSetChanged() {
        bitmapList.clear()
        val widgetBean = widgetDao.selectByIdSync(widgetId)
        if (widgetBean != null) {
            bitmapList.addAll(widgetBean.imageList.first().imageUri.toBitmapsSync())
            widgetInfo = widgetBean.widgetInfo
            linkInfo = widgetBean.linkInfo
        }
    }

    override fun onDestroy() {
        bitmapList.clear()
    }

    override fun getCount(): Int {
        return bitmapList.size
    }

    override fun getViewAt(position: Int): RemoteViews? {
        if (bitmapList.isNullOrEmpty()) {
            return null
        }

        val remoteViews = RemoteViews(context.packageName, R.layout.layout_widget_image)
        remoteViews.setImageViewBitmap(R.id.iv_picture, bitmapList[position])
        return remoteViews
    }

    override fun getLoadingView(): RemoteViews {
        return RemoteViews(context.packageName, R.layout.layout_widget_image_loading)
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }
}