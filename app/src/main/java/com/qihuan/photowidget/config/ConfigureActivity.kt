package com.qihuan.photowidget.config

import com.qihuan.photowidget.core.model.WidgetType

/**
 * The configuration screen for the [com.qihuan.photowidget.PhotoWidgetProvider] AppWidget.
 */
class ConfigureActivity : BaseConfigureActivity() {
    override val widgetType: WidgetType
        get() = WidgetType.NORMAL
}