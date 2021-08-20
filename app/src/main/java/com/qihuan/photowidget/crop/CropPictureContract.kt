package com.qihuan.photowidget.crop

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.util.TypedValue
import androidx.activity.result.contract.ActivityResultContract
import com.qihuan.photowidget.R
import com.qihuan.photowidget.common.TEMP_DIR_NAME
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

        val outDir = File(context.cacheDir, TEMP_DIR_NAME)
        if (!outDir.exists()) {
            outDir.mkdirs()
        }
        tempOutFile = File(outDir, "${System.currentTimeMillis()}.png")

        val intent = UCrop.of(input, Uri.fromFile(tempOutFile))
            .withOptions(UCrop.Options().apply {
                val value = TypedValue()
                context.theme.resolveAttribute(R.attr.colorSecondary, value, true)
                val mainColor = value.data

                setStatusBarColor(mainColor)
                setToolbarWidgetColor(Color.WHITE)
                setToolbarColor(mainColor)
                setActiveControlsWidgetColor(mainColor)
                setCompressionFormat(Bitmap.CompressFormat.PNG)
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