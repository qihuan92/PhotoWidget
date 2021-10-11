package com.qihuan.photowidget.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.qihuan.photowidget.bean.TipsType
import com.qihuan.photowidget.databinding.ItemTipAddWidgetBinding
import com.qihuan.photowidget.databinding.ItemTipIgnoreBatteryOptimizationsBinding

/**
 * TipAdapter
 * @author qi
 * @since 2021/10/11
 */
class TipAdapter : ListAdapter<TipsType, RecyclerView.ViewHolder>(DiffCallback()) {

    private class DiffCallback : DiffUtil.ItemCallback<TipsType>() {
        override fun areItemsTheSame(oldItem: TipsType, newItem: TipsType): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: TipsType, newItem: TipsType): Boolean {
            return oldItem == newItem
        }
    }

    class AddWidgetViewHolder(
        binding: ItemTipAddWidgetBinding
    ) : RecyclerView.ViewHolder(binding.root)

    class IgnoreBatteryOptimizationsViewHolder(
        binding: ItemTipIgnoreBatteryOptimizationsBinding
    ) : RecyclerView.ViewHolder(binding.root)

    class EmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private var onIgnoreTipClickListener: ((TipsType) -> Unit)? = null
    private var onPositiveButtonClickListener: ((TipsType) -> Unit)? = null

    override fun getItemViewType(position: Int): Int {
        return getItem(position).code
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TipsType.IGNORE_BATTERY_OPTIMIZATIONS.code -> {
                val binding = ItemTipIgnoreBatteryOptimizationsBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                binding.btnIgnoreTip.setOnClickListener {
                    onIgnoreTipClickListener?.invoke(TipsType.IGNORE_BATTERY_OPTIMIZATIONS)
                }
                binding.btnIgnoreBatteryOptimizations.setOnClickListener {
                    onPositiveButtonClickListener?.invoke(TipsType.IGNORE_BATTERY_OPTIMIZATIONS)
                }
                IgnoreBatteryOptimizationsViewHolder(binding)
            }
            TipsType.ADD_WIDGET.code -> {
                AddWidgetViewHolder(
                    ItemTipAddWidgetBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            else -> EmptyViewHolder(View(parent.context))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            TipsType.IGNORE_BATTERY_OPTIMIZATIONS.code -> {
            }
            TipsType.ADD_WIDGET.code -> {
            }
        }
    }

    fun setOnIgnoreTipClickListener(listener: (TipsType) -> Unit) {
        onIgnoreTipClickListener = listener
    }

    fun setOnPositiveButtonClickListener(listener: (TipsType) -> Unit) {
        onPositiveButtonClickListener = listener
    }
}