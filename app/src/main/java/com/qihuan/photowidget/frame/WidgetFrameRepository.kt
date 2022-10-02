package com.qihuan.photowidget.frame

import android.app.Application
import android.net.Uri
import com.qihuan.photowidget.bean.WidgetFrameResource
import com.qihuan.photowidget.core.model.WidgetFrameType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * WidgetFrameRepository
 * @author qi
 * @since 2022/2/14
 */
class WidgetFrameRepository(private val context: Application) {

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun getWidgetFrameResourceList(): List<WidgetFrameResource> {
        val list = mutableListOf<WidgetFrameResource>()
        withContext(Dispatchers.IO) {
            list.add(WidgetFrameResource(1, "无", "无相框", WidgetFrameType.NONE, null, null))
            list.add(WidgetFrameResource(2, "选择图片", "自定义图片", WidgetFrameType.IMAGE, null, null))
            list.add(WidgetFrameResource(3, "选择颜色", "自定义颜色", WidgetFrameType.COLOR, null, null))

            val headSize = list.size
            val frameAssetList = context.assets.list("frame")
            frameAssetList?.forEachIndexed { index, path ->
                list.add(
                    WidgetFrameResource(
                        headSize + index + 1,
                        path,
                        path,
                        WidgetFrameType.BUILD_IN,
                        Uri.parse("file:///android_asset/frame/${path}")
                    )
                )
            }
        }
        return list
    }
}