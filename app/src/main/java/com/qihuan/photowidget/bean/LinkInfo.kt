package com.qihuan.photowidget.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * LinkInfo
 * @author qi
 * @since 3/22/21
 */
@Parcelize
data class LinkInfo(
    val type: LinkType,
    val title: String,
    val description: String,
    var link: String
) : Parcelable {

    fun getAppName(): String {
        return link.split("/")[1]
    }

    fun getPackageName(): String {
        return link.split("/")[2]
    }

    companion object {
        fun of(link: String?): LinkInfo? {
            if (link.isNullOrEmpty()) {
                return null
            }
            return if (link.startsWith("${LinkType.OPEN_APP.value}/")) {
                val info = link.split("/")
                LinkInfo(
                    LinkType.OPEN_APP,
                    "打开应用: [ ${info[1]} ]",
                    "包名: ${info[2]}",
                    link
                )
            } else {
                LinkInfo(LinkType.OPEN_URL, "打开链接", "地址: $link", link)
            }
        }
    }
}