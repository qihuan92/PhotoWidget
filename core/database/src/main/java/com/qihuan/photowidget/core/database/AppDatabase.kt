package com.qihuan.photowidget.core.database

import android.content.Context
import androidx.room.*
import com.qihuan.photowidget.core.database.dao.LinkInfoDao
import com.qihuan.photowidget.core.database.dao.WidgetDao
import com.qihuan.photowidget.core.database.dao.WidgetFrameResourceDao
import com.qihuan.photowidget.core.database.dao.WidgetInfoDao
import com.qihuan.photowidget.core.database.migration.*
import com.qihuan.photowidget.core.database.model.*

/**
 * AppDatabase
 * @author qi
 * @since 2020/8/11
 */
@Database(
    entities = [
        WidgetInfo::class,
        WidgetImage::class,
        LinkInfo::class,
        WidgetFrame::class,
        WidgetFrameResource::class,
    ],
    version = 15,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 12, to = 13),
        AutoMigration(from = 13, to = 14),
        AutoMigration(from = 14, to = 15),
    ]
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun widgetInfoDao(): WidgetInfoDao

    abstract fun widgetDao(): WidgetDao

    abstract fun linkInfoDao(): LinkInfoDao

    abstract fun widgetFrameResourceDao(): WidgetFrameResourceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "photo_widget"
                ).addMigrations(
                    MigrationFor1To2(context),
                    MigrationFor2To3(context),
                    MigrationFor3To4(),
                    MigrationFor4To5(),
                    MigrationFor5To6(),
                    MigrationFor6To7(),
                    MigrationFor7To8(),
                    MigrationFor8To9(),
                    MigrationFor9To10(),
                    MigrationFor10To11(),
                    MigrationFor11To12(),
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}