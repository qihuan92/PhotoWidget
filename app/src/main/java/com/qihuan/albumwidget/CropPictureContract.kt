package com.qihuan.albumwidget

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
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