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

        fun bind(item: Uri) {
            binding.ivPicture.setImageURI(item)
            binding.btnDelete.setOnClickListener {
                onItemDeleteListener?.invoke(item)
            }
        }
    }

    private var onItemDeleteListener: ((Uri) -> Unit)? = null

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

    fun setOnItemDeleteListener(onItemDeleteListener: ((Uri) -> Unit)) {
        this.onItemDeleteListener = onItemDeleteListener
    }
}