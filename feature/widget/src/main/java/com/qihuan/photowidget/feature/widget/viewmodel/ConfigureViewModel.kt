package com.qihuan.photowidget.feature.widget.viewmodel

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qihuan.photowidget.core.common.data.repository.SettingsRepository
import com.qihuan.photowidget.core.database.dao.LinkInfoDao
import com.qihuan.photowidget.core.database.dao.WidgetDao
import com.qihuan.photowidget.core.database.dao.WidgetInfoDao
import com.qihuan.photowidget.core.database.model.LinkInfo
import com.qihuan.photowidget.core.database.model.WidgetFrame
import com.qihuan.photowidget.core.database.model.WidgetFrameResource
import com.qihuan.photowidget.core.database.model.WidgetImage
import com.qihuan.photowidget.core.database.model.WidgetInfo
import com.qihuan.photowidget.core.model.PhotoScaleType
import com.qihuan.photowidget.core.model.PlayInterval
import com.qihuan.photowidget.core.model.RadiusUnit
import com.qihuan.photowidget.core.model.WidgetFrameType
import com.qihuan.photowidget.core.model.WidgetType
import com.qihuan.photowidget.feature.widget.data.repository.WidgetFrameRepository
import com.qihuan.photowidget.feature.widget.domain.model.SaveWidgetResult
import com.qihuan.photowidget.feature.widget.domain.usecase.SaveWidgetUseCase
import kotlinx.coroutines.launch
import java.util.Collections

/**
 * ConfigureViewModel
 *
 * @author qi
 * @since 2021/11/30
 */
