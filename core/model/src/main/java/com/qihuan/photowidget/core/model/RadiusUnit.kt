package com.qihuan.photowidget.core.model

/**
 * RadiusUnit
 * @author qi
 * @since 2021/11/30
 */
enum class RadiusUnit(
    val value: String,
    val unitName: String,
    val maxValue: Float,
    override val text: Int,
    override val icon: Int? = null
) : SelectionItem {
    ANGLE("angle", "°", 90f, R.string.radius_unit_angle),
    LENGTH("length", "dp", 50f, R.string.radius_unit_length),
    ;

    companion object {
        fun get(value: String): RadiusUnit {
            return values().firstOrNull { it.value == value } ?: LENGTH
        }
    }
}