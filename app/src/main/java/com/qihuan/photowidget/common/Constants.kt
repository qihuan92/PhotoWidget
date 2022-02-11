package com.qihuan.photowidget.common

import android.graphics.Bitmap
import android.os.Build
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.qihuan.photowidget.R
import com.qihuan.photowidget.view.ItemSelectionDialog

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
const val KEY_DEFAULT_WIDGET_RADIUS = "defaultWidgetRadius"
const val KEY_DEFAULT_WIDGET_RADIUS_UNIT = "defaultWidgetRadiusUnit"
const val KEY_DEFAULT_WIDGET_SCALE_TYPE = "defaultWidgetScaleType"

object WorkTags {
    const val PERIODIC_REFRESH_WIDGET = "periodic_refresh_widget"
    const val ONE_TIME_REFRESH_WIDGET = "one_time_refresh_widget"
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
    val description: String,
    @DrawableRes val icon: Int,
) : ItemSelectionDialog.Item {
    OPEN_APP("openApp", "打开应用", R.drawable.ic_round_apps_24),
    OPEN_URL("openUrl", "打开URL", R.drawable.ic_round_link_24),
    OPEN_ALBUM("openAlbum", "打开相册", R.drawable.ic_round_photo_album_24),
    OPEN_FILE("openFile", "打开文件", R.drawable.ic_round_insert_drive_file_24),
    //OPEN_FOLDER("openFolder", "打开文件夹", R.drawable.ic_round_folder_24),
    //LOCK_SCREEN("lockScreen", "锁定屏幕", R.drawable.ic_round_screen_lock_portrait_24),
    ;

    override fun getIcon(): Int? {
        return icon
    }

    override fun getItemText(): String {
        return description
    }

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
    val description: String,
    @DrawableRes val icon: Int,
) : ItemSelectionDialog.Item {
    CENTER_CROP(ImageView.ScaleType.CENTER_CROP, "中心裁剪", R.drawable.ic_round_crop_24),
    FIT_CENTER(ImageView.ScaleType.FIT_CENTER, "居中展示", R.drawable.ic_outline_image_24),
    //FIT_XY(ImageView.ScaleType.FIT_XY, "拉伸显示"),
    ;

    override fun getIcon(): Int? {
        return icon
    }

    override fun getItemText(): String {
        return description
    }

    companion object {
        fun get(scaleType: ImageView.ScaleType): PhotoScaleType? {
            return values().find { it.scaleType == scaleType }
        }

        fun getDescription(scaleType: ImageView.ScaleType): String {
            return get(scaleType)?.description ?: ""
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
    val description: String,
    val simpleDescription: String,
    @DrawableRes val icon: Int,
) : ItemSelectionDialog.Item {
    NONE(-1, "关闭（可以点击左右边缘进行切换）", "关闭", R.drawable.ic_round_timer_off_24),
    THREE_SECONDS(3000, "3秒", "3秒", R.drawable.ic_round_timer_24),
    FIVE_SECONDS(5000, "5秒", "5秒", R.drawable.ic_round_timer_24),
    TEN_SECONDS(10000, "10秒", "10秒", R.drawable.ic_round_timer_24),
    THIRTY_SECONDS(30000, "30秒", "30秒", R.drawable.ic_round_timer_24),
    ;

    override fun getIcon(): Int? {
        return icon
    }

    override fun getItemText(): String {
        return description
    }

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
    val description: String,
) : ItemSelectionDialog.Item {
    ANGLE("angle", "°", 90f, "角度(°)"),
    LENGTH("length", "dp", 50f, "长度(dp)"),
    ;

    override fun getIcon(): Int? {
        return null
    }

    override fun getItemText(): String {
        return description
    }

    companion object {
        fun get(value: String): RadiusUnit {
            return values().firstOrNull { it.value == value } ?: ANGLE
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
    val description: String,
) : ItemSelectionDialog.Item {
    NONE(INVALID_AUTO_REFRESH_INTERVAL, "关闭"),
    DAY(86400000L, "每天"),
    TWELVE_HOURS(43200000L, "每12小时"),
    HOUR(3600000L, "每小时"),
    FIFTEEN_MINUTES(900000L, "每15分钟"),
    ;

    override fun getIcon(): Int? {
        return null
    }

    override fun getItemText(): String {
        return description
    }

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
    THEME_COLOR, COLOR, IMAGE
}