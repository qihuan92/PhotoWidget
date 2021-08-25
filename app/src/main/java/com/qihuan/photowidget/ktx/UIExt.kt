package com.qihuan.photowidget.ktx

import android.animation.Animator
import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import androidx.core.view.*
import kotlin.math.hypot

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

fun View.circularRevealAnimator(): Animator {
    val cx = width / 2
    val cy = height / 2
    val finalRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()
    return ViewAnimationUtils.createCircularReveal(this, cx, cy, 0f, finalRadius)
}

val Resources.androidShortAnimTime
    get() = getInteger(android.R.integer.config_shortAnimTime).toLong()

val Resources.androidMediumAnimTime
    get() = getInteger(android.R.integer.config_mediumAnimTime).toLong()

val Resources.androidLongAnimTime
    get() = getInteger(android.R.integer.config_longAnimTime).toLong()
