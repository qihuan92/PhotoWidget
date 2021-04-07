package com.qihuan.photowidget.ktx

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import com.qihuan.photowidget.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * BitmapExt
 * @author qi
 * @since 12/8/20
 */
suspend fun Bitmap.blur(
    context: Context,
    width: Int,
    height: Int,
    @IntRange(from = 0, to = 25) radius: Int = 20,
    translateX: Int = 0,
    translateY: Int = 0,
    sampleFactor: Float = 5.0f,
    @ColorInt color: Int = context.getColor(R.color.blur_mask)
): Bitmap {
    return withContext(Dispatchers.IO) {
        val srcBitmap = Bitmap.createBitmap(this@blur, translateX, translateY, width, height)
        val canvas = Canvas(srcBitmap)
        canvas.drawColor(color)

        val scaledBitmap = getScaledBitmap(srcBitmap, sampleFactor)

        val renderScript = RenderScript.create(context)
        val input = Allocation.createFromBitmap(renderScript, scaledBitmap)
        val output = Allocation.createFromBitmap(renderScript, Bitmap.createBitmap(scaledBitmap))
        try {
            //doStackBlur(renderScript, scaledBitmap, radius, input, output)
            doGaussianBlur(renderScript, radius, input, output)
            output.copyTo(scaledBitmap)
        } catch (e: Exception) {
            logE("Blur", "Blur error", e)
        } finally {
            input.destroy()
            output.destroy()
            renderScript.destroy()
        }

        getScaledBitmap(scaledBitmap, 1f / sampleFactor)
    }
}

private fun doGaussianBlur(
    renderScript: RenderScript,
    radius: Int,
    input: Allocation,
    output: Allocation
) {
    val scriptIntrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
    scriptIntrinsicBlur.setRadius(radius.toFloat())
    scriptIntrinsicBlur.setInput(input)
    scriptIntrinsicBlur.forEach(output)
}

private fun getScaledBitmap(bitmap: Bitmap, factor: Float): Bitmap {
    if (factor == 1.0f) {
        return bitmap
    }
    val width = bitmap.width
    val height = bitmap.height
    val ratio = 1f / factor
    val matrix = Matrix()
    matrix.postScale(ratio, ratio)
    return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true)
}