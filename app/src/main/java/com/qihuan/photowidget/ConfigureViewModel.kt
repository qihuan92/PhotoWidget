package com.qihuan.photowidget

import android.app.Application
import android.appwidget.AppWidgetManager
import android.net.Uri
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableFloat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.qihuan.photowidget.bean.*
import com.qihuan.photowidget.common.SingleLiveEvent
import com.qihuan.photowidget.db.AppDatabase
import com.qihuan.photowidget.ktx.copyDir
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
    companion object {
        const val TEMP_DIR_NAME = "temp"
    }

    private val context by lazy { getApplication<Application>().applicationContext }
    private val widgetInfoDao by lazy { AppDatabase.getDatabase(context).widgetInfoDao() }
    private val widgetDao by lazy { AppDatabase.getDatabase(context).widgetDao() }

    val widgetRadius by lazy { ObservableFloat(0f) }
    val verticalPadding by lazy { ObservableFloat(0f) }
    val horizontalPadding by lazy { ObservableFloat(0f) }
    val reEdit by lazy { ObservableBoolean(true) }
    val autoPlayInterval by lazy { MutableLiveData<Int?>() }
    val imageUriList by lazy { MutableLiveData<MutableList<Uri>>(mutableListOf()) }
    val isLoading by lazy { SingleLiveEvent<Boolean?>(null) }
    val isDone by lazy { SingleLiveEvent(false) }
    val message by lazy { SingleLiveEvent<String>(null) }
    val linkInfo by lazy { ObservableField<LinkInfo>() }

    fun addImage(uri: Uri) {
        val value = imageUriList.value
        value?.add(uri)
        imageUriList.postValue(value)
    }

    private fun replaceImageList(uriList: List<Uri>) {
        imageUriList.postValue(uriList.toMutableList())
    }

    fun deleteImage(uri: Uri) {
        val value = imageUriList.value
        value?.removeAt(value.indexOf(uri))

        val tempFile = uri.toFile()
        if (tempFile.exists()) {
            tempFile.delete()
        }
        imageUriList.postValue(value)
    }

    private suspend fun copyToTempDir(widgetId: Int) {
        val cacheDir = context.cacheDir
        val filesDir = context.filesDir

        withContext(Dispatchers.IO) {
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
            isLoading.value = true
            val widgetInfo = widgetInfoDao.selectById(widgetId)
            if (widgetInfo != null) {
                copyToTempDir(widgetInfo.widgetId)
                verticalPadding.set(widgetInfo.verticalPadding)
                horizontalPadding.set(widgetInfo.horizontalPadding)
                widgetRadius.set(widgetInfo.widgetRadius)
                reEdit.set(widgetInfo.reEdit)

                widgetInfo.openUrl?.let {
                    if (it.startsWith("openApp/")) {
                        val info = it.split("/")
                        linkInfo.set(
                            LinkInfo(
                                LinkType.OPEN_APP,
                                "打开应用: [ ${info[1]} ]",
                                "包名: ${info[2]}",
                                it
                            )
                        )
                    } else {
                        linkInfo.set(LinkInfo(LinkType.URL, "打开链接", "地址: $it", it))
                    }
                }
                autoPlayInterval.postValue(widgetInfo.autoPlayInterval)
            }
            isLoading.value = false
        }
    }

    fun saveWidget(widgetId: Int) {
        if (imageUriList.value.isNullOrEmpty()) {
            message.value = context.getString(R.string.warning_select_picture)
            return
        }
        viewModelScope.launch {
            isLoading.value = true
            isDone.value = false

            val widgetInfo = WidgetInfo(
                widgetId,
                verticalPadding.get(),
                horizontalPadding.get(),
                widgetRadius.get(),
                autoPlayInterval.value,
                reEdit.get(),
                linkInfo.get()?.link
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

            isLoading.value = false
            isDone.value = true
        }
    }

    fun deleteLink() {
        linkInfo.set(null)
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