package com.qihuan.photowidget.core.model

import android.widget.ImageView

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/**
 * PhotoScaleType
 * @author qi
 * @since 4/15/21
 */
enum class PhotoScaleType(
    val scaleType: ImageView.ScaleType,
    @StringRes override val text: Int,
    @DrawableRes override val icon: Int,
) : SelectionItem {
    CENTER_CROP(
        ImageView.ScaleType.CENTER_CROP,
        R.string.scale_type_center_crop,
        R.drawable.ic_round_crop_24
    ),
    FIT_CENTER(
        ImageView.ScaleType.FIT_CENTER,
        R.string.scale_type_fit_center,
        R.drawable.ic_outline_image_24
    ),
    ;

    companion object {
        fun get(scaleType: ImageView.ScaleType): PhotoScaleType? {
            return values().find { it.scaleType == scaleType }
        }
    }
}