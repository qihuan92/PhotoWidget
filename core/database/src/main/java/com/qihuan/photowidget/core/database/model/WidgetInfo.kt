package com.qihuan.photowidget.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.qihuan.photowidget.core.model.PhotoScaleType
import com.qihuan.photowidget.core.model.PlayInterval
import com.qihuan.photowidget.core.model.RadiusUnit
import com.qihuan.photowidget.core.model.WidgetType

/**
 * PictureInfo
 *
 * @author qi
 * @since 11/19/20
 */
@Entity(tableName = "widget_info")
data class WidgetInfo(
    @PrimaryKey
    val widgetId: Int,
    val topPadding: Float,
    val bottomPadding: Float,
    val leftPadding: Float,
    val rightPadding: Float,
    val widgetRadius: Float,
    @ColumnInfo(defaultValue = "length")
    val widgetRadiusUnit: RadiusUnit = RadiusUnit.LENGTH,
    val widgetTransparency: Float = 0f,
    val autoPlayInterval: PlayInterval = PlayInterval.NONE,
    val widgetType: WidgetType = WidgetType.NORMAL,
    val linkInfo: String? = null,
    val photoScaleType: PhotoScaleType = PhotoScaleType.CENTER_CROP,
    val createTime: Long? = System.currentTimeMillis(),
)