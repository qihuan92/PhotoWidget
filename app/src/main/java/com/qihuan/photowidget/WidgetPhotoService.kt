package com.qihuan.photowidget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.qihuan.photowidget.db.AppDatabase
import com.qihuan.photowidget.ktx.dp

/**
 * PhotoImageService
 * @author qi
 * @since 12/9/20
 */

internal const val EXTRA_IMAGE_URI = "image_uri"
internal const val EXTRA_IMAGE_RADIUS = "image_radius"

class WidgetPhotoService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return WidgetPhotoViewFactory(applicationContext, intent)
    }
}

class WidgetPhotoViewFactory(
    private val context: Context,
    private val intent: Intent?
) : RemoteViewsService.RemoteViewsFactory {

    private val widgetInfoDao by lazy { AppDatabase.getDatabase(context).widgetInfoDao() }
    private val imageUriList by lazy { mutableListOf<Uri>() }
    private var radius = 0f
    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate() {
        widgetId = intent?.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
    }

    override fun onDataSetChanged() {
        imageUriList.clear()
        radius = 0f
        val widgetInfo = widgetInfoDao.selectByIdSync(widgetId)
        if (widgetInfo != null) {
            imageUriList.addAll(widgetInfo.uri)
            radius = widgetInfo.widgetRadius
        }
    }

    override fun onDestroy() {
        imageUriList.clear()
    }

    override fun getCount(): Int {
        return imageUriList.size
    }

    override fun getViewAt(position: Int): RemoteViews? {
        if (imageUriList.isNullOrEmpty()) {
            return null
        }
        val remoteViews = RemoteViews(context.packageName, R.layout.layout_widget_image)
        remoteViews.setImageViewBitmap(
            R.id.iv_picture,
            createWidgetBitmap(context, imageUriList[position], radius.dp)
        )
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