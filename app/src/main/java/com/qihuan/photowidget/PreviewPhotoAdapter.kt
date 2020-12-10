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
    private var onItemAddListener: (() -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_HEADER) {
            val headerViewHolder = HeaderViewHolder(
                ItemPreviewPhotoAddBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            headerViewHolder.itemView.setOnClickListener {
                onItemAddListener?.invoke()
            }
            return headerViewHolder
        } else {
            return ViewHolder(
                ItemPreviewPhotoBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ),
                onItemDeleteListener
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder) {
            val realPosition = position - 1
            holder.bind(getItem(realPosition), realPosition)
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

    fun setOnItemDeleteListener(onItemDeleteListener: ((Int, Uri) -> Unit)) {
        this.onItemDeleteListener = onItemDeleteListener
    }

    fun setOnItemAddListener(onItemAddListener: (() -> Unit)?) {
        this.onItemAddListener = onItemAddListener
    }
}