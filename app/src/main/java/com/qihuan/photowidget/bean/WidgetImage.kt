package com.qihuan.photowidget.bean

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * WidgetImage
 * @author qi
 * @since 12/15/20
 */
@Entity(tableName = "widget_image")
data class WidgetImage(
    @PrimaryKey(autoGenerate = true)
    val imageId: Int? = null,
    val widgetId: Int,
    val imageUri: Uri,
    val createTime: Long,
    val sort: Int,
)
