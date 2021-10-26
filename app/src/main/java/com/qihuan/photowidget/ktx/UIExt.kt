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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.qihuan.photowidget.R
import com.qihuan.photowidget.databinding.DialogLoadingBinding

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

fun SwipeRefreshLayout.setDefaultColors() {
    setColorSchemeResources(
        R.color.purple_200,
        R.color.purple_500,
        R.color.purple_700,
    )
}

val Resources.androidShortAnimTime
    get() = getInteger(android.R.integer.config_shortAnimTime).toLong()

val Resources.androidMediumAnimTime
    get() = getInteger(android.R.integer.config_mediumAnimTime).toLong()

val Resources.androidLongAnimTime
    get() = getInteger(android.R.integer.config_longAnimTime).toLong()

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
