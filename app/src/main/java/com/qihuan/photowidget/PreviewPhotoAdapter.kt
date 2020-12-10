package com.qihuan.photowidget

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.qihuan.photowidget.databinding.ItemPreviewPhotoAddBinding
import com.qihuan.photowidget.databinding.ItemPreviewPhotoBinding

/**
 * PreviewPhotoAdapter
 * @author qi
 * @since 12/9/20
 */
class PreviewPhotoAdapter : ListAdapter<Uri, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        const val TYPE_ITEM = 0
        const val TYPE_HEADER = 1
    }

    private class DiffCallback : DiffUtil.ItemCallback<Uri>() {
        override fun areItemsTheSame(oldItem: Uri, newItem: Uri): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Uri, newItem: Uri): Boolean {
            return oldItem == newItem
        }
    }

    class HeaderViewHolder(
        binding: ItemPreviewPhotoAddBinding
    ) : RecyclerView.ViewHolder(binding.root)

    class ViewHolder(
        private val binding: ItemPreviewPhotoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Uri) {
            binding.ivPicture.setImageURI(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            HeaderViewHolder(
                ItemPreviewPhotoAddBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else {
            ViewHolder(
                ItemPreviewPhotoBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            holder.bind(getItem(position - 1))
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            TYPE_HEADER
        } else {
            TYPE_ITEM
        }
    }
}