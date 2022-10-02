package com.qihuan.photowidget.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.BaseAdapter
import android.widget.ImageView
import com.qihuan.photowidget.core.database.model.WidgetImage
import com.qihuan.photowidget.ktx.load

/**
 * WidgetPhotoAdapter
 * @author qi
 * @since 12/9/20
 */
class WidgetPhotoAdapter(
    private val context: Context
) : BaseAdapter() {

    private val itemList = mutableListOf<WidgetImage>()
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
        if (itemList.isEmpty()) {
            return View(context)
        }

        return ImageView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            scaleType = this@WidgetPhotoAdapter.scaleType
            load(itemList[position].imageUri)
        }
    }

    fun setData(itemList: List<WidgetImage>) {
        this.itemList.clear()
        this.itemList.addAll(itemList)
        notifyDataSetChanged()
    }

    fun setScaleType(scaleType: ImageView.ScaleType) {
        this.scaleType = scaleType
        notifyDataSetChanged()
    }
}