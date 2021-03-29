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
    val link: String,
) : Parcelable