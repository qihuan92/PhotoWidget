package com.qihuan.photowidget.ktx

import android.widget.RemoteViews
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.IdRes

/**
 * RemoteViewsExt
 * @author qi
 * @since 2022/3/1
 */
fun RemoteViews.setBackgroundColor(@IdRes viewId: Int, @ColorInt color: Int) {
    setInt(viewId, "setBackgroundColor", color)
}

fun RemoteViews.setAlpha(@IdRes viewId: Int, @FloatRange(from = 0.0, to = 1.0) alpha: Float) {
    setFloat(viewId, "setAlpha", alpha)
}

fun RemoteViews.setImageAlpha(@IdRes viewId: Int, @FloatRange(from = 0.0, to = 1.0) alpha: Float) {
    setInt(viewId, "setImageAlpha", (255 * alpha).toInt())
}

fun RemoteViews.setImageTransparency(
    @IdRes viewId: Int,
    @FloatRange(from = 0.0, to = 100.0) transparency: Float
) {
    setImageAlpha(viewId, 1f - transparency / 100f)
}