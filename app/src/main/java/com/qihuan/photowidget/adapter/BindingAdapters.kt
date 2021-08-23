package com.qihuan.photowidget.adapter

import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.google.android.material.card.MaterialCardView
import com.google.android.material.slider.Slider
import com.qihuan.photowidget.R
import com.qihuan.photowidget.bean.LinkType
import com.qihuan.photowidget.ktx.dp


/**
 * BindingAdapters
 * @author qi
 * @since 12/16/20
 */
object BindingAdapters {

    @JvmStatic
    @InverseBindingAdapter(attribute = "android:value", event = "sliderAttrChanged")
    fun getSliderValue(view: Slider): Float {
        return view.value
    }

    @JvmStatic
    @BindingAdapter("sliderAttrChanged")
    fun setSliderListeners(view: Slider, attrChange: InverseBindingListener) {
        view.addOnChangeListener { _, _, _ ->
            attrChange.onChange()
        }
    }

    @JvmStatic
    @BindingAdapter("android:paddingLeft")
    fun setPaddingLeft(view: View, value: Float) {
        view.updatePadding(left = value.dp)
    }

    @JvmStatic
    @BindingAdapter("android:paddingTop")
    fun setPaddingTop(view: View, value: Float) {
        view.updatePadding(top = value.dp)
    }

    @JvmStatic
    @BindingAdapter("android:paddingRight")
    fun setPaddingRight(view: View, value: Float) {
        view.updatePadding(right = value.dp)
    }

    @JvmStatic
    @BindingAdapter("android:paddingBottom")
    fun setPaddingBottom(view: View, value: Float) {
        view.updatePadding(bottom = value.dp)
    }

    @JvmStatic
    @BindingAdapter("android:alpha")
    fun setAlpha(view: View, value: Float) {
        view.alpha = value
    }

    @JvmStatic
    @BindingAdapter("cardCornerRadius")
    fun setCardCornerRadius(view: MaterialCardView, value: Float) {
        view.radius = value.dp.toFloat()
    }

    @JvmStatic
    @BindingAdapter("isVisible")
    fun setVisible(view: View, value: Boolean) {
        view.isVisible = value
    }

    @JvmStatic
    @BindingAdapter("linkTypeIcon")
    fun linkTypeIcon(view: TextView, type: LinkType?) {
        if (type == null) {
            return
        }
        val resId = when (type) {
            LinkType.OPEN_URL -> R.drawable.ic_round_link_24
            LinkType.OPEN_APP -> R.drawable.ic_round_apps_24
        }
        view.setCompoundDrawablesWithIntrinsicBounds(resId, 0, 0, 0)
    }
}