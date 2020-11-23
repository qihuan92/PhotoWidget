package com.qihuan.albumwidget.db

import android.net.Uri
import androidx.room.TypeConverter

/**
 * Converters
 * @author qi
 * @since 2020/8/11
 */
class Converters {
    @TypeConverter
    fun revertUri(value: String): Uri? {
        return Uri.parse(value)
    }

    @TypeConverter
    fun convertUri(value: Uri): String {
        return value.toString()
    }
}