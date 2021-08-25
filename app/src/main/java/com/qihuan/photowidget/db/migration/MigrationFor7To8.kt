package com.qihuan.photowidget.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * MigrationFor7To8
 * @author qi
 * @since 2021-08-23
 */
class MigrationFor7To8 : Migration(7, 8) {

    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("update widget_info set autoPlayInterval = -1 where autoPlayInterval is null")
        database.execSQL("CREATE TABLE IF NOT EXISTS `_new_widget_info` (`widgetId` INTEGER NOT NULL, `verticalPadding` REAL NOT NULL, `horizontalPadding` REAL NOT NULL, `widgetRadius` REAL NOT NULL, `widgetTransparency` REAL NOT NULL, `autoPlayInterval` INTEGER NOT NULL, `linkInfo` TEXT, `photoScaleType` TEXT NOT NULL, `createTime` INTEGER, PRIMARY KEY(`widgetId`))")
        database.execSQL("INSERT INTO `_new_widget_info` (widgetRadius,linkInfo,widgetTransparency,photoScaleType,horizontalPadding,createTime,widgetId,autoPlayInterval,verticalPadding) SELECT widgetRadius,openUrl,widgetTransparency,photoScaleType,horizontalPadding,createTime,widgetId,autoPlayInterval,verticalPadding FROM `widget_info`")
        database.execSQL("DROP TABLE `widget_info`")
        database.execSQL("ALTER TABLE `_new_widget_info` RENAME TO `widget_info`")
    }
}