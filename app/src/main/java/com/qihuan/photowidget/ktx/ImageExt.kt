package com.qihuan.photowidget.ktx

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions

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
        .load(uri)
        .transition(DrawableTransitionOptions.withCrossFade())
        .apply(RequestOptions.bitmapTransform(RoundedCorners(radiusPx)))
        .into(this)
}

fun ImageView.load(uri: Uri) {
    Glide.with(context)
        .load(uri)
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(this)
}

fun Uri.getRoundedBitmap(context: Context, radius: Int): Bitmap {
    val radiusPx = radius * 2
    return Glide.with(context)
        .asBitmap()
        .load(this)
        .apply(RequestOptions.bitmapTransform(RoundedCorners(radiusPx)))
        .submit()
        .get()
}