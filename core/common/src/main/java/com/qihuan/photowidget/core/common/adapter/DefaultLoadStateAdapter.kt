package com.qihuan.photowidget.core.common.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.qihuan.photowidget.core.common.R
import com.qihuan.photowidget.core.common.databinding.ItemLoadErrorBinding

class DefaultLoadStateAdapter(
    private val adapter: PagingDataAdapter<out Any, out RecyclerView.ViewHolder>
) : LoadStateAdapter<DefaultLoadStateAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder, loadState: LoadState) {
        holder.bindTo(loadState)
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): ViewHolder {
        return ViewHolder(parent) { adapter.retry() }
    }

    class ViewHolder(
        parent: ViewGroup,
        private val retryCallback: () -> Unit
    ) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_load_error, parent, false)
    ) {
        private val binding = ItemLoadErrorBinding.bind(itemView)

        init {
            binding.retryButton.setOnClickListener {
                retryCallback()
            }
        }

        fun bindTo(loadState: LoadState) {
            with(binding) {
                progressBar.isVisible = loadState is LoadState.Loading
                retryButton.isVisible = loadState is LoadState.Error
                errorMsg.isVisible =
                    !(loadState as? LoadState.Error)?.error?.message.isNullOrBlank()
                errorMsg.text = (loadState as? LoadState.Error)?.error?.message
            }
        }
    }
}