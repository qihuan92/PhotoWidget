package com.qihuan.photowidget.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * MigrationFor5To6
 * @author qi
 * @since 2021-04-07
 */
class MigrationFor5To6 : Migration(5, 6) {

    override fun migrate(database: SupportSQLiteDatabase) {
        // 创建新表
        database.execSQL(
            """
                CREATE TABLE IF NOT EXISTS `temp_widget_info` 
                (
                `widgetId` INTEGER NOT NULL, 
                `verticalPadding` REAL NOT NULL, 
                `horizontalPadding` REAL NOT NULL, 
                `widgetRadius` REAL NOT NULL, 
                `widgetTransparency` REAL NOT NULL, 
                `autoPlayInterval` INTEGER, 
                `openUrl` TEXT, 
                `createTime` INTEGER, 
                PRIMARY KEY(`widgetId`)
                )
            """
        )

        // 记录迁移
        database.execSQL(
            """
                   insert into temp_widget_info(widgetId, verticalPadding, horizontalPadding, widgetRadius, widgetTransparency, autoPlayInterval, openUrl, createTime)
                   select widgetId, verticalPadding, horizontalPadding, widgetRadius, 0, autoPlayInterval, openUrl, createTime
                   from widget_info
                """
        )

        // 删除旧表
        database.execSQL("drop table widget_info")

        // 修改表名称
        database.execSQL("alter table temp_widget_info rename to widget_info")
    }
}