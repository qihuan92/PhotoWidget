package com.qihuan.photowidget.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.qihuan.photowidget.bean.LinkInfo
import com.qihuan.photowidget.bean.WidgetImage
import com.qihuan.photowidget.bean.WidgetInfo
import com.qihuan.photowidget.db.migration.*

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
    ],
    version = 12,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun widgetInfoDao(): WidgetInfoDao

    abstract fun widgetDao(): WidgetDao

    abstract fun linkInfoDao(): LinkInfoDao

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