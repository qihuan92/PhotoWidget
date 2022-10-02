package com.qihuan.photowidget.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.qihuan.photowidget.R
import com.qihuan.photowidget.bean.WidgetBean
import com.qihuan.photowidget.core.model.WidgetFrameType
import com.qihuan.photowidget.core.model.WidgetType
import com.qihuan.photowidget.databinding.ItemWidgetInfoBinding
import com.qihuan.photowidget.ktx.dp
import com.qihuan.photowidget.ktx.load
import com.qihuan.photowidget.ktx.loadToBackground

/**
 * WidgetPagingAdapter
 * @author qi
 * @since 3/29/21
 */
class WidgetPagingAdapter :
    PagingDataAdapter<WidgetBean, WidgetPagingAdapter.ViewHolder>(DiffCallback()) {

    private class DiffCallback : DiffUtil.ItemCallback<WidgetBean>() {
        override fun areItemsTheSame(oldItem: WidgetBean, newItem: WidgetBean): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: WidgetBean, newItem: WidgetBean): Boolean {
            return oldItem == newItem
        }
    }

    inner class ViewHolder(
        private val binding: ItemWidgetInfoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                onItemClickListener?.invoke(bindingAdapterPosition, it)
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(item: WidgetBean?) {
            if (item != null) {
                val imageList = item.imageList
                val widgetInfo = item.widgetInfo
                val frame = item.frame
                if (imageList.isNotEmpty()) {
                    binding.ivWidgetPicture.load(imageList.first().imageUri)
                } else {
                    binding.ivWidgetPicture.setImageResource(R.drawable.ic_round_broken_image_24)
                }
                binding.tvTitle.text = "ID: ${widgetInfo.widgetId}"
                binding.ivGifTag.isVisible = item.widgetInfo.widgetType == WidgetType.GIF

                if (frame != null) {
                    binding.layoutPhotoContainer.setPadding(10f.dp)
                    if (frame.type == WidgetFrameType.BUILD_IN || frame.type == WidgetFrameType.IMAGE) {
                        val frameUri = frame.frameUri
                        if (frameUri != null) {
                            binding.layoutPhotoContainer.loadToBackground(frameUri)
                        } else {
                            binding.layoutPhotoContainer.setBackgroundResource(R.color.card_background_color)
                        }
                    } else if (frame.type == WidgetFrameType.COLOR) {
                        val frameColor = frame.frameColor
                        if (frameColor != null) {
                            val frameColorInt = Color.parseColor(frameColor)
                            binding.layoutPhotoContainer.setBackgroundColor(frameColorInt)
                        } else {
                            binding.layoutPhotoContainer.setBackgroundResource(R.color.card_background_color)
                        }
                    } else {
                        binding.layoutPhotoContainer.setBackgroundResource(R.color.card_background_color)
                    }
                } else {
                    binding.layoutPhotoContainer.setPadding(0)
                }
            }
        }
    }

    private var onItemClickListener: ((Int, View) -> Unit)? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemWidgetInfoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    fun setOnItemClickListener(listener: ((Int, View) -> Unit)) {
        this.onItemClickListener = listener
    }
}