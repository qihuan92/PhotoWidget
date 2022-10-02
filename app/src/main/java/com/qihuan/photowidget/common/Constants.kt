@file:Suppress("unused")

package com.qihuan.photowidget.common

import android.graphics.Bitmap
import android.os.Build
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.qihuan.photowidget.BuildConfig
import com.qihuan.photowidget.R
import com.qihuan.photowidget.bean.SelectionItem

/**
 * Constants
 * @author qi
 * @since 2021/8/20
 */
const val TEMP_DIR_NAME = "temp"
const val FRAME_DIR_NAME = "frame"
const val MAIN_PAGE_SPAN_COUNT = 2
const val DEFAULT_COMPRESSION_QUALITY = 75
const val INVALID_AUTO_REFRESH_INTERVAL = -1L
const val KEY_AUTO_REFRESH_INTERVAL = "autoRefreshInterval"
const val KEY_DEFAULT_WIDGET_RADIUS = "defaultWidgetRadius"
const val KEY_DEFAULT_WIDGET_RADIUS_UNIT = "defaultWidgetRadiusUnit"
const val KEY_DEFAULT_WIDGET_SCALE_TYPE = "defaultWidgetScaleType"

object BroadcastAction {
    const val APPWIDGET_DELETED = "${BuildConfig.APPLICATION_ID}.APPWIDGET_DELETED"
}

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

/**
 * LinkType
 * @author qi
 * @since 3/22/21
 */
enum class LinkType(
    val value: String,
    @StringRes override val text: Int,
    @DrawableRes override val icon: Int?,
) : SelectionItem {
    OPEN_APP("openApp", R.string.link_type_open_app, R.drawable.ic_round_apps_24),
    OPEN_URL("openUrl", R.string.link_type_open_url, R.drawable.ic_round_link_24),
    OPEN_ALBUM("openAlbum", R.string.link_type_open_album, R.drawable.ic_round_photo_album_24),
    OPEN_FILE("openFile", R.string.link_type_open_file, R.drawable.ic_round_insert_drive_file_24),
    ;

    companion object {
        fun get(value: String?): LinkType? {
            return values().firstOrNull { it.value == value }
        }
    }
}

/**
 * PhotoScaleType
 * @author qi
 * @since 4/15/21
 */
enum class PhotoScaleType(
    val scaleType: ImageView.ScaleType,
    @StringRes override val text: Int,
    @DrawableRes override val icon: Int,
) : SelectionItem {
    CENTER_CROP(
        ImageView.ScaleType.CENTER_CROP,
        R.string.scale_type_center_crop,
        R.drawable.ic_round_crop_24
    ),
    FIT_CENTER(
        ImageView.ScaleType.FIT_CENTER,
        R.string.scale_type_fit_center,
        R.drawable.ic_outline_image_24
    ),
    ;

    companion object {
        fun get(scaleType: ImageView.ScaleType): PhotoScaleType? {
            return values().find { it.scaleType == scaleType }
        }
    }
}

/**
 * PlayInterval
 * @author qi
 * @since 2021/8/19
 */
enum class PlayInterval(
    /**
     * 毫秒值
     */
    val interval: Int = -1,
    @StringRes override val text: Int,
    @StringRes val simpleDescription: Int,
    @DrawableRes override val icon: Int?
) : SelectionItem {
    NONE(
        -1,
        R.string.play_interval_none,
        R.string.play_interval_none_simple,
        R.drawable.ic_round_timer_off_24
    ),
    THREE_SECONDS(
        3000,
        R.string.play_interval_three_seconds,
        R.string.play_interval_three_seconds,
        R.drawable.ic_round_timer_24
    ),
    FIVE_SECONDS(
        5000,
        R.string.play_interval_five_seconds,
        R.string.play_interval_five_seconds,
        R.drawable.ic_round_timer_24
    ),
    TEN_SECONDS(
        10000,
        R.string.play_interval_ten_seconds,
        R.string.play_interval_ten_seconds,
        R.drawable.ic_round_timer_24
    ),
    THIRTY_SECONDS(
        30000,
        R.string.play_interval_thirty_seconds,
        R.string.play_interval_thirty_seconds,
        R.drawable.ic_round_timer_24
    ),
    ;

    companion object {
        fun get(interval: Int): PlayInterval {
            if (interval < 0) {
                return NONE
            }
            for (value in values()) {
                if (value.interval == interval) {
                    return value
                }
            }
            return NONE
        }
    }
}

/**
 * RadiusUnit
 * @author qi
 * @since 2021/11/30
 */
enum class RadiusUnit(
    val value: String,
    val unitName: String,
    val maxValue: Float,
    override val text: Int,
    override val icon: Int? = null
) : SelectionItem {
    ANGLE("angle", "°", 90f, R.string.radius_unit_angle),
    LENGTH("length", "dp", 50f, R.string.radius_unit_length),
    ;

    companion object {
        fun get(value: String): RadiusUnit {
            return values().firstOrNull { it.value == value } ?: LENGTH
        }
    }
}

/**
 * TipsType
 * @author qi
 * @since 2021/10/11
 */
enum class TipsType(
    val code: Int
) {
    IGNORE_BATTERY_OPTIMIZATIONS(1),
    ADD_WIDGET(2),
}

/**
 * WidgetType
 * @author qi
 * @since 2021/10/27
 */
enum class WidgetType(
    val code: String
) {
    NORMAL("normal"),
    GIF("gif"),
    ;

    companion object {
        fun get(code: String): WidgetType {
            for (value in values()) {
                if (code == value.code) {
                    return value
                }
            }
            return NORMAL
        }
    }
}

/**
 * AutoRefreshInterval
 * @author qi
 * @since 2021/11/8
 */
enum class AutoRefreshInterval(
    val value: Long,
    @StringRes override val text: Int,
    override val icon: Int? = null
) : SelectionItem {
    NONE(INVALID_AUTO_REFRESH_INTERVAL, R.string.auto_refresh_interval_none),
    DAY(86400000L, R.string.auto_refresh_interval_day),
    TWELVE_HOURS(43200000L, R.string.auto_refresh_interval_twelve_hours),
    HOUR(3600000L, R.string.auto_refresh_interval_hour),
    FIFTEEN_MINUTES(900000L, R.string.auto_refresh_interval_fifteen_minutes),
    ;

    companion object {
        fun get(value: Long): AutoRefreshInterval {
            return values().first { it.value == value }
        }
    }
}

/**
 * 相框类型
 *
 * @author qi
 * @since 2022/02/11
 */
enum class WidgetFrameType {
    NONE, THEME_COLOR, COLOR, IMAGE, BUILD_IN
}