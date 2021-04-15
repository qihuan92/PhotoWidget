package com.qihuan.photowidget.adapter

import android.content.Context
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.BaseAdapter
import android.widget.ImageView
import com.qihuan.photowidget.ktx.load

/**
 * WidgetPhotoAdapter
 * @author qi
 * @since 12/9/20
 */
class WidgetPhotoAdapter(
    private val context: Context
) : BaseAdapter() {

    private val itemList = mutableListOf<Uri>()
    private var scaleType = ImageView.ScaleType.CENTER_CROP

    override fun getCount(): Int {
        return itemList.size
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        if (itemList.isNullOrEmpty()) {
            return View(context)
        }

        return ImageView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            scaleType = this@WidgetPhotoAdapter.scaleType
            load(itemList[position])
        }
    }

    fun setData(itemList: List<Uri>) {
        this.itemList.clear()
        this.itemList.addAll(itemList)
        notifyDataSetChanged()
    }

    fun setScaleType(scaleType: ImageView.ScaleType) {
        this.scaleType = scaleType
        notifyDataSetChanged()
    }
}