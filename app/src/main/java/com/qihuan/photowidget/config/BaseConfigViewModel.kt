package com.qihuan.photowidget.config

import android.app.Application
import android.appwidget.AppWidgetManager
import android.net.Uri
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.qihuan.photowidget.bean.LinkInfo
import com.qihuan.photowidget.bean.WidgetBean
import com.qihuan.photowidget.bean.WidgetImage
import com.qihuan.photowidget.bean.WidgetInfo
import com.qihuan.photowidget.common.PlayInterval
import com.qihuan.photowidget.common.RadiusUnit
import com.qihuan.photowidget.common.TEMP_DIR_NAME
import com.qihuan.photowidget.db.AppDatabase
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
    private val repository by lazy { SettingsRepository(application) }

    val topPadding by lazy { MutableLiveData(0f) }
    val bottomPadding by lazy { MutableLiveData(0f) }
    val leftPadding by lazy { MutableLiveData(0f) }
    val rightPadding by lazy { MutableLiveData(0f) }
    val widgetRadius by lazy { MutableLiveData(0f) }
    val widgetRadiusUnit by lazy { MutableLiveData(RadiusUnit.ANGLE) }
    val linkInfo by lazy { MutableLiveData<LinkInfo>() }
    val uiState by lazy { MutableLiveData(UIState.LOADING) }
    val imageUriList by lazy { MutableLiveData<MutableList<Uri>>(mutableListOf()) }
    val isEditState by lazy { MutableLiveData(false) }

    init {
        loadWidget()
    }

    private fun loadWidget() {
        viewModelScope.launch {
            uiState.value = UIState.LOADING
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

            uiState.value = UIState.SHOW_CONTENT
        }
    }

    private suspend fun getDefaultWidgetInfo(): WidgetInfo {
        val (defaultRadius, defaultRadiusUnit) = repository.getWidgetDefaultRadius()
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
            photoScaleType = repository.getWidgetDefaultScaleType(),
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

    suspend fun saveWidget(): Boolean {
        val widgetInfo = getCurrentWidgetInfo()

        val uriList: List<Uri>
        try {
            uriList = saveWidgetPhotoFiles()
        } catch (e: Exception) {
            return false
        }

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
        return true
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

            onPostSaveFiles(widgetDir, uriList)

            return@withContext uriList
        }
    }

    protected abstract fun onPostSaveFiles(widgetDir: File, uriList: List<Uri>)
}