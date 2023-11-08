package com.qihuan.photowidget.core.database.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.qihuan.photowidget.core.model.LinkType
import kotlinx.parcelize.Parcelize

/**
 * LinkInfo
 * @author qi
 * @since 3/22/21
 */
@Entity(tableName = "link_info")
@Parcelize
data class LinkInfo(
    @PrimaryKey
    val widgetId: Int,
    val type: LinkType,
    val title: String,
    val description: String,
    var link: String
) : Parcelable