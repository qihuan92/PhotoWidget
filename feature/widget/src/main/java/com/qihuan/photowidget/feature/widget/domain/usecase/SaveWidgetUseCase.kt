package com.qihuan.photowidget.feature.widget.domain.usecase

import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.qihuan.photowidget.core.common.CopyFileException
import com.qihuan.photowidget.core.common.ktx.compressImageFile
import com.qihuan.photowidget.core.common.ktx.copyFileSmart
import com.qihuan.photowidget.core.common.ktx.createOrExistsDir
import com.qihuan.photowidget.core.common.ktx.getExtension
import com.qihuan.photowidget.core.common.ktx.logE
import com.qihuan.photowidget.core.common.ktx.saveGifFramesToDir
import com.qihuan.photowidget.core.database.dao.WidgetDao
import com.qihuan.photowidget.core.database.model.LinkInfo
import com.qihuan.photowidget.core.database.model.WidgetBean
import com.qihuan.photowidget.core.database.model.WidgetFrame
import com.qihuan.photowidget.core.database.model.WidgetImage
import com.qihuan.photowidget.core.database.model.WidgetInfo
import com.qihuan.photowidget.core.model.FRAME_DIR_NAME
import com.qihuan.photowidget.core.model.FileExtension
import com.qihuan.photowidget.core.model.WidgetFrameType
import com.qihuan.photowidget.core.model.WidgetType
import com.qihuan.photowidget.feature.widget.R
import com.qihuan.photowidget.feature.widget.domain.model.SaveWidgetResult
import com.qihuan.photowidget.feature.widget.getWidgetDir
import com.qihuan.photowidget.feature.widget.updateAppWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * @Author : qi
 * @Date : 2024/1/18 16:50
 * @Description :
 **/
class SaveWidgetUseCase(
    private val context: Context,
    private val widgetDao: WidgetDao,
) {
    companion object {
        private const val TAG = "SaveWidgetUseCase"
    }

    suspend operator fun invoke(
        widgetInfo: WidgetInfo,
        widgetFrame: WidgetFrame,
        linkInfo: LinkInfo?,
        imageList: List<WidgetImage>?,
        deleteImageList: List<WidgetImage>?
    ): SaveWidgetResult {
        if (imageList.isNullOrEmpty()) {
            return SaveWidgetResult.Fail(R.string.warning_select_picture)
        }

        deleteImageList?.let { deleteImages(it) }
        val saveImagesResult = runCatching {
            saveImages(context, widgetInfo, imageList)
        }.onFailure {
            logE(TAG, "saveImages error: ${it.message}", it)
        }

        if (saveImagesResult.isFailure) {
            return SaveWidgetResult.Fail(R.string.save_fail)
        }

        // Save photo frame file.
        val frame = saveWidgetFrame(context, widgetFrame)

        val widgetBean = WidgetBean(widgetInfo, imageList, linkInfo, frame)
        widgetDao.save(widgetBean, deleteImageList)
        context.updateAppWidget(widgetBean)
        return SaveWidgetResult.Success
    }

    private suspend fun deleteImages(list: List<WidgetImage>) {
        withContext(Dispatchers.IO) {
            list.forEach {
                runCatching {
                    it.imageUri.toFile().delete()
                }.onFailure { e ->
                    logE("ConfigureViewModel", "Delete image fail: " + e.message, e)
                }
            }
        }
    }

    private suspend fun saveImages(
        context: Context,
        widgetInfo: WidgetInfo,
        list: List<WidgetImage>
    ) {
        if (list.isEmpty()) {
            return
        }

        val widgetFileDir = context.getWidgetDir(list.first().widgetId).apply {
            createOrExistsDir()
        }

        list.filter { it.imageId == null }.forEach { widgetImage ->
            val imageUri = widgetImage.imageUri
            val fileExtension = imageUri.getExtension(context) ?: FileExtension.PNG
            val fileName = "${System.currentTimeMillis()}.${fileExtension}"
            val destFile = File(widgetFileDir, fileName)
            withContext(Dispatchers.IO) {
                context.copyFileSmart(imageUri, destFile)
            }
            if (widgetInfo.widgetType == WidgetType.GIF) {
                widgetImage.imageUri = destFile.toUri()
                withContext(Dispatchers.IO) {
                    destFile.toUri().saveGifFramesToDir(
                        context,
                        File(widgetFileDir, destFile.nameWithoutExtension),
                        widgetInfo.widgetRadius,
                        widgetInfo.widgetRadiusUnit
                    )
                }
            } else {
                val compressedFile = context.compressImageFile(destFile)
                widgetImage.imageUri = compressedFile.toUri()
            }
        }

        // Reorder.
        list.forEachIndexed { index, widgetImage ->
            widgetImage.sort = index
        }
    }

    private suspend fun saveWidgetFrame(context: Context, widgetFrame: WidgetFrame): WidgetFrame? {
        if (widgetFrame.type == WidgetFrameType.NONE) {
            return null
        }
        val widgetFileDir = context.getWidgetDir(widgetFrame.widgetId).apply {
            createOrExistsDir()
        }
        var frameUri: Uri? = null
        widgetFrame.frameUri?.let {
            val frameDir = File(widgetFileDir, FRAME_DIR_NAME).apply {
                if (exists()) {
                    withContext(Dispatchers.IO) { deleteRecursively() }
                }
                createOrExistsDir()
            }

            val frameFileName = "${System.currentTimeMillis()}.${it.getExtension(context)}"
            val frameFile = File(frameDir, frameFileName)
            withContext(Dispatchers.IO) {
                try {
                    context.copyFileSmart(it, frameFile)
                } catch (e: CopyFileException) {
                    logE(TAG, "saveWidgetFrame copy error: ${e.message}", e)
                    return@withContext null
                }
            }

            frameUri = frameFile.toUri()
            if (widgetFrame.type == WidgetFrameType.IMAGE) {
                frameUri = context.compressImageFile(frameFile).toUri()
            }
        }
        return widgetFrame.copy(frameUri = frameUri)
    }
}