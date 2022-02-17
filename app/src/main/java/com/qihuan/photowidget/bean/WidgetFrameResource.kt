package com.qihuan.photowidget.bean

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.qihuan.photowidget.common.WidgetFrameType

/**
 * WidgetFrameResource
 * @author qi
 * @since 2022/2/11
 */
@Entity(tableName = "widget_frame_resource")
data class WidgetFrameResource(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val name: String,
    val description: String?,
    val type: WidgetFrameType,
    val frameUri: Uri? = null,
    val frameColor: String? = null,
    val createTime: Long = System.currentTimeMillis(),
)
