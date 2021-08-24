package com.qihuan.photowidget.ktx

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.core.view.*

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

fun View.paddingStatusBar() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val barInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
        view.updatePadding(top = barInsets.top)
        insets
    }
}

fun View.paddingNavigationBar() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val barInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
        view.post {
            view.updatePadding(bottom = barInsets.bottom)
        }
        insets
    }
}

fun View.marginNavigationBar() {
    val marginBottom = marginBottom
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val navigationBarInserts = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
        view.updateLayoutParams {
            (this as ViewGroup.MarginLayoutParams).setMargins(
                leftMargin,
                topMargin,
                rightMargin,
                marginBottom + navigationBarInserts.bottom
            )
        }
        insets
    }
}

fun View.marginNavigationBarAndIme() {
    val marginBottom = marginBottom
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val navigationBarInserts = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
        val imeInserts = insets.getInsets(WindowInsetsCompat.Type.ime())
        view.updateLayoutParams {
            (this as ViewGroup.MarginLayoutParams).setMargins(
                leftMargin,
                topMargin,
                rightMargin,
                marginBottom + navigationBarInserts.bottom + imeInserts.bottom
            )
        }
        insets
    }
}