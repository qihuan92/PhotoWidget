package com.qihuan.photowidget.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.qihuan.photowidget.core.database.model.WidgetImage
import com.qihuan.photowidget.databinding.ItemPreviewPhotoBinding
import com.qihuan.photowidget.ktx.load

/**
 * PreviewPhotoAdapter
 *
 * @author qi
 * @since 12/9/20
 */
class PreviewPhotoAdapter :
    ListAdapter<WidgetImage, PreviewPhotoAdapter.ViewHolder>(DiffCallback()) {

    private class DiffCallback : DiffUtil.ItemCallback<WidgetImage>() {
        override fun areItemsTheSame(oldItem: WidgetImage, newItem: WidgetImage): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: WidgetImage, newItem: WidgetImage): Boolean {
            return oldItem == newItem
        }
    }

    inner class ViewHolder(
        private val binding: ItemPreviewPhotoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                onItemClickListener?.invoke(bindingAdapterPosition, it)
            }
            binding.btnDelete.setOnClickListener {
                onItemDeleteListener?.invoke(bindingAdapterPosition, it)
            }
        }

        fun bind(item: WidgetImage) {
            binding.ivPicture.load(item.imageUri)
        }
    }

    private var onItemDeleteListener: ((Int, View) -> Unit)? = null
    private var onItemClickListener: ((Int, View) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemPreviewPhotoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setOnItemDeleteListener(onItemDeleteListener: ((Int, View) -> Unit)) {
        this.onItemDeleteListener = onItemDeleteListener
    }

    fun setOnItemClickListener(onItemClickListener: ((Int, View) -> Unit)) {
        this.onItemClickListener = onItemClickListener
    }
}