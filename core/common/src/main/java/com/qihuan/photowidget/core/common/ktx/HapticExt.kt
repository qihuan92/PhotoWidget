package com.qihuan.photowidget.core.common.ktx

import android.view.HapticFeedbackConstants
import android.view.View

/**
 * HapticExt
 * @author qi
 * @since 2022/2/8
 */
fun View.performHapticFeedback() {
    performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
}

fun View.performHapticHeavyClick() {
    performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
}