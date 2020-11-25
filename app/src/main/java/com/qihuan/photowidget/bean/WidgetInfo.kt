package com.qihuan.photowidget.bean

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * PictureInfo
 * @author qi
 * @since 11/19/20
 */
@Entity(tableName = "widget_info")
data class WidgetInfo(
    @PrimaryKey
    val widgetId: Int,
    var uri: Uri,
    val verticalPadding: Int,
    val horizontalPadding: Int,
    val widgetRadius: Int,
)