package com.qihuan.photowidget.db.migration

import android.content.Context
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * MigrationFor1To2
 * @author qi
 * @since 12/4/20
 */
class MigrationFor1To2(
    private val context: Context
) : Migration(1, 2) {

    private val density by lazy { context.resources.displayMetrics.density }

    override fun migrate(database: SupportSQLiteDatabase) {
        // 创建新表
        database.execSQL(
            """
                CREATE TABLE IF NOT EXISTS `temp_widget_info`
                (
                `widgetId` INTEGER NOT NULL, 
                `uri` TEXT NOT NULL, 
                `verticalPadding` REAL NOT NULL, 
                `horizontalPadding` REAL NOT NULL, 
                `widgetRadius` REAL NOT NULL, 
                PRIMARY KEY(`widgetId`)
                )
                """
        )

        // 记录迁移
        database.execSQL(
            """
                   insert into temp_widget_info(widgetId, uri, verticalPadding, horizontalPadding, widgetRadius)
                   select widgetId, uri, cast(verticalPadding / ? as Int), cast(horizontalPadding / ? as Int), cast(widgetRadius / ? as Int)
                   from widget_info
                """, arrayOf(density, density, density)
        )

        // 删除旧表
        database.execSQL("drop table widget_info")

        // 修改表名称
        database.execSQL("alter table temp_widget_info rename to widget_info")
    }
}