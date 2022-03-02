package com.qihuan.photowidget.ktx

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.gifdecoder.StandardGifDecoder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.qihuan.photowidget.App
import com.qihuan.photowidget.GlideApp
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
fun ImageView.load(uri: Uri) {
    val request = GlideApp.with(context)
        .load(uri)
    request.thumbnail(request.clone().sizeMultiplier(0.01f))
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(this)
}

fun ImageView.load(drawable: Drawable) {
    GlideApp.with(context)
        .load(drawable)
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(this)
}

fun View.loadToBackground(uri: Uri) {
    post {
        GlideApp.with(context)
            .load(uri)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .transition(DrawableTransitionOptions.withCrossFade())
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