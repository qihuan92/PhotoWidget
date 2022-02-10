package com.qihuan.photowidget.ktx

import android.view.HapticFeedbackConstants
import android.view.View

/**
 * HapticExt
 * @author qi
 * @since 2022/2/8
 */
fun View.performHapticFeedback() {
    performHapticFeedback(
        HapticFeedbackConstants.CONTEXT_CLICK,
        HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
    )
}

fun View.performHapticHeavyClick() {
    performHapticFeedback(
        HapticFeedbackConstants.LONG_PRESS,
        HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
    )
}