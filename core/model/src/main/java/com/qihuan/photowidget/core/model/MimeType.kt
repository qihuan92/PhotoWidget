package com.qihuan.photowidget.core.model

import android.graphics.Bitmap

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