package com.qihuan.photowidget.db

import androidx.paging.PagingSource
import androidx.room.*
import com.qihuan.photowidget.bean.*

/**
 * WidgetDao
 * @author qi
 * @since 12/16/20
 */
@Dao
abstract class WidgetDao {
    @Transaction
    @Query("select * from widget_info order by createTime desc")
    abstract fun selectAll(): PagingSource<Int, WidgetBean>

    @Transaction
    @Query("select * from widget_info order by createTime desc")
    abstract suspend fun selectList(): Array<WidgetBean>

    @Query("select count(*) from widget_info")
    abstract suspend fun selectWidgetCount(): Int

    @Transaction
    @Query("select * from widget_info where widgetId = :id")
    abstract suspend fun selectById(id: Int): WidgetBean?

    @Transaction
    @Query("select * from widget_info where widgetId = :id")
    abstract fun selectByIdSync(id: Int): WidgetBean?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertInfo(widgetInfo: WidgetInfo)

    @Query("select * from widget_image where widgetId = :widgetId order by sort")
    abstract suspend fun selectImageList(widgetId: Int): Array<WidgetImage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertImage(imageList: List<WidgetImage>)

    @Query("delete from widget_info where widgetId = :id")
    abstract suspend fun deleteInfoById(id: Int)

    @Query("delete from widget_image where widgetId = :id")
    abstract suspend fun deleteImageByWidgetId(id: Int)

    @Query("delete from widget_image where imageId = :id")
    abstract suspend fun deleteImageById(id: Int)

    @Query("delete from widget_image where imageId in (:idList)")
    abstract suspend fun deleteImageByIdList(idList: List<Int>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertLinkInfo(record: LinkInfo)

    @Query("delete from link_info where widgetId = :id")
    abstract suspend fun deleteLinkInfo(id: Int)

    @Query("select * from widget_frame where widgetId = :widgetId")
    abstract suspend fun selectWidgetFrameByWidgetId(widgetId: Int): WidgetFrame?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertWidgetFrame(record: WidgetFrame)

    @Query("delete from widget_frame where widgetId = :widgetId")
    abstract suspend fun deleteWidgetFrameByWidgetId(widgetId: Int)

    @Transaction
    open suspend fun save(widgetBean: WidgetBean, deleteImageList: List<WidgetImage>?) {
        insertInfo(widgetBean.widgetInfo)

        if (!deleteImageList.isNullOrEmpty()) {
            deleteImageByIdList(deleteImageList.mapNotNull { it.imageId })
        }
        insertImage(widgetBean.imageList)

        val linkInfo = widgetBean.linkInfo
        if (linkInfo != null) {
            insertLinkInfo(linkInfo)
        } else {
            deleteLinkInfo(widgetBean.widgetInfo.widgetId)
        }

        val widgetFrame = widgetBean.frame
        if (widgetFrame != null) {
            insertWidgetFrame(widgetFrame)
        } else {
            deleteWidgetFrameByWidgetId(widgetBean.widgetInfo.widgetId)
        }
    }

    suspend fun deleteByWidgetId(widgetId: Int) {
        deleteInfoById(widgetId)
        deleteImageByWidgetId(widgetId)
        deleteLinkInfo(widgetId)
    }
}