package com.qihuan.photowidget.config

import android.app.Application
import android.net.Uri
import androidx.core.net.toFile
import com.qihuan.photowidget.bean.WidgetInfo
import com.qihuan.photowidget.common.PhotoScaleType
import com.qihuan.photowidget.common.RadiusUnit
import com.qihuan.photowidget.common.WidgetType
import com.qihuan.photowidget.ktx.saveGifFramesToDir
import java.io.File

/**
 * GifConfigureViewModel
 * @author qi
 * @since 2021-10-27
 */
class GifConfigureViewModel(
    application: Application,
    private val appWidgetId: Int
) : BaseConfigViewModel(application, appWidgetId) {

    override fun addImage(uri: Uri) {
        clearImageList()
        super.addImage(uri)
    }

    override fun displayWidget(widgetInfo: WidgetInfo) {
        topPadding.value = widgetInfo.topPadding
        bottomPadding.value = widgetInfo.bottomPadding
        leftPadding.value = widgetInfo.leftPadding
        rightPadding.value = widgetInfo.rightPadding
        widgetRadius.value = widgetInfo.widgetRadius
        widgetRadiusUnit.value = widgetInfo.widgetRadiusUnit
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
            widgetTransparency = 0f,
            photoScaleType = PhotoScaleType.CENTER_CROP,
            widgetType = WidgetType.GIF,
        )
    }

    override fun onPostSaveFiles(widgetDir: File, uriList: List<Uri>) {
        uriList.forEach { uri ->
            uri.saveGifFramesToDir(
                File(widgetDir, uri.toFile().nameWithoutExtension),
                widgetRadius.value ?: 0f,
                widgetRadiusUnit.value ?: RadiusUnit.ANGLE
            )
        }
    }
}