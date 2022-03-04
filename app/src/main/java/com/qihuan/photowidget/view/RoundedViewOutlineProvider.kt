package com.qihuan.photowidget.view

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider

/**
 * RoundedViewOutlineProvider
 *
 * @author qi
 * @since 2022/3/2
 */
class RoundedViewOutlineProvider(private val radius: Float) : ViewOutlineProvider() {
    override fun getOutline(view: View?, outline: Outline?) {
        if (view != null && outline != null) {
            outline.setRoundRect(0, 0, view.width, view.height, radius)
        }
    }
}