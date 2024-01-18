package com.qihuan.photowidget.feature.about.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes

data class LinkItem(
    val name: String,
    val link: String,
    @ColorRes val color: Int,
    @DrawableRes val icon: Int,
)