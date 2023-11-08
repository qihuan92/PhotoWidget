package com.qihuan.photowidget.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * MigrationFor4To5
 * @author qi
 * @since 12/11/20
 */
class MigrationFor4To5 : Migration(4, 5) {

    override fun migrate(database: SupportSQLiteDatabase) {
        // 创建新表
        database.execSQL(
            """
                CREATE TABLE IF NOT EXISTS `temp_widget_info` (
                `widgetId` INTEGER NOT NULL, 
                `verticalPadding` REAL NOT NULL, 
                `horizontalPadding` REAL NOT NULL, 
                `widgetRadius` REAL NOT NULL, 
                `autoPlayInterval` INTEGER, 
                `openUrl` TEXT, 
                `createTime` INTEGER, 
                PRIMARY KEY(`widgetId`))
            """
        )

        // 记录迁移
        database.execSQL(
            """
                   insert into temp_widget_info(widgetId, verticalPadding, horizontalPadding, widgetRadius, autoPlayInterval, openUrl)
                   select widgetId, verticalPadding, horizontalPadding, widgetRadius, autoPlayInterval, openUrl
                   from widget_info
                """
        )

        // 删除旧表
        database.execSQL("drop table widget_info")

        // 修改表名称
        database.execSQL("alter table temp_widget_info rename to widget_info")
    }
}