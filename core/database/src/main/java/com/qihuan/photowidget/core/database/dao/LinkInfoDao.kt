package com.qihuan.photowidget.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.qihuan.photowidget.core.database.model.LinkInfo

/**
 * LinkInfoDao
 * @author qi
 * @since 2021/9/27
 */
@Dao
interface LinkInfoDao {
    @Query("select * from link_info where widgetId = :id")
    suspend fun selectById(id: Int): LinkInfo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(record: LinkInfo)

    @Query("delete from link_info where widgetId = :id")
    suspend fun deleteById(id: Int)
}