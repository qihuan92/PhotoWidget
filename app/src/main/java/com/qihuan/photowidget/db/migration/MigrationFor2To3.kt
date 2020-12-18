package com.qihuan.photowidget.db.migration

import android.content.Context
import androidx.core.net.toUri
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.io.File

/**
 * MigrationFor2To3
 * @author qi
 * @since 12/11/20
 */
class MigrationFor2To3(
    private val context: Context
) : Migration(2, 3) {

    override fun migrate(database: SupportSQLiteDatabase) {
        // 迁移图片地址
        database.query("select * from widget_info").use { cursor ->
            while (cursor.moveToNext()) {
                val widgetId = cursor.getInt(cursor.getColumnIndex("widgetId"))
                val sourceFile = File(context.filesDir, "widget_${widgetId}.png")
                if (sourceFile.exists()) {
                    val targetDir = File(context.filesDir, "widget_${widgetId}")
                    if (!targetDir.exists()) {
                        targetDir.mkdirs()
                    }
                    val targetFile = File(targetDir, "${System.currentTimeMillis()}.png")
                    sourceFile.copyTo(targetFile, overwrite = true)
                    sourceFile.delete()

                    database.execSQL("update widget_info set uri = ? where widgetId = ?", arrayOf(targetFile.toUri().toString(), widgetId))
                }
            }
        }

        // 新增字段
        database.execSQL("ALTER TABLE widget_info ADD COLUMN autoPlayInterval INTEGER")
        // 创建图片表
        database.execSQL("CREATE TABLE IF NOT EXISTS `widget_image` (`imageId` INTEGER PRIMARY KEY AUTOINCREMENT, `widgetId` INTEGER NOT NULL, `imageUri` TEXT NOT NULL, `createTime` INTEGER NOT NULL)")
        // 迁移图片地址数据
        database.execSQL(
            """
           insert into widget_image(widgetId, imageUri, createTime)
           select widgetId, uri, strftime('%s','now') from widget_info 
        """
        )

        // 删除 info 表中 uri 字段
        database.execSQL(
            """
                CREATE TABLE IF NOT EXISTS `temp_widget_info`
                (
                `widgetId` INTEGER NOT NULL,
                `verticalPadding` REAL NOT NULL,
                `horizontalPadding` REAL NOT NULL, 
                `widgetRadius` REAL NOT NULL,
                `autoPlayInterval` INTEGER,
                PRIMARY KEY(`widgetId`)
                )
            """
        )
        database.execSQL(
            """
                insert into temp_widget_info(widgetId, verticalPadding, horizontalPadding, widgetRadius, autoPlayInterval)
                select widgetId, verticalPadding, horizontalPadding, widgetRadius, null
                from widget_info
            """
        )
        database.execSQL("drop table widget_info")
        database.execSQL("alter table temp_widget_info rename to widget_info")
    }
}