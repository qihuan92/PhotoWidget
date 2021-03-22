package com.qihuan.photowidget.bean

import androidx.annotation.DrawableRes

/**
 * LinkInfo
 * @author qi
 * @since 3/22/21
 */
data class LinkInfo(
    @DrawableRes val icon: Int,
    val title: String,
    val description: String,
    val link: String,
)