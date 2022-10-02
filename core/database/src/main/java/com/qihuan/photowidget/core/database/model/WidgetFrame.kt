package com.qihuan.photowidget.core.database.model

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.qihuan.photowidget.core.model.WidgetFrameType

/**
 * WidgetFrame
 * @author qi
 * @since 2022/2/11
 */
@Entity(tableName = "widget_frame")
data class WidgetFrame(
    @PrimaryKey
    val widgetId: Int,
    val frameUri: Uri? = null,
    val frameColor: String? = null,
    val width: Float,
    val type: WidgetFrameType
)