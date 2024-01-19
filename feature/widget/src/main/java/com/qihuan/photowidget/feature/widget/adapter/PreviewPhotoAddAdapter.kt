package com.qihuan.photowidget.feature.widget.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.qihuan.photowidget.feature.widget.databinding.ItemPreviewPhotoAddBinding

/**
 * PreviewPhotoAddAdapter
 * @author qi
 * @since 12/9/20
 */
class PreviewPhotoAddAdapter : ListAdapter<Any, RecyclerView.ViewHolder>(DiffCallback()) {

    private class DiffCallback : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            return true
        }
    }

    class ViewHolder(
        binding: ItemPreviewPhotoAddBinding
    ) : RecyclerView.ViewHolder(binding.root)

    private var onItemAddListener: (() -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val headerViewHolder = ViewHolder(
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
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

    }

    fun setOnItemAddListener(onItemAddListener: (() -> Unit)?) {
        this.onItemAddListener = onItemAddListener
    }
}