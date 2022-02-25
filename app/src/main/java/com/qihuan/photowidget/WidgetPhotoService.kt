package com.qihuan.photowidget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.ImageView
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.net.toFile
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.target.Target
import com.qihuan.photowidget.bean.LinkInfo
import com.qihuan.photowidget.bean.WidgetImage
import com.qihuan.photowidget.bean.WidgetInfo
import com.qihuan.photowidget.db.AppDatabase
import com.qihuan.photowidget.ktx.calculateRadiusPx
import com.qihuan.photowidget.ktx.dp

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
        val widgetWidth = appWidgetManager.getWidgetImageWidth(widgetInfo).toFloat().dp
        val widgetHeight = appWidgetManager.getWidgetImageHeight(widgetInfo).toFloat().dp
        if (imageUri.toFile().exists()) {
            var targetWidth = Target.SIZE_ORIGINAL
            var targetHeight = Target.SIZE_ORIGINAL
            val transformList = arrayListOf<Transformation<Bitmap>>()

            if (widgetWidth > 0 && widgetHeight > 0 && scaleType == ImageView.ScaleType.CENTER_CROP) {
                targetWidth = widgetWidth
                targetHeight = widgetHeight
                transformList.add(CenterCrop())
            }

            if (radius > 0) {
                val calculateWidth: Int
                val calculateHeight: Int
                if (widgetWidth > 0 && widgetHeight > 0 && scaleType == ImageView.ScaleType.CENTER_CROP) {
                    calculateWidth = widgetWidth
                    calculateHeight = widgetHeight
                } else {
                    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeFile(imageUri.path, options)
                    calculateWidth = options.outWidth
                    calculateHeight = options.outHeight
                }

                val calculateRadius =
                    calculateRadiusPx(calculateWidth, calculateHeight, radius, radiusUnit)

                if (calculateRadius > 0) {
                    transformList.add(RoundedCorners(calculateRadius))
                }
            }

            val bitmap = Glide.with(context)
                .asBitmap()
                .load(imageUri)
                .transform(*transformList.toTypedArray())
                .submit(targetWidth, targetHeight)
                .get()
            remoteViews.setImageViewBitmap(R.id.iv_picture, bitmap)

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