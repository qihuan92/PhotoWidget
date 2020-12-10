package com.qihuan.photowidget.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.qihuan.photowidget.databinding.ItemPreviewPhotoBinding

/**
 * PreviewPhotoAdapter
 * @author qi
 * @since 12/9/20
 */
class PreviewPhotoAdapter : ListAdapter<Uri, RecyclerView.ViewHolder>(DiffCallback()) {

    private class DiffCallback : DiffUtil.ItemCallback<Uri>() {
        override fun areItemsTheSame(oldItem: Uri, newItem: Uri): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Uri, newItem: Uri): Boolean {
            return oldItem == newItem
        }
    }

    class ViewHolder(
        private val binding: ItemPreviewPhotoBinding,
        private val onItemDeleteListener: ((Int, Uri) -> Unit)?
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Uri, position: Int) {
            binding.ivPicture.setImageURI(item)
            binding.btnDelete.setOnClickListener {
                onItemDeleteListener?.invoke(position, item)
            }
        }
    }

    private var onItemDeleteListener: ((Int, Uri) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(
            ItemPreviewPhotoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onItemDeleteListener
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            holder.bind(getItem(position), position)
        }
    }

    fun setOnItemDeleteListener(onItemDeleteListener: ((Int, Uri) -> Unit)) {
        this.onItemDeleteListener = onItemDeleteListener
    }
}