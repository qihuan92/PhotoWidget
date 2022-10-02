package com.qihuan.photowidget.core.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/**
 * LinkType
 * @author qi
 * @since 3/22/21
 */
enum class LinkType(
    val value: String,
    @StringRes override val text: Int,
    @DrawableRes override val icon: Int?,
) : SelectionItem {
    OPEN_APP("openApp", R.string.link_type_open_app, R.drawable.ic_round_apps_24),
    OPEN_URL("openUrl", R.string.link_type_open_url, R.drawable.ic_round_link_24),
    OPEN_ALBUM("openAlbum", R.string.link_type_open_album, R.drawable.ic_round_photo_album_24),
    OPEN_FILE("openFile", R.string.link_type_open_file, R.drawable.ic_round_insert_drive_file_24),
    ;

    companion object {
        fun get(value: String?): LinkType? {
            return values().firstOrNull { it.value == value }
        }
    }
}