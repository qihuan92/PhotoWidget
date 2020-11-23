package com.qihuan.albumwidget.ktx

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue

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