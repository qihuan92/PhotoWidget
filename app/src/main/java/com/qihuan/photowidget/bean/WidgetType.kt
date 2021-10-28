package com.qihuan.photowidget.bean

/**
 * WidgetType
 * @author qi
 * @since 2021/10/27
 */
enum class WidgetType(
    val code: String
) {
    NORMAL("normal"),
    GIF("gif"),
    ;

    companion object {
        fun get(code: String): WidgetType {
            for (value in values()) {
                if (code == value.code) {
                    return value
                }
            }
            return NORMAL
        }
    }
}