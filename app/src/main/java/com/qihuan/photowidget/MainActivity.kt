package com.qihuan.photowidget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.databinding.ObservableBoolean
import androidx.paging.LoadState
import com.qihuan.photowidget.adapter.DefaultLoadStateAdapter
import com.qihuan.photowidget.adapter.WidgetPagingAdapter
import com.qihuan.photowidget.databinding.ActivityMainBinding
import com.qihuan.photowidget.ktx.adapterBarsColor
import com.qihuan.photowidget.ktx.viewBinding

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
        binding.viewModel = viewModel
        adaptBars()

        bindView()
        bindData()
    }

    private fun adaptBars() {
        adapterBarsColor(resources, window, binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val barInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updatePadding(top = barInsets.top)
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.rvList) { view, insets ->
            val navigationBarInserts = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            view.updatePadding(bottom = navigationBarInserts.bottom)
            insets
        }
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