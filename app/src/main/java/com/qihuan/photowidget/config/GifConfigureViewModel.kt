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
import com.qihuan.photowidget.ktx.saveGifFramesToDir
import com.qihuan.photowidget.updateAppWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * GifConfigureViewModel
 * @author qi
 * @since 2021-10-27
 */
class GifConfigureViewModel(
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

    val topPadding by lazy { MutableLiveData(0f) }
    val bottomPadding by lazy { MutableLiveData(0f) }
    val leftPadding by lazy { MutableLiveData(0f) }
    val rightPadding by lazy { MutableLiveData(0f) }
    val widgetRadius by lazy { MutableLiveData(0f) }
    val imageUri by lazy { MutableLiveData<Uri>() }
    val linkInfo by lazy { MutableLiveData<LinkInfo>() }
    val uiState by lazy { MutableLiveData(UIState.LOADING) }
    val isEditState by lazy { MutableLiveData(false) }

    init {
        loadWidget()
    }

    fun addImage(uri: Uri) {
        imageUri.value = uri
    }

    private fun replaceImage(uri: Uri?) {
        imageUri.value = uri
    }

    private suspend fun copyToTempDir() {
        val cacheDir = context.cacheDir
        val imageList = widgetDao.selectImageList(appWidgetId)
        if (imageList.isEmpty()) {
            return
        }
        val image = imageList.first()
        var uri: Uri? = null
        withContext(Dispatchers.IO) {
            val tempDir = File(cacheDir, TEMP_DIR_NAME)
            if (!tempDir.exists()) {
                tempDir.mkdirs()
            }

            val imageFile = image.imageUri.toFile()
            if (imageFile.exists()) {
                val tempFile = File(tempDir, imageFile.name)
                imageFile.copyTo(tempFile, true)
                uri = tempFile.toUri()
            }
        }
        replaceImage(uri)
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
            } else {
                isEditState.value = false
            }

            val linkInfoFromDb = linkInfoDao.selectById(appWidgetId)
            linkInfo.value = linkInfoFromDb

            uiState.value = UIState.SHOW_CONTENT
        }
    }

    suspend fun saveWidget(): Boolean {
        val uri = saveWidgetPhotoFile() ?: return false

        val widgetInfo = WidgetInfo(
            widgetId = appWidgetId,
            topPadding = topPadding.value ?: 0f,
            bottomPadding = bottomPadding.value ?: 0f,
            leftPadding = leftPadding.value ?: 0f,
            rightPadding = rightPadding.value ?: 0f,
            widgetRadius = widgetRadius.value ?: 0f,
            widgetTransparency = 0f,
            photoScaleType = PhotoScaleType.CENTER_CROP,
            widgetType = WidgetType.GIF,
        )


        val imageList = mutableListOf(
            WidgetImage(
                widgetId = appWidgetId,
                imageUri = uri,
                createTime = System.currentTimeMillis(),
                sort = 0
            )
        )

        val widgetBean = WidgetBean(widgetInfo, imageList, linkInfo.value)
        widgetDao.save(widgetBean)
        updateAppWidget(context, AppWidgetManager.getInstance(context), widgetBean)

        return true
    }

    fun deleteLink() {
        linkInfo.value = null
    }

    private suspend fun saveWidgetPhotoFile(): Uri? {
        val filesDir = context.filesDir
        var uri: Uri? = null

        withContext(Dispatchers.IO) {
            val widgetDir = File(filesDir, "widget_${appWidgetId}")
            if (widgetDir.exists() && widgetDir.isDirectory) {
                widgetDir.deleteRecursively()
            }
            if (!widgetDir.exists()) {
                widgetDir.mkdirs()
            }

            val tempUri = imageUri.value
            val tempFile = tempUri?.toFile()
            if (tempFile != null && tempFile.exists()) {
                val file = File(widgetDir, tempFile.name)
                tempFile.copyTo(file, true)
                uri = file.toUri()
                try {
                    uri?.saveGifFramesToDir(File(widgetDir, file.nameWithoutExtension))
                } catch (e: Exception) {
                    uri = null
                }
            }
        }
        return uri
    }

    fun updateLinkInfo(value: LinkInfo?) {
        linkInfo.value = value
    }
}