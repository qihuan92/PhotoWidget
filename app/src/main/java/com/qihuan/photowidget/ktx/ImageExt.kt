package com.qihuan.photowidget.ktx

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.gifdecoder.StandardGifDecoder
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.qihuan.photowidget.App
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * ImageExt
 * @author qi
 * @since 3/30/21
 */
fun ImageView.loadRounded(uri: Uri, radius: Int) {
    if (radius == 0) {
        load(uri)
        return
    }
    val radiusPx = radius * 2
    Glide.with(context)
        .asBitmap()
        .load(uri)
        .apply(RequestOptions.bitmapTransform(RoundedCorners(radiusPx)))
        .into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                setImageBitmap(resource)
            }

            override fun onLoadCleared(placeholder: Drawable?) {

            }
        })
}

fun ImageView.load(uri: Uri) {
    Glide.with(context)
        .load(uri)
        .into(this)
}

fun Uri.toRoundedBitmap(
    context: Context,
    radius: Int,
    scaleType: ImageView.ScaleType,
    width: Int,
    height: Int
): Bitmap {
    val radiusPx = radius * 2
    var builder = Glide.with(context)
        .asBitmap()
        .load(this)

    val transformList = mutableListOf<Transformation<Bitmap>>()
    if (width > 0 && height > 0) {
        if (scaleType == ImageView.ScaleType.CENTER_CROP) {
            transformList.add(CenterCrop())
        }
    }
    if (radiusPx > 0) {
        transformList.add(RoundedCorners(radiusPx))
    }

    if (transformList.isNotEmpty()) {
        builder = builder.transform(*transformList.toTypedArray())
    }

    if (scaleType != ImageView.ScaleType.CENTER_CROP || (width <= 0 || height <= 0)) {
        return builder.submit().get()
    }
    return builder.submit(width, height).get()
}

fun Uri.saveGifFramesToDir(dir: File) {
    dir.deleteRecursively()
    if (!dir.exists()) {
        dir.mkdirs()
    }

    if (!dir.isDirectory) {
        return
    }

    val gifDrawable = Glide.with(App.context)
        .asGif()
        .load(this)
        .submit()
        .get()

    try {
        val gifState = gifDrawable.constantState
        val frameLoader = gifState?.javaClass?.getDeclaredField("frameLoader")?.apply {
            isAccessible = true
        }
        val gifFrameLoader = frameLoader?.get(gifState)
        val gifDecoder = gifFrameLoader?.javaClass?.getDeclaredField("gifDecoder")?.apply {
            isAccessible = true
        }
        val standardGifDecoder = gifDecoder?.get(gifFrameLoader) as StandardGifDecoder
        for (index in 0..standardGifDecoder.frameCount) {
            standardGifDecoder.advance()
            val frame = standardGifDecoder.nextFrame
            frame?.saveFile(dir, index.toString())
        }
    } catch (e: Exception) {
        logE("ImageExt", "saveGifFramesToDir()", e)
        gifDrawable.firstFrame.saveFile(dir, "0")
    }
}

fun Bitmap.saveFile(dir: File, displayName: String) {
    val compressFormat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Bitmap.CompressFormat.WEBP_LOSSY
    } else {
        Bitmap.CompressFormat.WEBP
    }

    val file = File(dir, "$displayName.webp")
    val fos = FileOutputStream(file)
    val bos = ByteArrayOutputStream()

    try {
        if (compress(compressFormat, 50, bos)) {
            fos.write(bos.toByteArray())
        }
    } catch (e: Exception) {
        logE("ImageExt", "Bitmap.save()", e)
    } finally {
        bos.flush()
        bos.close()

        fos.flush()
        fos.close()
    }
}