class ConfigureViewModel(
    private val appWidgetId: Int,
    private val widgetType: WidgetType,
    private val widgetInfoDao: WidgetInfoDao,
    private val widgetDao: WidgetDao,
    private val linkInfoDao: LinkInfoDao,
    private val settingsRepository: SettingsRepository,
    private val widgetFrameRepository: WidgetFrameRepository,
    private val saveWidgetUseCase: SaveWidgetUseCase,
) : ViewModel() {

    enum class UIState {
        LOADING, SHOW_CONTENT
    }

    val imageList by lazy { MutableLiveData<MutableList<WidgetImage>>(mutableListOf()) }
    private val deleteImageList by lazy { mutableListOf<WidgetImage>() }

    val widgetFrameResourceList by lazy { MutableLiveData<List<WidgetFrameResource>>(mutableListOf()) }
    private var unsavedWidgetFrameUri: Uri? = null

    val topPadding by lazy { MutableLiveData(0f) }
    val bottomPadding by lazy { MutableLiveData(0f) }
    val leftPadding by lazy { MutableLiveData(0f) }
    val rightPadding by lazy { MutableLiveData(0f) }
    val widgetRadius by lazy { MutableLiveData(0f) }
    val widgetRadiusUnit by lazy { MutableLiveData(RadiusUnit.LENGTH) }
    val linkInfo by lazy { MutableLiveData<LinkInfo>() }
    val widgetFrameType by lazy { MutableLiveData(WidgetFrameType.NONE) }
    val widgetFrameUri by lazy { MutableLiveData<Uri>() }
    val widgetFrameColor by lazy { MutableLiveData<String>() }
    val widgetFrameWidth by lazy { MutableLiveData(0f) }
    val widgetTransparency by lazy { MutableLiveData(0f) }
    val autoPlayInterval by lazy { MutableLiveData(PlayInterval.NONE) }
    val photoScaleType by lazy { MutableLiveData(if (widgetType == WidgetType.GIF) PhotoScaleType.FIT_CENTER else PhotoScaleType.CENTER_CROP) }

    val uiState by lazy { MutableLiveData(UIState.LOADING) }
    val isEditState by lazy { MutableLiveData(false) }

    init {
        loadWidget()
    }

    private fun loadWidget() {
        viewModelScope.launch {
            uiState.value = UIState.LOADING
            widgetFrameResourceList.value = widgetFrameRepository.getWidgetFrameResourceList()

            val widgetInfo = widgetInfoDao.selectById(appWidgetId)
            if (widgetInfo != null) {
                isEditState.value = true
                imageList.value = widgetDao.selectImageList(appWidgetId).toMutableList()
                displayWidget(widgetInfo)
            } else {
                isEditState.value = false
                val defaultWidgetInfo = getDefaultWidgetInfo()
                displayWidget(defaultWidgetInfo)
            }

            val linkInfoFromDb = linkInfoDao.selectById(appWidgetId)
            linkInfo.value = linkInfoFromDb

            val widgetFrameFromDb = widgetDao.selectWidgetFrameByWidgetId(appWidgetId)
            if (widgetFrameFromDb != null) {
                widgetFrameType.value = widgetFrameFromDb.type
                widgetFrameWidth.value = widgetFrameFromDb.width
                widgetFrameColor.value = widgetFrameFromDb.frameColor
                widgetFrameUri.value = widgetFrameFromDb.frameUri
            }

            uiState.value = UIState.SHOW_CONTENT
        }
    }

    private suspend fun getDefaultWidgetInfo(): WidgetInfo {
        val (defaultRadius, defaultRadiusUnit) = settingsRepository.getWidgetDefaultRadius()
        return WidgetInfo(
            widgetId = appWidgetId,
            topPadding = 0f,
            bottomPadding = 0f,
            leftPadding = 0f,
            rightPadding = 0f,
            widgetRadius = defaultRadius,
            widgetRadiusUnit = defaultRadiusUnit,
            widgetTransparency = 0f,
            autoPlayInterval = PlayInterval.NONE,
            photoScaleType = settingsRepository.getWidgetDefaultScaleType(),
        )
    }

    private fun displayWidget(widgetInfo: WidgetInfo) {
        topPadding.value = widgetInfo.topPadding
        bottomPadding.value = widgetInfo.bottomPadding
        leftPadding.value = widgetInfo.leftPadding
        rightPadding.value = widgetInfo.rightPadding
        widgetRadius.value = widgetInfo.widgetRadius
        widgetRadiusUnit.value = widgetInfo.widgetRadiusUnit
        widgetTransparency.value = widgetInfo.widgetTransparency
        autoPlayInterval.value = widgetInfo.autoPlayInterval
        photoScaleType.value = widgetInfo.photoScaleType
    }

    fun addImage(uri: Uri) {
        val imageListValue = imageList.value
        val widgetImage = WidgetImage(
            null,
            appWidgetId,
            uri,
            System.currentTimeMillis(),
            imageListValue?.size ?: 0
        )
        imageListValue?.add(widgetImage)
        imageList.value = imageListValue
    }

    fun deleteImage(position: Int) {
        val imageListValue = imageList.value
        val deleteImage = imageListValue?.removeAt(position)
        if (deleteImage?.imageId != null) {
            deleteImageList.add(deleteImage)
        }
        imageList.value = imageListValue
    }

    fun deleteLink() {
        linkInfo.value = null
    }

    fun updateLinkInfo(value: LinkInfo?) {
        linkInfo.value = value
    }

    fun updateRadiusUnit(item: RadiusUnit) {
        if (widgetRadiusUnit.value == item) {
            return
        }
        widgetRadius.value = 0f
        widgetRadiusUnit.value = item
    }

    fun updatePhotoScaleType(value: PhotoScaleType) {
        photoScaleType.value = value
    }

    fun updateAutoPlayInterval(value: PlayInterval) {
        autoPlayInterval.value = value
    }

    fun swapImageList(fromPosition: Int, toPosition: Int) {
        val list = imageList.value ?: mutableListOf()
        Collections.swap(list, fromPosition, toPosition)
        imageList.value = list
    }

    fun setWidgetFrame(
        type: WidgetFrameType,
        color: String? = null,
        uri: Uri? = null
    ) {
        if (type == WidgetFrameType.NONE) {
            widgetFrameWidth.value = 0f
        } else {
            if (widgetFrameWidth.value == 0f) {
                widgetFrameWidth.value = 10f
            }
        }

        unsavedWidgetFrameUri = uri
        widgetFrameUri.value = uri
        widgetFrameType.value = type
        widgetFrameColor.value = color
    }

    suspend fun saveWidget(): SaveWidgetResult {
        return saveWidgetUseCase(
            getCurrentWidgetInfo(), WidgetFrame(
                widgetId = appWidgetId,
                frameUri = unsavedWidgetFrameUri,
                frameColor = widgetFrameColor.value,
                width = widgetFrameWidth.value ?: 0f,
                type = widgetFrameType.value ?: WidgetFrameType.NONE
            ), linkInfo.value, imageList.value, deleteImageList
        )
    }

    private fun getCurrentWidgetInfo(): WidgetInfo {
        return WidgetInfo(
            widgetId = appWidgetId,
            topPadding = topPadding.value ?: 0f,
            bottomPadding = bottomPadding.value ?: 0f,
            leftPadding = leftPadding.value ?: 0f,
            rightPadding = rightPadding.value ?: 0f,
            widgetRadius = widgetRadius.value ?: 0f,
            widgetRadiusUnit = widgetRadiusUnit.value ?: RadiusUnit.LENGTH,
            widgetTransparency = widgetTransparency.value ?: 0f,
            autoPlayInterval = autoPlayInterval.value ?: PlayInterval.NONE,
            photoScaleType = photoScaleType.value ?: PhotoScaleType.CENTER_CROP,
            widgetType = widgetType
        )
    }
}