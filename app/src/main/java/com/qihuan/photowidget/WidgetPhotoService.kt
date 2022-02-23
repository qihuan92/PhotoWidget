package com.qihuan.photowidget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.TypedValue
import android.widget.ImageView
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.net.toFile
import com.bumptech.glide.Glide
import com.qihuan.photowidget.bean.LinkInfo
import com.qihuan.photowidget.bean.WidgetImage
import com.qihuan.photowidget.bean.WidgetInfo
import com.qihuan.photowidget.db.AppDatabase
import com.qihuan.photowidget.ktx.calculateRadiusPx
import com.qihuan.photowidget.ktx.dp
import com.qihuan.photowidget.ktx.toRoundedBitmap

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
    private var widgetInfo: WidgetInfo? = null
    private var linkInfo: LinkInfo? = null
    private val imageList by lazy { mutableListOf<WidgetImage>() }
    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private val appWidgetManager by lazy { AppWidgetManager.getInstance(context) }

    override fun onCreate() {
        widgetId = intent?.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
    }

    override fun onDataSetChanged() {
        imageList.clear()
        val widgetBean = widgetDao.selectByIdSync(widgetId)
        if (widgetBean != null) {
            imageList.addAll(widgetBean.imageList)
            widgetInfo = widgetBean.widgetInfo
            linkInfo = widgetBean.linkInfo
        }
    }

    override fun onDestroy() {
        imageList.clear()
    }

    override fun getCount(): Int {
        return imageList.size
    }

    override fun getViewAt(position: Int): RemoteViews? {
        val widgetInfo = this.widgetInfo ?: return null
        if (imageList.isNullOrEmpty()) {
            return null
        }

        val scaleType = widgetInfo.photoScaleType.scaleType
        val imageUri = imageList[position].imageUri
        val radius = widgetInfo.widgetRadius
        val radiusUnit = widgetInfo.widgetRadiusUnit
        val remoteViews = createImageRemoteViews(context, scaleType)
        val imageWidth = appWidgetManager.getWidgetImageWidth(widgetInfo).toFloat().dp
        val imageHeight = appWidgetManager.getWidgetImageHeight(widgetInfo).toFloat().dp
        if (imageUri.toFile().exists()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && scaleType == ImageView.ScaleType.CENTER_CROP) {
                val bitmap = Glide.with(context).asBitmap().load(imageUri).submit().get()
                remoteViews.setImageViewBitmap(R.id.iv_picture, bitmap)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    remoteViews.setViewOutlinePreferredRadius(
                        R.id.iv_picture,
                        calculateRadiusPx(imageWidth, imageHeight, radius, radiusUnit).toFloat(),
                        TypedValue.COMPLEX_UNIT_PX
                    )
                    remoteViews.setBoolean(R.id.iv_picture, "setClipToOutline", true)
                }
            } else {
                val imageBitmap =
                    imageUri.toRoundedBitmap(
                        context,
                        radius,
                        radiusUnit,
                        scaleType,
                        imageWidth,
                        imageHeight
                    )
                remoteViews.setImageViewBitmap(R.id.iv_picture, imageBitmap)
            }
            remoteViews.setOnClickFillInIntent(
                R.id.iv_picture,
                createLinkIntent(context, linkInfo, imageUri)
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