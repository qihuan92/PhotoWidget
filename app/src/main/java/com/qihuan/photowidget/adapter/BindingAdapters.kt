package com.qihuan.photowidget.adapter

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.google.android.material.slider.Slider
import com.qihuan.photowidget.R
import com.qihuan.photowidget.bean.LinkType


/**
 * BindingAdapters
 * @author qi
 * @since 12/16/20
 */
object BindingAdapters {

    @JvmStatic
    @InverseBindingAdapter(attribute = "android:value", event = "app:sliderAttrChanged")
    fun getSliderValue(view: Slider): Float {
        return view.value
    }

    @JvmStatic
    @BindingAdapter("app:sliderAttrChanged")
    fun setSliderListeners(view: Slider, attrChange: InverseBindingListener) {
        view.addOnChangeListener { _, _, _ ->
            attrChange.onChange()
        }
    }

    @JvmStatic
    @BindingAdapter("app:isVisible")
    fun setVisible(view: View, value: Boolean) {
        view.isVisible = value
    }

    @JvmStatic
    @BindingAdapter("android:src")
    fun setImageDrawable(view: ImageView, drawable: Drawable?) {
        view.setImageDrawable(drawable)
    }

    @JvmStatic
    @BindingAdapter("android:src")
    fun setImageResource(view: ImageView, resource: Int) {
        view.setImageResource(resource)
    }

    @JvmStatic
    @BindingAdapter("app:linkTypeIcon")
    fun linkTypeIcon(view: TextView, type: LinkType?) {
        if (type == null) {
            return
        }
        val resId = when (type) {
            LinkType.URL -> R.drawable.ic_round_link_24
            LinkType.OPEN_APP -> R.drawable.ic_round_apps_24
        }
        view.setCompoundDrawablesWithIntrinsicBounds(resId, 0, 0, 0)
    }
}