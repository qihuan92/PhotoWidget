package com.qihuan.photowidget.main

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.databinding.ObservableBoolean
import androidx.paging.LoadState
import com.qihuan.photowidget.adapter.DefaultLoadStateAdapter
import com.qihuan.photowidget.adapter.WidgetPagingAdapter
import com.qihuan.photowidget.config.ConfigureActivity
import com.qihuan.photowidget.databinding.ActivityMainBinding
import com.qihuan.photowidget.ktx.*

class MainActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivityMainBinding::inflate)
    private val viewModel by viewModels<MainViewModel>()
    private val widgetAdapter by lazy { WidgetPagingAdapter() }
    val isEmpty by lazy { ObservableBoolean(true) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(binding.root)
        binding.activity = this
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        binding.root.paddingStatusBar()
        binding.rvList.paddingNavigationBar()

        bindView()
        bindData()
    }

    private fun bindView() {
        binding.rvList.adapter =
            widgetAdapter.withLoadStateFooter(DefaultLoadStateAdapter(widgetAdapter))
        widgetAdapter.setOnItemClickListener { position, _ ->
            val widgetId =
                widgetAdapter.peek(position)?.widgetInfo?.widgetId ?: return@setOnItemClickListener
            startActivity(
                Intent(this, ConfigureActivity::class.java).apply {
                    val extras = Bundle().apply {
                        putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                    }
                    putExtras(extras)
                }
            )
        }
        widgetAdapter.addLoadStateListener { loadState ->
            if (loadState.source.refresh is LoadState.NotLoading && loadState.append.endOfPaginationReached && widgetAdapter.itemCount < 1) {
                isEmpty.set(true)
            } else {
                isEmpty.set(false)
            }
        }

        binding.refreshLayout.setOnRefreshListener {
            widgetAdapter.refresh()
        }
    }

    private fun bindData() {
        viewModel.widgetPagingData.observe(this) {
            binding.refreshLayout.isRefreshing = false
            widgetAdapter.submitData(lifecycle, it)
        }
    }

    fun refresh() {
        widgetAdapter.refresh()
    }
}