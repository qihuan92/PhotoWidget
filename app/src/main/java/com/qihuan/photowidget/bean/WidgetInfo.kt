package com.qihuan.photowidget.bean

import android.net.Uri
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
    var uri: List<Uri>,
    val verticalPadding: Float,
    val horizontalPadding: Float,
    val widgetRadius: Float,
    val autoPlay: Boolean,
    val autoPlayInterval: Int?
)