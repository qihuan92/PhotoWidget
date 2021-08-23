package com.qihuan.photowidget.bean

import androidx.room.Entity
import androidx.room.PrimaryKey

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
    val verticalPadding: Float,
    val horizontalPadding: Float,
    val widgetRadius: Float,
    val widgetTransparency: Float = 0f,
    val autoPlayInterval: PlayInterval = PlayInterval.NONE,
    val linkInfo: LinkInfo?,
    val photoScaleType: PhotoScaleType,
    val createTime: Long? = System.currentTimeMillis(),
)