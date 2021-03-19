package com.qihuan.photowidget.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.qihuan.photowidget.bean.InstalledAppInfo
import com.qihuan.photowidget.databinding.ItemAppBinding

/**
 * InstalledAppAdapter
 * @author qi
 * @since 3/19/21
 */
class InstalledAppAdapter :
    ListAdapter<InstalledAppInfo, InstalledAppAdapter.ViewHolder>(DiffCallback()) {

    private class DiffCallback : DiffUtil.ItemCallback<InstalledAppInfo>() {
        override fun areItemsTheSame(
            oldItem: InstalledAppInfo,
            newItem: InstalledAppInfo
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: InstalledAppInfo,
            newItem: InstalledAppInfo
        ): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): InstalledAppAdapter.ViewHolder {
        return ViewHolder(
            ItemAppBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: InstalledAppAdapter.ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemAppBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: InstalledAppInfo) {
            binding.ivIcon.setImageDrawable(item.icon)
            binding.tvAppName.text = item.appName
            binding.tvPackageName.text = item.packageName
        }
    }
}