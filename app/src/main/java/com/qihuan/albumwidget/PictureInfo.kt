package com.qihuan.albumwidget

import android.net.Uri

/**
 * PictureInfo
 * @author qi
 * @since 11/19/20
 */
data class PictureInfo(
    val uri: Uri,
    val verticalPadding: Int,
    val horizontalPadding: Int,
    val widgetRadius: Int,
)