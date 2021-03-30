package com.qihuan.photowidget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.net.toFile
import com.qihuan.photowidget.bean.WidgetImage
import com.qihuan.photowidget.db.AppDatabase
import com.qihuan.photowidget.ktx.dp
import com.qihuan.photowidget.ktx.getRoundedBitmap

/**
 * PhotoImageService
 * @author qi
 * @since 12/9/20
 */

class WidgetPhotoService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        return WidgetPhotoViewFactory(applicationContext, intent)
    }
}

class WidgetPhotoViewFactory(
    private val context: Context,
    private val intent: Intent?
) : RemoteViewsService.RemoteViewsFactory {

    private val widgetDao by lazy { AppDatabase.getDatabase(context).widgetDao() }
    private val imageList by lazy { mutableListOf<WidgetImage>() }
    private var radius = 0f
    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate() {
        widgetId = intent?.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
    }

    override fun onDataSetChanged() {
        imageList.clear()
        radius = 0f
        val widgetBean = widgetDao.selectByIdSync(widgetId)
        if (widgetBean != null) {
            imageList.addAll(widgetBean.imageList)
            val widgetInfo = widgetBean.widgetInfo
            radius = widgetInfo.widgetRadius
        }
    }

    override fun onDestroy() {
        imageList.clear()
    }

    override fun getCount(): Int {
        return imageList.size
    }

    override fun getViewAt(position: Int): RemoteViews? {
        if (imageList.isNullOrEmpty()) {
            return null
        }
        val remoteViews = RemoteViews(context.packageName, R.layout.layout_widget_image)
        val uri = imageList[position].imageUri
        if (uri.toFile().exists()) {
            remoteViews.setImageViewBitmap(
                R.id.iv_picture,
                uri.getRoundedBitmap(context, radius.dp)
            )
        } else {
            remoteViews.setImageViewResource(R.id.iv_picture, R.drawable.shape_photo_404)
        }
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