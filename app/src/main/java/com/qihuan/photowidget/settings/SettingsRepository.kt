package com.qihuan.photowidget.settings

import android.app.Application
import android.content.Context
import com.qihuan.photowidget.common.INVALID_AUTO_REFRESH_INTERVAL
import com.qihuan.photowidget.common.KEY_AUTO_REFRESH_INTERVAL
import com.qihuan.photowidget.ktx.calculateFormatSizeRecursively
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * SettingsRepository
 * @author qi
 * @since 2021/11/8
 */
class SettingsRepository(private val application: Application) {

    private val sp by lazy { application.getSharedPreferences("settings", Context.MODE_PRIVATE) }

    suspend fun clearCache() {
        withContext(Dispatchers.IO) {
            application.cacheDir.deleteRecursively()
        }
    }

    suspend fun getCacheSize(): String {
        return withContext(Dispatchers.IO) {
            application.cacheDir.calculateFormatSizeRecursively()
        }
    }

    fun saveAutoRefreshInterval(interval: Long) {
        sp.edit()
            .putLong(KEY_AUTO_REFRESH_INTERVAL, interval)
            .apply()
    }

    fun clearAutoRefreshInterval() {
        sp.edit()
            .remove(KEY_AUTO_REFRESH_INTERVAL)
            .apply()
    }

    suspend fun getAutoRefreshInterval(): Long {
        return withContext(Dispatchers.IO) {
            sp.getLong(KEY_AUTO_REFRESH_INTERVAL, INVALID_AUTO_REFRESH_INTERVAL)
        }
    }
}