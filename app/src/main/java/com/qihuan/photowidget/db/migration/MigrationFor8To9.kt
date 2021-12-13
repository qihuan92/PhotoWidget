package com.qihuan.photowidget.db.migration

import android.database.sqlite.SQLiteDatabase
import androidx.core.content.contentValuesOf
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.qihuan.photowidget.bean.LinkInfo
import com.qihuan.photowidget.common.LinkType

/**
 * MigrationFor8To9
 * @author qi
 * @since 2021-09-27
 */
class MigrationFor8To9 : Migration(8, 9) {

    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS `link_info` (`widgetId` INTEGER NOT NULL, `type` TEXT NOT NULL, `title` TEXT NOT NULL, `description` TEXT NOT NULL, `link` TEXT NOT NULL, PRIMARY KEY(`widgetId`))")
        val cursor = database.query("select widgetId, linkInfo from widget_info")
        cursor.use {
            while (it != null && it.moveToNext()) {
                val widgetId = it.getInt(cursor.getColumnIndexOrThrow("widgetId"))
                val linkInfoStr = it.getString(cursor.getColumnIndexOrThrow("linkInfo"))
                val linkInfo = convertLinkInfo(widgetId, linkInfoStr)
                linkInfo?.apply {
                    val values = contentValuesOf(
                        "widgetId" to widgetId,
                        "type" to type.value,
                        "title" to title,
                        "description" to description,
                        "link" to link,
                    )
                    database.insert("link_info", SQLiteDatabase.CONFLICT_REPLACE, values)
                }
            }
        }
    }

    private fun convertLinkInfo(widgetId: Int, linkInfoStr: String?): LinkInfo? {
        if (linkInfoStr.isNullOrEmpty()) {
            return null
        }
        val type: LinkType
        val title: String
        val description: String
        val link: String
        if (linkInfoStr.startsWith("${LinkType.OPEN_APP.value}/")) {
            val info = linkInfoStr.split("/")
            type = LinkType.OPEN_APP
            title = "打开应用: [ ${info[1]} ]"
            description = "包名: ${info[2]}"
            link = info[2]
        } else {
            type = LinkType.OPEN_URL
            title = "打开链接"
            description = "地址: $linkInfoStr"
            link = linkInfoStr
        }
        return LinkInfo(widgetId, type, title, description, link)
    }
}