package com.qihuan.photowidget.adapter

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

    @InverseBindingAdapter(attribute = "android:value", event = "app:sliderAttrChanged")
    @JvmStatic
    fun getSliderValue(view: Slider): Float {
        return view.value
    }

    @BindingAdapter("app:sliderAttrChanged")
    @JvmStatic
    fun setSliderListeners(view: Slider, attrChange: InverseBindingListener) {
        view.addOnChangeListener { _, _, _ ->
            attrChange.onChange()
        }
    }
}