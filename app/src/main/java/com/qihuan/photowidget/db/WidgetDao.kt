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

    suspend fun save(widgetBean: WidgetBean) {
        insertInfo(widgetBean.widgetInfo)
        insertImage(widgetBean.imageList)
    }
}