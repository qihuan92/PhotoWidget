package com.qihuan.photowidget

import android.content.Context
import com.qihuan.photowidget.core.common.AppInfo

/**
 * 应用信息
 *
 * @author Qi
 * @since 2022/10/3
 */
class AppInfoImpl(private val context: Context) : AppInfo {
    override val versionName: String
        get() = BuildConfig.VERSION_NAME

    override val versionCode: Int
        get() = BuildConfig.VERSION_CODE

    override val appName: String
        get() = context.getString(R.string.app_name)
}