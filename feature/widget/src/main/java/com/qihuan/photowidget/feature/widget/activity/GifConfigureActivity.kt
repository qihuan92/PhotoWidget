package com.qihuan.photowidget.feature.widget.activity

import com.qihuan.photowidget.core.model.WidgetType

/**
 * The configuration screen for the [com.qihuan.photowidget.GifPhotoWidgetProvider] AppWidget.
 */
class GifConfigureActivity : BaseConfigureActivity() {
    override val widgetType: WidgetType
        get() = WidgetType.GIF
}