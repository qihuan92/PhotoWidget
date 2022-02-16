package com.qihuan.photowidget.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.qihuan.photowidget.R
import com.qihuan.photowidget.bean.WidgetFrameResource
import com.qihuan.photowidget.common.WidgetFrameType
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

            binding.ivIcon.isVisible = item.type != WidgetFrameType.BUILD_IN
            when (item.type) {
                WidgetFrameType.NONE -> {
                    binding.ivFrame.setImageResource(R.color.default_color_primary)
                    binding.ivIcon.setImageResource(R.drawable.ic_round_block_24)
                }
                WidgetFrameType.IMAGE -> {
                    binding.ivFrame.setImageResource(R.color.default_color_primary)
                    binding.ivIcon.setImageResource(R.drawable.ic_outline_image_24)
                }
                WidgetFrameType.COLOR -> {
                    binding.ivFrame.setImageResource(R.drawable.frame_hsv_palette)
                    binding.ivIcon.setImageResource(R.drawable.ic_round_color_lens_24)
                }
                WidgetFrameType.THEME_COLOR -> {
                    binding.ivFrame.setImageResource(R.color.default_color_primary)
                    binding.ivIcon.setImageResource(android.R.color.transparent)
                }
                WidgetFrameType.BUILD_IN -> {
                    item.frameUri?.let {
                        binding.ivFrame.load(it)
                    }
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