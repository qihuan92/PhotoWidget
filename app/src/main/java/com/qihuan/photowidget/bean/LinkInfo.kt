package com.qihuan.photowidget.bean

import android.content.Context
import android.net.Uri
import android.os.Parcelable
import androidx.documentfile.provider.DocumentFile
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.qihuan.photowidget.R
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

fun Context.createAlbumLink(appWidgetId: Int): LinkInfo = LinkInfo(
    appWidgetId,
    LinkType.OPEN_ALBUM,
    getString(R.string.widget_link_open_album_title),
    getString(R.string.widget_link_open_album_description),
    ""
)

fun Context.createFileLink(appWidgetId: Int, uri: Uri): LinkInfo {
    val fileName = DocumentFile.fromSingleUri(this, uri)?.name
    return LinkInfo(
        appWidgetId,
        LinkType.OPEN_FILE,
        getString(R.string.widget_link_open_file_title),
        String.format(getString(R.string.widget_link_open_file_description), fileName),
        uri.toString()
    )
}