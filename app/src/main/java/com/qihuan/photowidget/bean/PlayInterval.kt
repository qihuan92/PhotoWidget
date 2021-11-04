package com.qihuan.photowidget.bean

import androidx.annotation.DrawableRes
import com.qihuan.photowidget.R
import com.qihuan.photowidget.view.ItemSelectionDialog

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
    val simpleDescription: String,
    @DrawableRes val icon: Int,
) : ItemSelectionDialog.Item {
    NONE(-1, "关闭（可以点击左右边缘进行切换）", "关闭", R.drawable.ic_round_timer_off_24),
    THREE_SECONDS(3000, "3秒", "3秒", R.drawable.ic_round_timer_24),
    FIVE_SECONDS(5000, "5秒", "5秒", R.drawable.ic_round_timer_24),
    TEN_SECONDS(10000, "10秒", "10秒", R.drawable.ic_round_timer_24),
    THIRTY_SECONDS(30000, "30秒", "30秒", R.drawable.ic_round_timer_24),
    ;

    override fun getIcon(): Int? {
        return icon
    }

    override fun getItemText(): String {
        return description
    }

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