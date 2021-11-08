package com.qihuan.photowidget.common

import android.graphics.Bitmap
import android.os.Build

/**
 * Constants
 * @author qi
 * @since 2021/8/20
 */
const val TEMP_DIR_NAME = "temp"
const val MAIN_PAGE_SPAN_COUNT = 2
const val DEFAULT_COMPRESSION_QUALITY = 75
const val INVALID_AUTO_REFRESH_INTERVAL = -1L
const val KEY_AUTO_REFRESH_INTERVAL = "autoRefreshInterval"

object License {
    const val MIT = "MIT License"
    const val APACHE_2 = "Apache Software License 2.0"
    const val GPL_V3 = "GNU general public license Version 3"
}

object FileExtension {
    const val JPEG = "jpg"
    const val PNG = "png"
    const val WEBP = "webp"
}

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

enum class MimeType(
    val mimeTypeName: String,
    val extensions: Set<String>,
    val compressFormat: Bitmap.CompressFormat,
) {
    JPEG("image/jpeg", setOf("jpg", "jpeg"), CompressFormatCompat.JPEG),
    PNG("image/png", setOf("png"), CompressFormatCompat.PNG),
    GIF("image/gif", setOf("gif"), CompressFormatCompat.PNG),
    WEBP("image/webp", setOf("webp"), CompressFormatCompat.WEBP_LOSSLESS),
    BMP("image/x-ms-bmp", setOf("bmp"), CompressFormatCompat.WEBP_LOSSLESS),
    ;

    companion object {
        fun getCompressFormatByMimeType(mimeType: String?): Bitmap.CompressFormat {
            return values().firstOrNull { it.mimeTypeName == mimeType }?.compressFormat
                ?: CompressFormatCompat.WEBP_LOSSLESS
        }
    }
}