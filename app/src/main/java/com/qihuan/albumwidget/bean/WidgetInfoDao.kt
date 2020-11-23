package com.qihuan.albumwidget.bean

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * PictureInfoDao
 * @author qi
 * @since 11/23/20
 */
@Dao
interface WidgetInfoDao {

    @Query("select * from widget_info where widgetId = :id")
    suspend fun selectById(id: Int): WidgetInfo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(record: WidgetInfo)

    @Query("delete from widget_info where widgetId = :id")
    suspend fun deleteById(id: Int)
}