package com.qihuan.photowidget.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * MigrationFor10To11
 * @author qi
 * @since 2021-10-20
 */
class MigrationFor10To11 : Migration(10, 11) {

    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS `_new_widget_info` (`widgetId` INTEGER NOT NULL, `topPadding` REAL NOT NULL, `bottomPadding` REAL NOT NULL, `leftPadding` REAL NOT NULL, `rightPadding` REAL NOT NULL, `widgetRadius` REAL NOT NULL, `widgetTransparency` REAL NOT NULL, `autoPlayInterval` INTEGER NOT NULL, `linkInfo` TEXT, `photoScaleType` TEXT NOT NULL, `createTime` INTEGER, PRIMARY KEY(`widgetId`))")
        database.execSQL("INSERT INTO `_new_widget_info` (widgetId,  topPadding, bottomPadding, leftPadding, rightPadding, widgetRadius, widgetTransparency, autoPlayInterval, linkInfo, photoScaleType, createTime) select widgetId,  verticalPadding, verticalPadding, horizontalPadding, horizontalPadding, widgetRadius, widgetTransparency, autoPlayInterval, linkInfo, photoScaleType, createTime from `widget_info`")
        database.execSQL("DROP TABLE `widget_info`")
        database.execSQL("ALTER TABLE `_new_widget_info` RENAME TO `widget_info`")
    }
}