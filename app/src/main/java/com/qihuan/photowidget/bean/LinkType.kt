package com.qihuan.photowidget.bean

import androidx.annotation.DrawableRes
import com.qihuan.photowidget.R
import com.qihuan.photowidget.view.ItemSelectionDialog

/**
 * LinkType
 * @author qi
 * @since 3/22/21
 */
enum class LinkType(
    val value: String,
    val description: String,
    @DrawableRes val icon: Int,
) : ItemSelectionDialog.Item {
    OPEN_APP("openApp", "打开应用", R.drawable.ic_round_apps_24),
    OPEN_URL("openUrl", "打开URL", R.drawable.ic_round_link_24),
    OPEN_ALBUM("openAlbum", "打开相册", R.drawable.ic_round_photo_album_24),
    ;

    override fun getIcon(): Int? {
        return icon
    }

    override fun getItemText(): String {
        return description
    }

    companion object {
        fun get(value: String?): LinkType? {
            return values().firstOrNull { it.value == value }
        }
    }
}