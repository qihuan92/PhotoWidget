package com.qihuan.albumwidget

import android.net.Uri

/**
 * CropPictureInfo
 * @author qi
 * @since 11/23/20
 */
data class CropPictureInfo(
    val inUri: Uri,
    val outUri: Uri
)