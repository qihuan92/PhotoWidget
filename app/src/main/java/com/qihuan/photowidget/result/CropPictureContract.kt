package com.qihuan.photowidget.result

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.util.TypedValue
import androidx.activity.result.contract.ActivityResultContract
import com.qihuan.photowidget.bean.CropPictureInfo
import com.yalantis.ucrop.UCrop


/**
 * CropPictureContract
 * @author qi
 * @since 11/20/20
 */
class CropPictureContract : ActivityResultContract<CropPictureInfo, Uri?>() {
    override fun createIntent(context: Context, input: CropPictureInfo): Intent {
        return UCrop.of(input.inUri, input.outUri)
            .withOptions(UCrop.Options().apply {
                val value = TypedValue()
                context.theme.resolveAttribute(android.R.attr.colorPrimary, value, true)
                val mainColor = value.data

                setStatusBarColor(mainColor)
                setToolbarWidgetColor(Color.WHITE)
                setToolbarColor(mainColor)
                setActiveControlsWidgetColor(mainColor)
                setCompressionFormat(Bitmap.CompressFormat.PNG)
            })
            .withMaxResultSize(1000, 1000)
            .getIntent(context)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        if (resultCode == Activity.RESULT_OK && intent != null) {
            return UCrop.getOutput(intent)
        }
        return null
    }
}