package com.qihuan.photowidget.core.database.model

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
    var imageUri: Uri,
    val createTime: Long,
    var sort: Int,
)
