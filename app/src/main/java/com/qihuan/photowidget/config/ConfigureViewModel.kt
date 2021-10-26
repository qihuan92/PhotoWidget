package com.qihuan.photowidget.config

import android.app.Application
import android.appwidget.AppWidgetManager
import android.net.Uri
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.qihuan.photowidget.bean.*
import com.qihuan.photowidget.common.TEMP_DIR_NAME
import com.qihuan.photowidget.db.AppDatabase
import com.qihuan.photowidget.ktx.deleteDir
import com.qihuan.photowidget.updateAppWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

/**
 * ConfigureViewModel
 * @author qi
 * @since 12/16/20
 */
class ConfigureViewModel(
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

    val widgetRadius by lazy { MutableLiveData(0f) }
    val topPadding by lazy { MutableLiveData(0f) }
    val bottomPadding by lazy { MutableLiveData(0f) }
    val leftPadding by lazy { MutableLiveData(0f) }
    val rightPadding by lazy { MutableLiveData(0f) }
    val widgetTransparency by lazy { MutableLiveData(0f) }
    val autoPlayInterval by lazy { MutableLiveData(PlayInterval.NONE) }
    val photoScaleType by lazy { MutableLiveData(PhotoScaleType.CENTER_CROP) }
    val imageUriList by lazy { MutableLiveData<MutableList<Uri>>(mutableListOf()) }
    val linkInfo by lazy { MutableLiveData<LinkInfo>() }
    val uiState by lazy { MutableLiveData(UIState.LOADING) }
    val isEditState by lazy { MutableLiveData(false) }

    init {
        loadWidget()
    }

    fun addImage(uri: Uri) {
        val value = imageUriList.value
        value?.add(uri)
        imageUriList.value = value
    }

    private fun replaceImageList(uriList: List<Uri>) {
        imageUriList.value = uriList.toMutableList()
    }

    fun deleteImage(position: Int) {
        val value = imageUriList.value
        val uri = value?.get(position)
        value?.removeAt(value.indexOf(uri))
        imageUriList.value = value

        viewModelScope.launch {
            deleteFile(uri)
        }
    }

    private suspend fun deleteFile(uri: Uri?) {
        if (uri == null) {
            return
        }
        withContext(Dispatchers.IO) {
            val tempFile = uri.toFile()
            if (tempFile.exists()) {
                tempFile.delete()
            }
        }
    }

    private suspend fun copyToTempDir() {
        val cacheDir = context.cacheDir
        val imageList = widgetDao.selectImageList(appWidgetId)

        val uriList = mutableListOf<Uri>()
        withContext(Dispatchers.IO) {
            // remove compressor cache
            val compressorCacheDir = File(cacheDir, "compressor")
            compressorCacheDir.deleteDir()

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

    private fun loadWidget() {
        viewModelScope.launch {
            uiState.value = UIState.LOADING
            val widgetInfo = widgetInfoDao.selectById(appWidgetId)
            if (widgetInfo != null) {
                isEditState.value = true
                copyToTempDir()
                topPadding.value = widgetInfo.topPadding
                bottomPadding.value = widgetInfo.bottomPadding
                leftPadding.value = widgetInfo.leftPadding
                rightPadding.value = widgetInfo.rightPadding
                widgetRadius.value = widgetInfo.widgetRadius
                widgetTransparency.value = widgetInfo.widgetTransparency
                autoPlayInterval.value = widgetInfo.autoPlayInterval
                photoScaleType.value = widgetInfo.photoScaleType
            } else {
                isEditState.value = false
            }

            val linkInfoFromDb = linkInfoDao.selectById(appWidgetId)
            linkInfo.value = linkInfoFromDb

            uiState.value = UIState.SHOW_CONTENT
        }
    }

    suspend fun saveWidget() {
        val widgetInfo = WidgetInfo(
            widgetId = appWidgetId,
            topPadding = topPadding.value ?: 0f,
            bottomPadding = bottomPadding.value ?: 0f,
            leftPadding = leftPadding.value ?: 0f,
            rightPadding = rightPadding.value ?: 0f,
            widgetRadius = widgetRadius.value ?: 0f,
            widgetTransparency = widgetTransparency.value ?: 0f,
            autoPlayInterval = autoPlayInterval.value ?: PlayInterval.NONE,
            photoScaleType = photoScaleType.value ?: PhotoScaleType.CENTER_CROP,
        )

        val uriList = saveWidgetPhotoFiles()
        val imageList = uriList.mapIndexed { index, uri ->
            WidgetImage(
                widgetId = appWidgetId,
                imageUri = uri,
                createTime = System.currentTimeMillis(),
                sort = index
            )
        }

        val widgetBean = WidgetBean(widgetInfo, imageList, linkInfo.value)
        widgetDao.save(widgetBean)
        updateAppWidget(context, AppWidgetManager.getInstance(context), widgetBean)
    }

    fun deleteLink() {
        linkInfo.value = null
    }

    private suspend fun saveWidgetPhotoFiles(): List<Uri> {
        val filesDir = context.filesDir
        return withContext(Dispatchers.IO) {
            val widgetDir = File(filesDir, "widget_${appWidgetId}")
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
            return@withContext uriList
        }
    }

    fun updatePhotoScaleType(value: PhotoScaleType) {
        photoScaleType.value = value
    }

    fun updateAutoPlayInterval(value: PlayInterval) {
        autoPlayInterval.value = value
    }

    fun updateLinkInfo(value: LinkInfo?) {
        linkInfo.value = value
    }

    fun swapImageList(fromPosition: Int, toPosition: Int) {
        val list = imageUriList.value ?: mutableListOf()
        Collections.swap(list, fromPosition, toPosition)
        imageUriList.value = list
    }
}