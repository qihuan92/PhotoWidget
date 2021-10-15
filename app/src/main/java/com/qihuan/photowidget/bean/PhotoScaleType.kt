package com.qihuan.photowidget.bean

import android.widget.ImageView
import com.qihuan.photowidget.view.ItemSelectionDialog

/**
 * PhotoScaleType
 * @author qi
 * @since 4/15/21
 */
enum class PhotoScaleType(
    val scaleType: ImageView.ScaleType,
    val description: String,
) : ItemSelectionDialog.Item {
    CENTER_CROP(ImageView.ScaleType.CENTER_CROP, "中心裁剪"),
    FIT_CENTER(ImageView.ScaleType.FIT_CENTER, "居中展示"),
    //FIT_XY(ImageView.ScaleType.FIT_XY, "拉伸显示"),
    ;

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