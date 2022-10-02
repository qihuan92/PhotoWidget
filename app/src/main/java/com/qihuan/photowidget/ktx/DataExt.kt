package com.qihuan.photowidget.ktx

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.qihuan.photowidget.R
import com.qihuan.photowidget.core.database.model.LinkInfo
import com.qihuan.photowidget.core.model.LinkType

fun Context.createAlbumLink(appWidgetId: Int): LinkInfo =
    LinkInfo(
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