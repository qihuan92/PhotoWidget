package com.qihuan.photowidget.feature.widget.domain.model

import androidx.annotation.StringRes

/**
 * @Author : qi
 * @Date : 2024/1/18 18:17
 * @Description :
 **/
sealed class SaveWidgetResult {
    object Success : SaveWidgetResult()
    data class Fail(@StringRes val message: Int) : SaveWidgetResult()
}