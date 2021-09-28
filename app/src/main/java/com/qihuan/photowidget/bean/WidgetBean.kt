package com.qihuan.photowidget.bean

import androidx.room.Embedded
import androidx.room.Relation

/**
 * WidgetBean
 * @author qi
 * @since 12/15/20
 */
data class WidgetBean(
    @Embedded
    val widgetInfo: WidgetInfo,

    @Relation(parentColumn = "widgetId", entityColumn = "widgetId")
    var _imageList: List<WidgetImage>,

    @Relation(parentColumn = "widgetId", entityColumn = "widgetId")
    var linkInfo: LinkInfo?,
) {
    val imageList get() = _imageList.sortedBy { it.sort }
}
