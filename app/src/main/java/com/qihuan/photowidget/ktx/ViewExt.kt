package com.qihuan.photowidget.ktx

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.view.View
import com.hoko.blur.HokoBlur
import com.hoko.blur.task.AsyncBlurTask
import com.qihuan.photowidget.R

/**
 * ViewExt
 * @author qi
 * @since 12/7/20
 */
fun View.blurBackground(inputBackground: Bitmap, radius: Int = 20) {
    post {
        val srcBitmap = Bitmap.createBitmap(
            inputBackground.width,
            inputBackground.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(srcBitmap)
        canvas.drawBitmap(inputBackground, 0F, 0F, null)
        val canvas1 = Canvas(srcBitmap)
        //canvas1.drawColor(Color.argb(90, 255, 255, 255))
        canvas1.drawColor(context.getColor(R.color.blur_mask))

        HokoBlur.with(context)
            .translateY(inputBackground.height - height)
            .radius(radius)
            .asyncBlur(srcBitmap, object : AsyncBlurTask.Callback {
                override fun onBlurSuccess(bitmap: Bitmap?) {
                    background = BitmapDrawable(resources, bitmap)
                }

                override fun onBlurFailed(error: Throwable?) {

                }
            })
    }
}