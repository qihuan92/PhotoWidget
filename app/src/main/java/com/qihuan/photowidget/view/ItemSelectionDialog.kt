package com.qihuan.photowidget.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.qihuan.photowidget.R
import com.qihuan.photowidget.bean.SelectionItem
import com.qihuan.photowidget.databinding.DialogItemSelectionBinding
import com.qihuan.photowidget.databinding.ItemDialogSelectionBinding
import com.qihuan.photowidget.ktx.performHapticFeedback
import com.qihuan.photowidget.ktx.viewBinding

/**
 * ItemSelectionDialog
 * @author qi
 * @since 2021/10/8
 */
class ItemSelectionDialog<T : SelectionItem>(
    context: Context,
    private val title: String? = null,
    private val itemList: List<T>? = null,
    private val onItemClickListener: ((ItemSelectionDialog<T>, T) -> Unit)? = null
) : BottomSheetDialog(context) {

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

    @Suppress("unused")
    fun setItemList(itemList: List<T>) {
        adapter.submitList(itemList)
    }

    override fun show() {
        super.show()
        val view = findViewById<View>(R.id.design_bottom_sheet)
        view?.post {
            val behavior = BottomSheetBehavior.from(view)
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED)
        }
    }

    class Adapter<T : SelectionItem>(
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

        class DiffCallback<T : SelectionItem> : DiffUtil.ItemCallback<T>() {
            override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
                return oldItem.text == newItem.text
            }
        }

        class ViewHolder<T : SelectionItem>(
            private val binding: ItemDialogSelectionBinding,
            dialog: ItemSelectionDialog<T>,
            onItemClickListener: ((ItemSelectionDialog<T>, T) -> Unit)? = null
        ) : RecyclerView.ViewHolder(binding.root) {

            private var currentItem: T? = null

            init {
                binding.root.setOnClickListener { view ->
                    currentItem?.let {
                        view.performHapticFeedback()
                        onItemClickListener?.invoke(dialog, it)
                    }
                }
            }

            fun bind(item: T) {
                currentItem = item

                val icon = item.icon
                binding.ivIcon.isVisible = icon != null
                if (icon != null) {
                    binding.ivIcon.setImageResource(icon)
                }
                binding.tvItem.setText(item.text)
            }
        }
    }
}