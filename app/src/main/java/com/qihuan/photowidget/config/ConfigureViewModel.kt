package com.qihuan.photowidget.config

import android.app.Application
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.qihuan.photowidget.bean.WidgetInfo
import com.qihuan.photowidget.common.PhotoScaleType
import com.qihuan.photowidget.common.PlayInterval
import com.qihuan.photowidget.common.RadiusUnit
import java.io.File
import java.util.*

/**
 * ConfigureViewModel
 * @author qi
 * @since 12/16/20
 */
class ConfigureViewModel(
    application: Application,
    private val appWidgetId: Int
) : BaseConfigViewModel(application, appWidgetId) {

    val widgetTransparency by lazy { MutableLiveData(0f) }
    val autoPlayInterval by lazy { MutableLiveData(PlayInterval.NONE) }
    val photoScaleType by lazy { MutableLiveData(PhotoScaleType.CENTER_CROP) }

    override fun displayWidget(widgetInfo: WidgetInfo) {
        topPadding.value = widgetInfo.topPadding
        bottomPadding.value = widgetInfo.bottomPadding
        leftPadding.value = widgetInfo.leftPadding
        rightPadding.value = widgetInfo.rightPadding
        widgetRadius.value = widgetInfo.widgetRadius
        widgetRadiusUnit.value = widgetInfo.widgetRadiusUnit
        widgetTransparency.value = widgetInfo.widgetTransparency
        autoPlayInterval.value = widgetInfo.autoPlayInterval
        photoScaleType.value = widgetInfo.photoScaleType
    }

    override fun getCurrentWidgetInfo(): WidgetInfo {
        return WidgetInfo(
            widgetId = appWidgetId,
            topPadding = topPadding.value ?: 0f,
            bottomPadding = bottomPadding.value ?: 0f,
            leftPadding = leftPadding.value ?: 0f,
            rightPadding = rightPadding.value ?: 0f,
            widgetRadius = widgetRadius.value ?: 0f,
            widgetRadiusUnit = widgetRadiusUnit.value ?: RadiusUnit.ANGLE,
            widgetTransparency = widgetTransparency.value ?: 0f,
            autoPlayInterval = autoPlayInterval.value ?: PlayInterval.NONE,
            photoScaleType = photoScaleType.value ?: PhotoScaleType.CENTER_CROP,
        )
    }

    override fun onPostSaveFiles(widgetDir: File, uriList: List<Uri>) {
    }

    fun updatePhotoScaleType(value: PhotoScaleType) {
        photoScaleType.value = value
    }

    fun updateAutoPlayInterval(value: PlayInterval) {
        autoPlayInterval.value = value
    }

    fun swapImageList(fromPosition: Int, toPosition: Int) {
        val list = imageUriList.value ?: mutableListOf()
        Collections.swap(list, fromPosition, toPosition)
        imageUriList.value = list
    }
}