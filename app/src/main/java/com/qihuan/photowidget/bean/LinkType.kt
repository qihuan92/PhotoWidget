package com.qihuan.photowidget.bean

import com.qihuan.photowidget.view.ItemSelectionDialog

/**
 * LinkType
 * @author qi
 * @since 3/22/21
 */
enum class LinkType(
    val value: String,
    val description: String,
) : ItemSelectionDialog.Item {
    OPEN_APP("openApp", "打开应用"),
    OPEN_URL("openUrl", "打开URL"),
    ;

    override fun getItemText(): String {
        return description
    }

    companion object {
        fun get(value: String?): LinkType? {
            return values().firstOrNull { it.value == value }
        }
    }
}