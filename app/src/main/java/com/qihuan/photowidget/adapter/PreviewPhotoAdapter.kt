package com.qihuan.photowidget.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.qihuan.photowidget.databinding.ItemPreviewPhotoBinding

/**
 * PreviewPhotoAdapter
 * @author qi
 * @since 12/9/20
 */
class PreviewPhotoAdapter : ListAdapter<Uri, PreviewPhotoAdapter.ViewHolder>(DiffCallback()) {

    private class DiffCallback : DiffUtil.ItemCallback<Uri>() {
        override fun areItemsTheSame(oldItem: Uri, newItem: Uri): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Uri, newItem: Uri): Boolean {
            return oldItem == newItem
        }
    }

    inner class ViewHolder(
        private val binding: ItemPreviewPhotoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.btnDelete.setOnClickListener {
                onItemDeleteListener?.invoke(layoutPosition - 1, it)
            }
        }

        fun bind(item: Uri) {
            binding.btnDelete.isEnabled = true
            Glide.with(itemView.context)
                .load(item)
                .into(binding.ivPicture)
        }
    }

    private var onItemDeleteListener: ((Int, View) -> Unit)? = null

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
}