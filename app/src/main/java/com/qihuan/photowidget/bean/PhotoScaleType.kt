package com.qihuan.photowidget.bean

import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.qihuan.photowidget.R
import com.qihuan.photowidget.view.ItemSelectionDialog

/**
 * PhotoScaleType
 * @author qi
 * @since 4/15/21
 */
enum class PhotoScaleType(
    val scaleType: ImageView.ScaleType,
    val description: String,
    @DrawableRes val icon: Int,
) : ItemSelectionDialog.Item {
    CENTER_CROP(ImageView.ScaleType.CENTER_CROP, "中心裁剪", R.drawable.ic_round_crop_24),
    FIT_CENTER(ImageView.ScaleType.FIT_CENTER, "居中展示", R.drawable.ic_outline_image_24),
    //FIT_XY(ImageView.ScaleType.FIT_XY, "拉伸显示"),
    ;

    override fun getIcon(): Int? {
        return icon
    }

    override fun getItemText(): String {
        return description
    }

    companion object {
        fun get(scaleType: ImageView.ScaleType): PhotoScaleType? {
            return values().find { it.scaleType == scaleType }
        }

        fun getDescription(scaleType: ImageView.ScaleType): String {
            return get(scaleType)?.description ?: ""
        }
    }
}