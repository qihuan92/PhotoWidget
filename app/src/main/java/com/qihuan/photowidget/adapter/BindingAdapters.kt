package com.qihuan.photowidget.adapter

import android.view.View
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.google.android.material.slider.Slider

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
    @BindingAdapter("android:isVisible")
    fun setVisible(view: View, value: Boolean) {
        view.isVisible = value
    }
}