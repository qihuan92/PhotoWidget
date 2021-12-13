package com.qihuan.photowidget.bean

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.qihuan.photowidget.common.PhotoScaleType
import com.qihuan.photowidget.common.PlayInterval
import com.qihuan.photowidget.common.RadiusUnit
import com.qihuan.photowidget.common.WidgetType

/**
 * PictureInfo
 * Dao: [com.qihuan.photowidget.db.WidgetInfoDao]
 * Database:[com.qihuan.photowidget.db.AppDatabase]
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
    @ColumnInfo(defaultValue = "angle")
    val widgetRadiusUnit: RadiusUnit = RadiusUnit.ANGLE,
    val widgetTransparency: Float = 0f,
    val autoPlayInterval: PlayInterval = PlayInterval.NONE,
    val widgetType: WidgetType = WidgetType.NORMAL,
    val linkInfo: String? = null,
    val photoScaleType: PhotoScaleType = PhotoScaleType.CENTER_CROP,
    val createTime: Long? = System.currentTimeMillis(),
)