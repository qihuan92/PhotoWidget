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

/**
 * BaseConfigViewModel
 * @author qi
 * @since 2021/11/30
 */
abstract class BaseConfigViewModel(
    application: Application,
    private val appWidgetId: Int
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
    val widgetFrameResourceList by lazy { MutableLiveData<List<WidgetFrameResource>>(mutableListOf()) }

    val uiState by lazy { MutableLiveData(UIState.LOADING) }
    val imageUriList by lazy { MutableLiveData<MutableList<Uri>>(mutableListOf()) }
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
                copyToTempDir()
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
                if (widgetFrameFromDb.type == WidgetFrameType.BUILD_IN || widgetFrameFromDb.type == WidgetFrameType.IMAGE) {
                    // 复制到临时文件
                    val frameFile = widgetFrameFromDb.frameUri?.toFile()
                    if (frameFile != null && frameFile.exists()) {
                        val tempFrameFolder =
                            File(context.cacheDir, TEMP_DIR_NAME + File.separator + FRAME_DIR_NAME)
                        val tempFrameFile = File(tempFrameFolder, frameFile.name)
                        withContext(Dispatchers.IO) {
                            frameFile.copyTo(tempFrameFile, overwrite = true)
                        }
                        widgetFrameUri.value = tempFrameFile.toUri()
                    }
                }
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

    protected abstract fun displayWidget(widgetInfo: WidgetInfo)

    private suspend fun copyToTempDir() {
        val cacheDir = context.cacheDir
        val imageList = widgetDao.selectImageList(appWidgetId)
        if (imageList.isEmpty()) {
            return
        }

        val uriList = mutableListOf<Uri>()
        withContext(Dispatchers.IO) {
            val tempDir = File(cacheDir, TEMP_DIR_NAME)
            if (!tempDir.exists()) {
                tempDir.mkdirs()
            }

            imageList.forEach {
                val imageFile = it.imageUri.toFile()
                if (imageFile.exists()) {
                    val tempFile = File(tempDir, imageFile.name)
                    imageFile.copyTo(tempFile, true)
                    uriList.add(tempFile.toUri())
                }
            }
        }
        replaceImageList(uriList)
    }

    open fun addImage(uri: Uri) {
        val value = imageUriList.value
        value?.add(uri)
        imageUriList.value = value
    }

    private fun replaceImageList(uriList: List<Uri>) {
        imageUriList.value = uriList.toMutableList()
    }

    protected fun clearImageList() {
        imageUriList.value?.clear()
    }

    fun deleteImage(position: Int) {
        val value = imageUriList.value
        val uri = value?.get(position)
        value?.removeAt(value.indexOf(uri))
        imageUriList.value = value

        viewModelScope.launch {
            deleteFiles(uri)
        }
    }

    private suspend fun deleteFiles(vararg uris: Uri?) {
        if (uris.isEmpty()) {
            return
        }
        withContext(Dispatchers.IO) {
            uris.filterNotNull()
                .forEach { uri ->
                    val tempFile = uri.toFile()
                    if (tempFile.exists()) {
                        tempFile.delete()
                    }
                }
        }
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

        val uriList: List<Uri>
        try {
            uriList = saveWidgetPhotoFiles()
        } catch (e: Exception) {
            logE("BaseConfigViewModel", e.message, e)
            throw SaveWidgetException(
                e.message ?: context.getString(R.string.save_fail_copy_photo_files)
            )
        }

        val imageList = uriList.mapIndexed { index, uri ->
            WidgetImage(
                widgetId = appWidgetId,
                imageUri = uri,
                createTime = System.currentTimeMillis(),
                sort = index
            )
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

        val widgetBean = WidgetBean(widgetInfo, imageList, linkInfo.value, widgetFrame)
        widgetDao.save(widgetBean)
        updateAppWidget(context, AppWidgetManager.getInstance(context), widgetBean)
    }

    protected abstract fun getCurrentWidgetInfo(): WidgetInfo

    private suspend fun saveWidgetPhotoFiles(): List<Uri> {
        return withContext(Dispatchers.IO) {
            val widgetDir = File(context.filesDir, "widget_${appWidgetId}")
            if (widgetDir.exists() && widgetDir.isDirectory) {
                widgetDir.delete()
            }
            if (!widgetDir.exists()) {
                widgetDir.mkdirs()
            }

            val tempUriList = imageUriList.value
            val uriList = mutableListOf<Uri>()
            tempUriList?.forEach {
                val tempFile = it.toFile()
                if (tempFile.exists()) {
                    val file = File(widgetDir, tempFile.name)
                    tempFile.copyTo(file, true)
                    uriList.add(file.toUri())
                }
            }

            afterSaveFiles(widgetDir, uriList)

            if (uriList.isEmpty()) {
                logE("BaseConfigViewModel", "saveWidgetPhotoFiles() - Output uri list is Empty!")
            }

            return@withContext uriList
        }
    }

    protected open fun afterSaveFiles(widgetDir: File, uriList: List<Uri>) {
    }
}