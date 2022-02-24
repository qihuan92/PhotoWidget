package com.qihuan.photowidget.ktx

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.gifdecoder.StandardGifDecoder
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.qihuan.photowidget.App
import com.qihuan.photowidget.common.CompressFormatCompat
import com.qihuan.photowidget.common.RadiusUnit
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
    Glide.with(context)
        .load(uri)
        .apply(RequestOptions.bitmapTransform(RoundedCorners(radius)))
        .into(this)
}

fun ImageView.load(uri: Uri) {
    Glide.with(context)
        .load(uri)
        .into(this)
}

fun View.loadToBackground(uri: Uri) {
    post {
        Glide.with(context)
            .load(uri)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(object : CustomTarget<Drawable>(width, height) {
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    background = resource
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    background = placeholder
                }
            })
    }
}

fun Uri.toRoundedBitmap(
    context: Context,
    radius: Float,
    radiusUnit: RadiusUnit,
    scaleType: ImageView.ScaleType,
    width: Int,
    height: Int
): Bitmap {
    var builder = Glide.with(context)
        .asBitmap()
        .load(this)

    val transformList = mutableListOf<Transformation<Bitmap>>()
    if (width > 0 && height > 0) {
        if (scaleType == ImageView.ScaleType.CENTER_CROP) {
            transformList.add(CenterCrop())
        }
    }
    if (radius > 0) {
        // Calculate radiusPx
        val radiusPx =
            if (scaleType == ImageView.ScaleType.CENTER_CROP && width > 0 && height > 0) {
                calculateRadiusPx(width, height, radius, radiusUnit)
            } else {
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(path, options)
                calculateRadiusPx(options.outWidth, options.outHeight, radius, radiusUnit)
            }
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

fun Uri.saveGifFramesToDir(dir: File, radius: Float = 0f, radiusUnit: RadiusUnit) {
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
            val roundedFrame = frame?.withRoundedCorner(radius, radiusUnit)
            roundedFrame?.saveFile(dir, index.toString())
        }
    } catch (e: Exception) {
        logE("ImageExt", "saveGifFramesToDir()", e)
        gifDrawable.firstFrame.saveFile(dir, "0")
    }
}

fun Bitmap.withRoundedCorner(radiusAngle: Float, radiusUnit: RadiusUnit): Bitmap {
    var builder = Glide.with(App.context)
        .asBitmap()
        .load(this)

    if (radiusAngle > 0) {
        val radiusPx = calculateRadiusPx(width, height, radiusAngle, radiusUnit)
        builder = builder.transform(RoundedCorners(radiusPx))
    }
    return builder.submit().get()
}

fun Bitmap.withRoundedCorner(radius: Int): Bitmap {
    var builder = Glide.with(App.context)
        .asBitmap()
        .load(this)

    if (radius > 0) {
        builder = builder.transform(RoundedCorners(radius))
    }
    return builder.submit().get()
}

fun Bitmap.saveFile(dir: File, displayName: String) {
    val file = File(dir, "$displayName.webp")
    val fos = FileOutputStream(file)
    val bos = ByteArrayOutputStream()

    try {
        if (compress(CompressFormatCompat.WEBP_LOSSY, 50, bos)) {
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