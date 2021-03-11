package com.qihuan.photowidget.ktx

import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableFloat
import androidx.viewbinding.ViewBinding

/**
 * BindingExt
 * @author qi
 * @since 2020/7/21
 */
inline fun <T : ViewBinding> AppCompatActivity.viewBinding(crossinline bindingInflater: (LayoutInflater) -> T) =
    lazy(LazyThreadSafetyMode.NONE) {
        bindingInflater.invoke(layoutInflater)
    }

fun ObservableFloat.observe(callback: (Float) -> Unit) {
    addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            callback.invoke(get())
        }
    })
}

fun ObservableBoolean.observe(callback: (Boolean) -> Unit) {
    addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            callback.invoke(get())
        }
    })
}