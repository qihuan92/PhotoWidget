package com.qihuan.photowidget

import android.app.Application
import android.net.Uri
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
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

    val widgetRadius by lazy { ObservableField(0f) }
    val verticalPadding by lazy { ObservableField(0f) }
    val horizontalPadding by lazy { ObservableField(0f) }
    val autoPlayInterval by lazy { MutableLiveData<Int?>() }
    val imageUriList by lazy { MutableLiveData<MutableList<Uri>>(mutableListOf()) }

    fun addImage(uri: Uri) {
        val value = imageUriList.value
        value?.add(uri)
        imageUriList.postValue(value)
    }

    fun replaceImageList(uriList: List<Uri>) {
        imageUriList.postValue(uriList.toMutableList())
    }

    fun deleteImage(position: Int, uri: Uri) {
        val value = imageUriList.value
        value?.removeAt(position)

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
            val widgetInfo = widgetInfoDao.selectById(widgetId)
            if (widgetInfo != null) {
                copyToTempDir(widgetInfo.widgetId)
                verticalPadding.set(widgetInfo.verticalPadding)
                horizontalPadding.set(widgetInfo.horizontalPadding)
                widgetRadius.set(widgetInfo.widgetRadius)
                autoPlayInterval.postValue(widgetInfo.autoPlayInterval)
            }
        }
    }

    fun saveWidget(widgetId: Int) {
        // todo
    }
}