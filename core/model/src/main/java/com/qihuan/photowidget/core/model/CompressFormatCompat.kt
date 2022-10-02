package com.qihuan.photowidget.core.model

import android.graphics.Bitmap
import android.os.Build

@Suppress("DEPRECATION")
object CompressFormatCompat {
    val WEBP_LOSSY = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Bitmap.CompressFormat.WEBP_LOSSY
    } else {
        Bitmap.CompressFormat.WEBP
    }

    val WEBP_LOSSLESS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Bitmap.CompressFormat.WEBP_LOSSLESS
    } else {
        Bitmap.CompressFormat.WEBP
    }

    val JPEG = Bitmap.CompressFormat.JPEG
    val PNG = Bitmap.CompressFormat.PNG
}