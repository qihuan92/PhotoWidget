package com.qihuan.photowidget.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.qihuan.photowidget.R
import com.qihuan.photowidget.bean.WidgetFrameResource
import com.qihuan.photowidget.core.model.WidgetFrameType
import com.qihuan.photowidget.databinding.ItemWidgetFramePreviewBinding
import com.qihuan.photowidget.ktx.load
import com.qihuan.photowidget.ktx.performHapticFeedback

/**
 * WidgetFrameResourceAdapter
 * @author qi
 * @since 2022/2/14
 */
class WidgetFrameResourceAdapter(private val onItemClickListener: ((WidgetFrameResource) -> Unit)?) :
    ListAdapter<WidgetFrameResource, WidgetFrameResourceAdapter.ViewHolder>(DiffCallback()) {

    private class DiffCallback : DiffUtil.ItemCallback<WidgetFrameResource>() {
        override fun areItemsTheSame(
            oldItem: WidgetFrameResource,
            newItem: WidgetFrameResource
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: WidgetFrameResource,
            newItem: WidgetFrameResource
        ): Boolean {
            return oldItem == newItem
        }
    }

    class ViewHolder(
        private val binding: ItemWidgetFramePreviewBinding,
        private val onItemClickListener: ((WidgetFrameResource) -> Unit)?
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentItem: WidgetFrameResource? = null

        init {
            binding.root.setOnClickListener { view ->
                currentItem?.let {
                    view.performHapticFeedback()
                    onItemClickListener?.invoke(it)
                }
            }
        }

        fun bind(item: WidgetFrameResource) {
            currentItem = item

            when (item.type) {
                WidgetFrameType.NONE -> {
                    binding.ivFrame.setImageResource(R.color.default_color_background)
                    binding.ivIcon.setBackgroundResource(android.R.color.transparent)
                    binding.ivIcon.setImageResource(R.drawable.ic_round_block_24)
                }
                WidgetFrameType.IMAGE -> {
                    binding.ivFrame.setImageResource(R.drawable.bg_photo_frame_preview)
                    binding.ivIcon.setBackgroundResource(R.drawable.bg_photo_frame_preview_icon)
                    binding.ivIcon.setImageResource(R.drawable.ic_outline_image_24)
                }
                WidgetFrameType.COLOR -> {
                    binding.ivFrame.setImageResource(R.drawable.frame_hsv_palette)
                    binding.ivIcon.setBackgroundResource(R.drawable.bg_photo_frame_preview_icon)
                    binding.ivIcon.setImageResource(R.drawable.ic_round_color_lens_24)
                }
                WidgetFrameType.THEME_COLOR -> {
                    binding.ivFrame.setImageResource(R.color.default_color_primary)
                    binding.ivIcon.setBackgroundResource(R.drawable.bg_photo_frame_preview_icon)
                    binding.ivIcon.setImageResource(R.drawable.ic_round_android_24_main_color)
                }
                WidgetFrameType.BUILD_IN -> {
                    item.frameUri?.let {
                        binding.ivFrame.load(it)
                    }
                    binding.ivIcon.setBackgroundResource(R.drawable.bg_photo_frame_preview_icon)
                    binding.ivIcon.setImageResource(android.R.color.transparent)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemWidgetFramePreviewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onItemClickListener,
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}