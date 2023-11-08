package com.qihuan.photowidget.core.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

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
    @StringRes override val text: Int,
    @StringRes val simpleDescription: Int,
    @DrawableRes override val icon: Int?
) : SelectionItem {
    NONE(
        -1,
        R.string.play_interval_none,
        R.string.play_interval_none_simple,
        R.drawable.ic_round_timer_off_24
    ),
    THREE_SECONDS(
        3000,
        R.string.play_interval_three_seconds,
        R.string.play_interval_three_seconds,
        R.drawable.ic_round_timer_24
    ),
    FIVE_SECONDS(
        5000,
        R.string.play_interval_five_seconds,
        R.string.play_interval_five_seconds,
        R.drawable.ic_round_timer_24
    ),
    TEN_SECONDS(
        10000,
        R.string.play_interval_ten_seconds,
        R.string.play_interval_ten_seconds,
        R.drawable.ic_round_timer_24
    ),
    THIRTY_SECONDS(
        30000,
        R.string.play_interval_thirty_seconds,
        R.string.play_interval_thirty_seconds,
        R.drawable.ic_round_timer_24
    ),
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