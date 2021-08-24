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
import com.qihuan.photowidget.ktx.copyDir
import com.qihuan.photowidget.ktx.deleteDir
import com.qihuan.photowidget.updateAppWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * ConfigureViewModel
 * @author qi
 * @since 12/16/20
 */
class ConfigureViewModel(application: Application) : AndroidViewModel(application) {
    enum class UIState {
        LOADING, SHOW_CONTENT
    }

    private val context by lazy { getApplication<Application>().applicationContext }
    private val widgetInfoDao by lazy { AppDatabase.getDatabase(context).widgetInfoDao() }
    private val widgetDao by lazy { AppDatabase.getDatabase(context).widgetDao() }

    val widgetRadius by lazy { MutableLiveData(0f) }
    val verticalPadding by lazy { MutableLiveData(0f) }
    val horizontalPadding by lazy { MutableLiveData(0f) }
    val widgetTransparency by lazy { MutableLiveData(0f) }
    val autoPlayInterval by lazy { MutableLiveData(PlayInterval.NONE) }
    val photoScaleType by lazy { MutableLiveData(PhotoScaleType.CENTER_CROP) }
    val imageUriList by lazy { MutableLiveData<MutableList<Uri>>(mutableListOf()) }
    val linkInfo by lazy { MutableLiveData<LinkInfo>() }
    val uiState by lazy { MutableLiveData(UIState.LOADING) }

    fun addImage(uri: Uri) {
        val value = imageUriList.value
        value?.add(uri)
        imageUriList.postValue(value)
    }

    private fun replaceImageList(uriList: List<Uri>) {
        imageUriList.postValue(uriList.toMutableList())
    }

    fun deleteImage(position: Int) {
        val value = imageUriList.value
        val uri = value?.get(position)
        value?.removeAt(value.indexOf(uri))
        imageUriList.postValue(value)

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

    private suspend fun copyToTempDir(widgetId: Int) {
        val cacheDir = context.cacheDir
        val filesDir = context.filesDir

        withContext(Dispatchers.IO) {
            // remove compressor cache
            val compressorCacheDir = File(cacheDir, "compressor")
            compressorCacheDir.deleteDir()

            val tempDir = File(cacheDir, TEMP_DIR_NAME)
            val widgetDir = File(filesDir, "widget_${widgetId}")
            copyDir(widgetDir, tempDir, override = true)

            if (tempDir.exists()) {
                val uriList = tempDir.listFiles()?.map { it.toUri() }
                if (uriList != null) {
                    replaceImageList(uriList)
                }
            }
        }
    }

    fun loadWidget(widgetId: Int) {
        viewModelScope.launch {
            uiState.value = UIState.LOADING
            val widgetInfo = widgetInfoDao.selectById(widgetId)
            if (widgetInfo != null) {
                copyToTempDir(widgetInfo.widgetId)
                verticalPadding.value = widgetInfo.verticalPadding
                horizontalPadding.value = widgetInfo.horizontalPadding
                widgetRadius.value = widgetInfo.widgetRadius
                widgetTransparency.value = widgetInfo.widgetTransparency
                linkInfo.value = widgetInfo.linkInfo
                autoPlayInterval.postValue(widgetInfo.autoPlayInterval)
                photoScaleType.postValue(widgetInfo.photoScaleType)
            }
            uiState.value = UIState.SHOW_CONTENT
        }
    }

    suspend fun saveWidget(widgetId: Int) {
        val widgetInfo = WidgetInfo(
            widgetId,
            verticalPadding.value ?: 0f,
            horizontalPadding.value ?: 0f,
            widgetRadius.value ?: 0f,
            widgetTransparency.value ?: 0f,
            autoPlayInterval.value ?: PlayInterval.NONE,
            linkInfo.value,
            photoScaleType.value ?: PhotoScaleType.CENTER_CROP,
        )

        val uriList = saveWidgetPhotoFiles(widgetId)
        val imageList = uriList.map {
            WidgetImage(
                widgetId = widgetId,
                imageUri = it,
                createTime = System.currentTimeMillis()
            )
        }

        val widgetBean = WidgetBean(widgetInfo, imageList)
        widgetDao.save(widgetBean)
        updateAppWidget(context, AppWidgetManager.getInstance(context), widgetBean)
    }

    fun deleteLink() {
        linkInfo.value = null
    }

    private suspend fun saveWidgetPhotoFiles(widgetId: Int): List<Uri> {
        val cacheDir = context.cacheDir
        val filesDir = context.filesDir

        return withContext(Dispatchers.IO) {
            val tempDir = File(cacheDir, TEMP_DIR_NAME)
            val widgetDir = File(filesDir, "widget_${widgetId}")
            copyDir(tempDir, widgetDir, override = true)

            val uriList = mutableListOf<Uri>()
            if (widgetDir.exists()) {
                widgetDir.listFiles()?.forEach {
                    uriList.add(it.toUri())
                }
            }
            return@withContext uriList
        }
    }
}