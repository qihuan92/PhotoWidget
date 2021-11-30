package com.qihuan.photowidget.bean

import com.qihuan.photowidget.view.ItemSelectionDialog

/**
 * RadiusUnit
 * @author qi
 * @since 2021/11/30
 */
enum class RadiusUnit(
    val value: String,
    val unitName: String,
    val maxValue: Float,
    val description: String,
) : ItemSelectionDialog.Item {
    ANGLE("angle", "°", 90f, "角度(°)"),
    LENGTH("length", "dp", 50f, "长度(dp)"),
    ;

    override fun getIcon(): Int? {
        return null
    }

    override fun getItemText(): String {
        return description
    }

    companion object {
        fun get(value: String): RadiusUnit {
            return values().firstOrNull { it.value == value } ?: ANGLE
        }
    }
}