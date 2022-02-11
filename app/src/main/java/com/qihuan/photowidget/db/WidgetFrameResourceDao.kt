package com.qihuan.photowidget.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.qihuan.photowidget.bean.WidgetFrameResource

/**
 * WidgetFrameResourceDao
 * @author qi
 * @since 2022/2/11
 */
@Dao
interface WidgetFrameResourceDao {

    @Query("select * from widget_frame_resource order by createTime desc")
    suspend fun selectList(): Array<WidgetFrameResource>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResource(resource: WidgetFrameResource)

    @Query("delete from widget_frame_resource where id = :id")
    suspend fun deleteResource(id: Int)
}