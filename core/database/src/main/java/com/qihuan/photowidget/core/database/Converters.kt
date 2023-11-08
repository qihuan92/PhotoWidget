package com.qihuan.photowidget.core.database

import android.net.Uri
import androidx.room.TypeConverter
import com.qihuan.photowidget.core.model.*

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
    fun convertUri(value: Uri?): String {
        return value.toString()
    }

    @TypeConverter
    fun revertScaleType(value: String): PhotoScaleType {
        return enumValueOf(value)
    }

    @TypeConverter
    fun convertScaleType(value: PhotoScaleType): String {
        return value.name
    }

    @TypeConverter
    fun convertPlayInterval(value: PlayInterval): Int {
        return value.interval
    }

    @TypeConverter
    fun revertPlayInterval(value: Int): PlayInterval {
        return PlayInterval.get(value)
    }

    @TypeConverter
    fun convertLinkType(value: LinkType?): String? {
        return value?.value
    }

    @TypeConverter
    fun revertLinkType(value: String?): LinkType? {
        return LinkType.get(value)
    }

    @TypeConverter
    fun convertWidgetType(value: WidgetType): String {
        return value.code
    }

    @TypeConverter
    fun revertWidgetType(value: String): WidgetType {
        return WidgetType.get(value)
    }

    @TypeConverter
    fun convertRadiusUnit(value: RadiusUnit): String {
        return value.value
    }

    @TypeConverter
    fun revertRadiusUnit(value: String): RadiusUnit {
        return RadiusUnit.get(value)
    }
}