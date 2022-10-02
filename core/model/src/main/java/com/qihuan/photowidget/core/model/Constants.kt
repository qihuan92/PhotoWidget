@file:Suppress("unused")

package com.qihuan.photowidget.core.model

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
    const val APPWIDGET_DELETED = "com.qihuan.photowidget.APPWIDGET_DELETED"
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