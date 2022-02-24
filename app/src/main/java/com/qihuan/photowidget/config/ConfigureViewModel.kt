package com.qihuan.photowidget.config

import android.app.Application
import android.appwidget.AppWidgetManager
import android.net.Uri
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.qihuan.photowidget.R
import com.qihuan.photowidget.bean.*
import com.qihuan.photowidget.common.*
import com.qihuan.photowidget.db.AppDatabase
import com.qihuan.photowidget.frame.WidgetFrameRepository
import com.qihuan.photowidget.ktx.*
import com.qihuan.photowidget.settings.SettingsRepository
import com.qihuan.photowidget.updateAppWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

/**
 * ConfigureViewModel
 *
 * @author qi
 * @since 2021/11/30
 */
class ConfigureViewModel(
    application: Application,
    private val appWidgetId: Int,
    private val widgetType: WidgetType
) : AndroidViewModel(application) {
    enum class UIState {
        LOADING, SHOW_CONTENT
    }

    private val context by lazy { getApplication<Application>().applicationContext }
    private val widgetInfoDao by lazy { AppDatabase.getDatabase(context).widgetInfoDao() }
    private val widgetDao by lazy { AppDatabase.getDatabase(context).widgetDao() }
    private val linkInfoDao by lazy { AppDatabase.getDatabase(context).linkInfoDao() }
    private val settingsRepository by lazy { SettingsRepository(application) }
    private val widgetFrameRepository by lazy { WidgetFrameRepository(application) }

    val imageList by lazy { MutableLiveData<MutableList<WidgetImage>>(mutableListOf()) }
    private val deleteImageList by lazy { mutableListOf<WidgetImage>() }
    val widgetFrameResourceList by lazy { MutableLiveData<List<WidgetFrameResource>>(mutableListOf()) }

    val topPadding by lazy { MutableLiveData(0f) }
    val bottomPadding by lazy { MutableLiveData(0f) }
    val leftPadding by lazy { MutableLiveData(0f) }
    val rightPadding by lazy { MutableLiveData(0f) }
    val widgetRadius by lazy { MutableLiveData(0f) }
    val widgetRadiusUnit by lazy { MutableLiveData(RadiusUnit.ANGLE) }
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
    val isFrameLoading = MutableLiveData(false)

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
//                if (widgetFrameFromDb.type == WidgetFrameType.BUILD_IN || widgetFrameFromDb.type == WidgetFrameType.IMAGE) {
//                    // 复制到临时文件
//                    val frameFile = widgetFrameFromDb.frameUri?.toFile()
//                    if (frameFile != null && frameFile.exists()) {
//                        val tempFrameFolder =
//                            File(context.cacheDir, TEMP_DIR_NAME + File.separator + FRAME_DIR_NAME)
//                        val tempFrameFile = File(tempFrameFolder, frameFile.name)
//                        withContext(Dispatchers.IO) {
//                            frameFile.copyTo(tempFrameFile, overwrite = true)
//                        }
//                        widgetFrameUri.value = tempFrameFile.toUri()
//                    }
//                }
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

    suspend fun setWidgetFrame(
        type: WidgetFrameType,
        color: String? = null,
        uri: Uri? = null
    ) {
        if (isFrameLoading.value == true) {
            return
        }

        isFrameLoading.value = true
        if (type == WidgetFrameType.NONE) {
            widgetFrameWidth.value = 0f
        } else {
            if (widgetFrameWidth.value == 0f) {
                widgetFrameWidth.value = 10f
            }
        }

        val tempFrameFolder =
            File(context.cacheDir, TEMP_DIR_NAME + File.separator + FRAME_DIR_NAME)
        if (tempFrameFolder.exists()) {
            tempFrameFolder.deleteRecursively()
        }
        tempFrameFolder.mkdirs()

        if (type == WidgetFrameType.BUILD_IN) {
            val assetName = uri?.path?.substringAfterLast("android_asset/")
            if (assetName != null) {
                val fileName = assetName.substringAfterLast("frame/")
                val file = File(tempFrameFolder, fileName)
                context.copyAssetsFile(assetName, file)
                widgetFrameUri.value = file.toUri()
            }
        } else if (type == WidgetFrameType.IMAGE) {
            if (uri != null) {
                val fileExt = uri.getExtension(context)
                val fileName = if (fileExt != null) "custom_frame.${fileExt}" else "custom_frame"
                val file = File(tempFrameFolder, fileName)
                context.copyFile(uri, file)
                val compressFile = context.compressImageFile(file)
                widgetFrameUri.value = compressFile.toUri()
            }
        } else {
            widgetFrameUri.value = uri
        }

        widgetFrameType.value = type
        widgetFrameColor.value = color

        isFrameLoading.value = false
    }

    @Throws(SaveWidgetException::class)
    suspend fun saveWidget() {
        val widgetInfo = getCurrentWidgetInfo()

        // Delete image files.
        deleteImageList.forEach {
            try {
                it.imageUri.toFile().delete()
            } catch (e: Exception) {
                logE("ConfigureViewModel", "Delete image fail: " + e.message, e)
            }
        }

        // Save new image files.
        val newImageList = imageList.value
        newImageList?.filter { it.imageId == null }?.forEach { widgetImage ->
            val destFileDir = File(context.filesDir, "widget_${appWidgetId}")
            if (!destFileDir.exists()) {
                destFileDir.mkdirs()
            }
            val imageUri = widgetImage.imageUri
            val fileExtension = imageUri.getExtension(context) ?: FileExtension.PNG
            val fileName = "${System.currentTimeMillis()}.${fileExtension}"
            val destFile = File(destFileDir, fileName)
            context.copyFile(imageUri, destFile)
            if (widgetType == WidgetType.GIF) {
                widgetImage.imageUri = destFile.toUri()
                withContext(Dispatchers.IO) {
                    destFile.toUri().saveGifFramesToDir(
                        File(destFileDir, destFile.nameWithoutExtension),
                        widgetRadius.value ?: 0f,
                        widgetRadiusUnit.value ?: RadiusUnit.ANGLE
                    )
                }
            } else {
                val compressedFile = context.compressImageFile(destFile)
                widgetImage.imageUri = compressedFile.toUri()
            }
        }

        // Reorder.
        newImageList?.forEachIndexed { index, widgetImage ->
            widgetImage.sort = index
        }

        var frameFileUri: Uri? = null
        val widgetFrame = if (widgetFrameType.value == WidgetFrameType.NONE) {
            null
        } else {
            // 相框文件目录
            val frameFolder =
                File(context.filesDir, "widget_${appWidgetId}" + File.separator + FRAME_DIR_NAME)
            // 删除原来相框文件
            if (frameFolder.exists()) {
                withContext(Dispatchers.IO) { frameFolder.deleteRecursively() }
            }

            // 保存相框文件
            if (widgetFrameType.value == WidgetFrameType.BUILD_IN || widgetFrameType.value == WidgetFrameType.IMAGE) {
                val tempFrameFile = widgetFrameUri.value?.toFile()

                if (tempFrameFile == null || !tempFrameFile.exists()) {
                    throw SaveWidgetException(context.getString(R.string.save_fail_frame_file_not_exists))
                }
                val frameFile = File(frameFolder, tempFrameFile.name)
                withContext(Dispatchers.IO) {
                    tempFrameFile.copyTo(frameFile, overwrite = true)
                }
                frameFileUri = frameFile.toUri()
            }

            WidgetFrame(
                widgetId = appWidgetId,
                frameUri = frameFileUri,
                frameColor = widgetFrameColor.value,
                width = widgetFrameWidth.value ?: 0f,
                type = widgetFrameType.value ?: WidgetFrameType.NONE
            )
        }

        val widgetBean = WidgetBean(widgetInfo, newImageList.orEmpty(), linkInfo.value, widgetFrame)
        widgetDao.save(widgetBean, deleteImageList)
        updateAppWidget(context, AppWidgetManager.getInstance(context), widgetBean)
    }

    private fun getCurrentWidgetInfo(): WidgetInfo {
        return WidgetInfo(
            widgetId = appWidgetId,
            topPadding = topPadding.value ?: 0f,
            bottomPadding = bottomPadding.value ?: 0f,
            leftPadding = leftPadding.value ?: 0f,
            rightPadding = rightPadding.value ?: 0f,
            widgetRadius = widgetRadius.value ?: 0f,
            widgetRadiusUnit = widgetRadiusUnit.value ?: RadiusUnit.ANGLE,
            widgetTransparency = widgetTransparency.value ?: 0f,
            autoPlayInterval = autoPlayInterval.value ?: PlayInterval.NONE,
            photoScaleType = photoScaleType.value ?: PhotoScaleType.CENTER_CROP,
            widgetType = widgetType
        )
    }
}