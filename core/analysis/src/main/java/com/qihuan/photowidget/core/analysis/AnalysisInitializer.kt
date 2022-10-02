package com.qihuan.photowidget.core.analysis

import android.app.Application
import android.content.Context
import androidx.startup.Initializer

/**
 * Analysis initializer.
 *
 * @author Qi
 * @since 2022/10/2
 */
class AnalysisInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        EventStatistics.init(context.applicationContext as Application)
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return mutableListOf()
    }

}