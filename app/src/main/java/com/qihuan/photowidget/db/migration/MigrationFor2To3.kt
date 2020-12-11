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
        database.execSQL("ALTER TABLE widget_info ADD COLUMN autoPlay INTEGER NOT NULL default '0'")
        database.execSQL("ALTER TABLE widget_info ADD COLUMN autoPlayInterval INTEGER")
    }
}