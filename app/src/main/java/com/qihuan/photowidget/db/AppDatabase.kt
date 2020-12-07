package com.qihuan.photowidget.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.qihuan.photowidget.bean.WidgetInfo
import com.qihuan.photowidget.bean.WidgetInfoDao
import com.qihuan.photowidget.db.migration.MigrationFor1To2

/**
 * AppDatabase
 * @author qi
 * @since 2020/8/11
 */
@Database(
    entities = [
        WidgetInfo::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun widgetInfoDao(): WidgetInfoDao

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
                    MigrationFor1To2(context)
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}