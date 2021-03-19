package com.qihuan.photowidget.bean

import android.graphics.drawable.Drawable

/**
 * InstalledAppInfo
 * @author qi
 * @since 3/19/21
 */
data class InstalledAppInfo(
    val icon: Drawable,
    val appName: String?,
    val packageName: String,
)
