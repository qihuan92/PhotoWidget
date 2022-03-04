package com.qihuan.photowidget.config

import com.qihuan.photowidget.common.WidgetType

/**
 * The configuration screen for the [com.qihuan.photowidget.GifPhotoWidgetProvider] AppWidget.
 */
class GifConfigureActivity : BaseConfigureActivity() {
    override val widgetType: WidgetType
        get() = WidgetType.GIF
}