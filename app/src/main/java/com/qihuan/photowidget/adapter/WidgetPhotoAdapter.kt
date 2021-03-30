package com.qihuan.photowidget.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.annotation.Px
import com.qihuan.photowidget.databinding.LayoutWidgetImageBinding
import com.qihuan.photowidget.ktx.loadRounded

/**
 * WidgetPhotoAdapter
 * @author qi
 * @since 12/9/20
 */
class WidgetPhotoAdapter(
    private val context: Context
) : BaseAdapter() {

    private val itemList = mutableListOf<Uri>()
    private var radius: Int = 0

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
        var binding: LayoutWidgetImageBinding? = null
        if (convertView == null) {
            binding =
                LayoutWidgetImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        }
        if (binding == null) {
            return View(context)
        }
        binding.apply {
            val uri = itemList[position]
            ivPicture.loadRounded(uri, radius)
        }
        return binding.root
    }

    fun setData(itemList: List<Uri>, @Px radius: Int) {
        this.itemList.clear()
        this.itemList.addAll(itemList)
        this.radius = radius
        notifyDataSetChanged()
    }
}