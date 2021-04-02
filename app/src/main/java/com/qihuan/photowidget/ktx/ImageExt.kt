package com.qihuan.photowidget.ktx

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

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

fun Uri.getRoundedBitmap(context: Context, radius: Int, width: Int, height: Int): Bitmap {
    val radiusPx = radius * 2
    val builder = Glide.with(context)
        .asBitmap()
        .load(this)
    if (width == 0 || height == 0) {
        return builder.transform(RoundedCorners(radiusPx)).submit().get()
    }
    return builder
        .transform(CenterCrop(), RoundedCorners(radiusPx))
        .submit(width, height)
        .get()
}