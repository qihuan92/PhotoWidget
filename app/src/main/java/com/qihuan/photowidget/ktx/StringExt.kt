package com.qihuan.photowidget.ktx

import com.qihuan.photowidget.bean.LinkInfo
import com.qihuan.photowidget.bean.LinkType

/**
 * StringExt
 * @author qi
 * @since 3/22/21
 */
fun String.parseLink(): LinkInfo {
    return if (isOpenAppLink()) {
        val info = split("/")
        LinkInfo(
            LinkType.OPEN_APP,
            "打开应用: [ ${info[1]} ]",
            "包名: ${info[2]}",
            this
        )
    } else {
        LinkInfo(LinkType.URL, "打开链接", "地址: $this", this)
    }
}

fun String.isOpenAppLink(): Boolean {
    return startsWith("openApp/")
}