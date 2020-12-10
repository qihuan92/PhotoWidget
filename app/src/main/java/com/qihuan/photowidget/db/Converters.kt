package com.qihuan.photowidget.db

import android.net.Uri
import androidx.core.net.toUri
import androidx.room.TypeConverter

/**
 * Converters
 * @author qi
 * @since 2020/8/11
 */
class Converters {
    @TypeConverter
    fun revertUriList(value: String): List<Uri> {
        val list = mutableListOf<Uri>()
        for (uri in value.split(",")) {
            list.add(uri.toUri())
        }
        return list
    }

    @TypeConverter
    fun convertUriList(value: List<Uri>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun revertUri(value: String): Uri? {
        return Uri.parse(value)
    }

    @TypeConverter
    fun convertUri(value: Uri): String {
        return value.toString()
    }
}