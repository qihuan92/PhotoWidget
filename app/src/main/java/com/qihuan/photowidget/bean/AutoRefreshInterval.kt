package com.qihuan.photowidget.bean

import com.qihuan.photowidget.common.INVALID_AUTO_REFRESH_INTERVAL
import com.qihuan.photowidget.view.ItemSelectionDialog

/**
 * AutoRefreshInterval
 * @author qi
 * @since 2021/11/8
 */
enum class AutoRefreshInterval(
    val value: Long,
    val description: String,
) : ItemSelectionDialog.Item {
    NONE(INVALID_AUTO_REFRESH_INTERVAL, "关闭"),
    DAY(86400000L, "每天"),
    TWELVE_HOURS(43200000L, "每12小时"),
    HOUR(3600000L, "每小时"),
    FIFTEEN_MINUTES(900000L, "每15分钟"),
    ;

    override fun getIcon(): Int? {
        return null
    }

    override fun getItemText(): String {
        return description
    }

    companion object {
        fun get(value: Long): AutoRefreshInterval {
            return values().first { it.value == value }
        }
    }
}