package com.qihuan.photowidget.db

import androidx.room.*
import com.qihuan.photowidget.bean.WidgetBean
import com.qihuan.photowidget.bean.WidgetImage
import com.qihuan.photowidget.bean.WidgetInfo

/**
 * WidgetDao
 * @author qi
 * @since 12/16/20
 */
@Dao
abstract class WidgetDao {
    @Transaction
    @Query("select * from widget_info where widgetId = :id")
    abstract suspend fun selectById(id: Int): WidgetBean?

    @Transaction
    @Query("select * from widget_info where widgetId = :id")
    abstract fun selectByIdSync(id: Int): WidgetBean?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertInfo(widgetInfo: WidgetInfo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertImage(imageList: List<WidgetImage>)

    @Query("delete from widget_info where widgetId = :id")
    abstract suspend fun deleteInfoById(id: Int)

    @Query("delete from widget_image where widgetId = :id")
    abstract suspend fun deleteImageByWidgetId(id: Int)

    @Query("delete from widget_image where imageId = :id")
    abstract suspend fun deleteImageById(id: Int)

    suspend fun save(widgetBean: WidgetBean) {
        insertInfo(widgetBean.widgetInfo)
        deleteImageByWidgetId(widgetBean.widgetInfo.widgetId)
        insertImage(widgetBean.imageList)
    }

    suspend fun deleteByWidgetId(widgetId: Int) {
        deleteInfoById(widgetId)
        deleteImageByWidgetId(widgetId)
    }
}