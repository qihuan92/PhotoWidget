package com.qihuan.photowidget.feature.widget.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.util.TypedValue
import androidx.activity.result.contract.ActivityResultContract
import com.qihuan.photowidget.core.common.ktx.createOrExistsDir
import com.qihuan.photowidget.core.common.ktx.getExtension
import com.qihuan.photowidget.core.model.FileExtension
import com.qihuan.photowidget.core.model.MimeType
import com.qihuan.photowidget.core.model.TEMP_DIR_NAME
import com.qihuan.photowidget.feature.widget.R
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.UCropActivity
import java.io.File


/**
 * CropPictureContract
 * @author qi
 * @since 11/20/20
 */
class CropPictureContract : ActivityResultContract<Uri, Uri?>() {
    private var tempOutFile: File? = null

    override fun createIntent(context: Context, input: Uri): Intent {
        val inputMimeType = context.contentResolver.getType(input)
        val outDir = File(context.cacheDir, TEMP_DIR_NAME).apply { createOrExistsDir() }
        val fileExtension = input.getExtension(context) ?: FileExtension.WEBP
        val fileName = "${System.currentTimeMillis()}.${fileExtension}"
        tempOutFile = File(outDir, fileName)

        val intent = UCrop.of(input, Uri.fromFile(tempOutFile))
            .withOptions(UCrop.Options().apply {
                val value = TypedValue()
                context.theme.resolveAttribute(R.attr.colorSecondary, value, true)
                val mainColor = value.data

                setStatusBarColor(mainColor)
                setToolbarWidgetColor(Color.WHITE)
                setToolbarColor(mainColor)
                setActiveControlsWidgetColor(mainColor)
                setCompressionFormat(MimeType.getCompressFormatByMimeType(inputMimeType))
                setCompressionQuality(100)
                setAllowedGestures(UCropActivity.SCALE, UCropActivity.ROTATE, UCropActivity.SCALE)
            })
            .getIntent(context)
        intent.setClass(context, CropActivity::class.java)
        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (resultCode == Activity.RESULT_OK && intent != null) {
            UCrop.getOutput(intent)
        } else {
            tempOutFile?.delete()
            null
        }
    }
}