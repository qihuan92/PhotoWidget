package com.qihuan.photowidget.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.qihuan.photowidget.core.database.model.WidgetInfo

/**
 * PictureInfoDao
 * @author qi
 * @since 11/23/20
 */
@Dao
interface WidgetInfoDao {

    @Query("select * from widget_info where widgetId = :id")
    suspend fun selectById(id: Int): WidgetInfo?

    @Query("select * from widget_info where widgetId = :id")
    fun selectByIdSync(id: Int): WidgetInfo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(record: WidgetInfo)

    @Query("delete from widget_info where widgetId = :id")
    suspend fun deleteById(id: Int)
}