package com.qihuan.photowidget.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.qihuan.photowidget.R
import com.qihuan.photowidget.databinding.DialogItemSelectionBinding
import com.qihuan.photowidget.databinding.ItemDialogSelectionBinding
import com.qihuan.photowidget.ktx.viewBinding

/**
 * ItemSelectionDialog
 * @author qi
 * @since 2021/10/8
 */
class ItemSelectionDialog<T : ItemSelectionDialog.Item>(
    context: Context,
    private val title: String? = null,
    private val itemList: List<T>? = null,
    private val onItemClickListener: ((ItemSelectionDialog<T>, T) -> Unit)? = null
) : BottomSheetDialog(ContextThemeWrapper(context, R.style.ThemeOverlay_Rounded)) {

    private val binding by viewBinding(DialogItemSelectionBinding::inflate)
    private val adapter by lazy { Adapter(this, onItemClickListener) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        binding.rvList.adapter = adapter
        if (!title.isNullOrEmpty()) {
            binding.tvTitle.text = title
        }
        itemList?.let {
            adapter.submitList(it)
        }
    }

    fun setItemList(itemList: List<T>) {
        adapter.submitList(itemList)
    }

    class Adapter<T : Item>(
        private val dialog: ItemSelectionDialog<T>,
        private val onItemClickListener: ((ItemSelectionDialog<T>, T) -> Unit)? = null
    ) : ListAdapter<T, Adapter.ViewHolder<T>>(DiffCallback()) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<T> {
            val binding = ItemDialogSelectionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ViewHolder(binding, dialog, onItemClickListener)
        }

        override fun onBindViewHolder(holder: ViewHolder<T>, position: Int) {
            holder.bind(getItem(position))
        }

        class DiffCallback<T : Item> : DiffUtil.ItemCallback<T>() {
            override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
                return oldItem.getItemText() == newItem.getItemText()
            }
        }

        class ViewHolder<T : Item>(
            private val binding: ItemDialogSelectionBinding,
            dialog: ItemSelectionDialog<T>,
            onItemClickListener: ((ItemSelectionDialog<T>, T) -> Unit)? = null
        ) : RecyclerView.ViewHolder(binding.root) {

            private var currentItem: T? = null

            init {
                binding.root.setOnClickListener {
                    currentItem?.let {
                        onItemClickListener?.invoke(dialog, it)
                    }
                }
            }

            fun bind(item: T) {
                currentItem = item
                binding.tvItem.text = item.getItemText()
            }
        }
    }

    interface Item {
        fun getItemText(): String
    }
}