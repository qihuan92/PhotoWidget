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
import com.qihuan.photowidget.bean.CropPictureInfo
import com.yalantis.ucrop.UCrop
import com.yalantis.ucrop.UCropActivity


/**
 * CropPictureContract
 * @author qi
 * @since 11/20/20
 */
class CropPictureContract : ActivityResultContract<CropPictureInfo, Uri?>() {
    override fun createIntent(context: Context, input: CropPictureInfo): Intent {
        val intent = UCrop.of(input.inUri, input.outUri)
            .withOptions(UCrop.Options().apply {
                val value = TypedValue()
                context.theme.resolveAttribute(R.attr.colorSecondary, value, true)
                val mainColor = value.data

                setStatusBarColor(mainColor)
                setToolbarWidgetColor(Color.WHITE)
                setToolbarColor(mainColor)
                setActiveControlsWidgetColor(mainColor)
                setCompressionFormat(Bitmap.CompressFormat.PNG)
                setAllowedGestures(UCropActivity.SCALE, UCropActivity.ROTATE, UCropActivity.SCALE)
            })
            .getIntent(context)
        intent.setClass(context, CropActivity::class.java)
        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        if (resultCode == Activity.RESULT_OK && intent != null) {
            return UCrop.getOutput(intent)
        }
        return null
    }
}