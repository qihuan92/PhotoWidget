package com.qihuan.photowidget.ktx

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.util.TypedValue
import android.view.View
import android.view.Window
import androidx.core.view.WindowCompat

/**
 * UIExt
 * @author qi
 * @since 2020/7/17
 */
val Float.dp
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        Resources.getSystem().displayMetrics
    ).toInt()

fun Int.toDp(context: Context): Float {
    return this / context.resources.displayMetrics.density
}

fun adapterBarsColor(resources: Resources, window: Window, view: View) {
    WindowCompat.getInsetsController(window, view)?.apply {
        when (resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_NO -> {
                isAppearanceLightStatusBars = true
                isAppearanceLightNavigationBars = true
            }
            Configuration.UI_MODE_NIGHT_YES -> {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }
}