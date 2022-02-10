package com.qihuan.photowidget.ktx

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.view.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.qihuan.photowidget.R
import com.qihuan.photowidget.common.RadiusUnit
import com.qihuan.photowidget.databinding.DialogLoadingBinding
import kotlin.math.min
import kotlin.math.tan

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

fun Context.createLoadingDialog(@StringRes message: Int = R.string.loading): AlertDialog {
    return createLoadingDialog(getString(message))
}

fun Context.createLoadingDialog(message: String = getString(R.string.loading)): AlertDialog {
    val binding = DialogLoadingBinding.inflate(LayoutInflater.from(this))
    binding.tvMessage.text = message
    return MaterialAlertDialogBuilder(this)
        .setCancelable(false)
        .setView(binding.root)
        .create()
}

fun calculateRadiusPx(
    width: Int,
    height: Int,
    value: Float,
    unit: RadiusUnit = RadiusUnit.ANGLE
): Int {
    when (unit) {
        RadiusUnit.ANGLE -> {
            if (value == 0f) {
                return 0
            }
            val maxRadius = min(width, height) / 2
            if (value == 90f) {
                return maxRadius
            }
            val degree = (90 - value) / 2
            val radians = Math.toRadians(degree.toDouble())
            return maxRadius - (tan(radians) * maxRadius).toInt()
        }
        RadiusUnit.LENGTH -> {
            return value.dp
        }
    }
}
