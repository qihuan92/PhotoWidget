package com.qihuan.photowidget.bean

/**
 * PlayInterval
 * @author qi
 * @since 2021/8/19
 */
enum class PlayInterval(
    /**
     * 毫秒值
     */
    val interval: Int? = null,
    val description: String,
) {
    NONE(null, "无"),
    THREE_SECONDS(3000, "3秒"),
    FIVE_SECONDS(5000, "5秒"),
    TEN_SECONDS(10000, "10秒"),
    THIRTY_SECONDS(30000, "30秒"),
    ;
}