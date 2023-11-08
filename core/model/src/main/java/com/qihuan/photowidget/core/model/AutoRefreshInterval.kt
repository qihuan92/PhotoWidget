package com.qihuan.photowidget.core.model

import androidx.annotation.StringRes;

/**
 * AutoRefreshInterval
 * @author qi
 * @since 2021/11/8
 */
enum class AutoRefreshInterval(
    val value: Long,
    @StringRes override val text: Int,
    override val icon: Int? = null
) : SelectionItem {
    NONE(INVALID_AUTO_REFRESH_INTERVAL, R.string.auto_refresh_interval_none),
    DAY(86400000L, R.string.auto_refresh_interval_day),
    TWELVE_HOURS(43200000L, R.string.auto_refresh_interval_twelve_hours),
    HOUR(3600000L, R.string.auto_refresh_interval_hour),
    FIFTEEN_MINUTES(900000L, R.string.auto_refresh_interval_fifteen_minutes),
    ;

    companion object {
        fun get(value: Long): AutoRefreshInterval {
            return values().first { it.value == value }
        }
    }
}