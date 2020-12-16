package com.qihuan.photowidget.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * MigrationFor2To3
 * @author qi
 * @since 12/11/20
 */
class MigrationFor2To3 : Migration(2, 3) {

    override fun migrate(database: SupportSQLiteDatabase) {
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
                PRIMARY KEY(`widgetId`)
                )
            """
        )
        database.execSQL(
            """
                insert into temp_widget_info(widgetId, verticalPadding, horizontalPadding, widgetRadius, autoPlayInterval)
                select widgetId, verticalPadding, horizontalPadding, widgetRadius, autoPlayInterval
                from widget_info
            """
        )
        database.execSQL("drop table widget_info")
        database.execSQL("alter table temp_widget_info rename to widget_info")
    }
}