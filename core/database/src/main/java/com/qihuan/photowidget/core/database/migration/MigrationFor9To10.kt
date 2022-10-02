package com.qihuan.photowidget.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * MigrationFor9To10
 * @author qi
 * @since 2021-08-23
 */
class MigrationFor9To10 : Migration(9, 10) {

    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS `temp_widget_image` (`imageId` INTEGER PRIMARY KEY AUTOINCREMENT, `widgetId` INTEGER NOT NULL, `imageUri` TEXT NOT NULL, `createTime` INTEGER NOT NULL, `sort` INTEGER NOT NULL)")
        database.execSQL("insert into temp_widget_image(imageId, widgetId, imageUri, createTime, sort) select imageId, widgetId, imageUri, createTime, 0 from widget_image")
        database.execSQL("drop table widget_image")
        database.execSQL("alter table temp_widget_image rename to widget_image")
    }
}