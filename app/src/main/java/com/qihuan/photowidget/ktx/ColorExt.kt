package com.qihuan.photowidget.ktx

import androidx.core.graphics.ColorUtils


/**
 * ColorExt
 * @author qi
 * @since 12/9/20
 */
fun Int.isDark(): Boolean {
    return ColorUtils.calculateLuminance(this) < 0.5
}