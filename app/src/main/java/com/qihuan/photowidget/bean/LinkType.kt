package com.qihuan.photowidget.bean

/**
 * LinkType
 * @author qi
 * @since 3/22/21
 */
enum class LinkType(
    val value: String,
) {
    OPEN_URL("openUrl"),
    OPEN_APP("openApp"),
    ;

    companion object {
        fun get(value: String?): LinkType? {
            return values().firstOrNull { it.value == value }
        }
    }
}