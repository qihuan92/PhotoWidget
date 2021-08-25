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
    val interval: Int = -1,
    val description: String,
) {
    NONE(-1, "无（可以点击左右边缘进行切换）"),
    THREE_SECONDS(3000, "3秒"),
    FIVE_SECONDS(5000, "5秒"),
    TEN_SECONDS(10000, "10秒"),
    THIRTY_SECONDS(30000, "30秒"),
    ;

    companion object {
        fun get(interval: Int): PlayInterval {
            if (interval < 0) {
                return NONE
            }
            for (value in values()) {
                if (value.interval == interval) {
                    return value
                }
            }
            return NONE
        }
    }
}