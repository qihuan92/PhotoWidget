package com.qihuan.photowidget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.net.toFile
import com.bumptech.glide.Glide
import com.qihuan.photowidget.bean.LinkInfo
import com.qihuan.photowidget.bean.WidgetInfo
import com.qihuan.photowidget.db.AppDatabase
import java.io.File

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
    private val imagePathList = mutableListOf<String>()

    override fun onCreate() {
        widgetId = intent?.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
    }

    override fun onDataSetChanged() {
        imagePathList.clear()
        val widgetBean = widgetDao.selectByIdSync(widgetId)
        if (widgetBean != null) {
            val gifFile = widgetBean.imageList.first().imageUri.toFile()

            val gifDir = File(gifFile.parent, gifFile.nameWithoutExtension)
            if (gifDir.exists() && gifDir.isDirectory) {
                val files = gifDir.listFiles()
                files?.sortBy { it.nameWithoutExtension.toInt() }
                files?.forEach {
                    imagePathList.add(it.path)
                }
            }
            widgetInfo = widgetBean.widgetInfo
            linkInfo = widgetBean.linkInfo
        }
    }

    override fun onDestroy() {
        imagePathList.clear()
    }

    override fun getCount(): Int {
        return imagePathList.size
    }

    override fun getViewAt(position: Int): RemoteViews? {
        if (imagePathList.isNullOrEmpty()) {
            return null
        }

        val path = imagePathList[position]

        // Scale image size
        val option = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(path, option)
        val showWidth = option.outWidth shr 1
        val showHeight = option.outHeight shr 1

        val bitmap = Glide.with(context).asBitmap().load(path).submit(showWidth, showHeight).get()
        val remoteViews = RemoteViews(context.packageName, R.layout.layout_widget_image)
        remoteViews.setImageViewBitmap(R.id.iv_picture, bitmap)
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