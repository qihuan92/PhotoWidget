package com.qihuan.photowidget.ktx

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel

/**
 * ViewModelExt
 * @author qi
 * @since 2022/3/23
 */
fun AndroidViewModel.getString(@StringRes resId: Int): String {
    return getApplication<Application>().getString(resId)
}