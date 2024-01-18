package com.qihuan.photowidget.feature.main.activity

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.paging.LoadState
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import com.qihuan.photowidget.core.common.JobManager
import com.qihuan.photowidget.core.common.adapter.DefaultLoadStateAdapter
import com.qihuan.photowidget.core.common.battery.IgnoringBatteryOptimizationsContract
import com.qihuan.photowidget.core.common.ktx.logE
import com.qihuan.photowidget.core.common.ktx.paddingNavigationBar
import com.qihuan.photowidget.core.common.ktx.performHapticFeedback
import com.qihuan.photowidget.core.common.ktx.viewBinding
import com.qihuan.photowidget.core.database.model.WidgetInfo
import com.qihuan.photowidget.core.model.TipsType
import com.qihuan.photowidget.core.model.WidgetType
import com.qihuan.photowidget.feature.main.R
import com.qihuan.photowidget.feature.main.adapter.TipAdapter
import com.qihuan.photowidget.feature.main.adapter.WidgetPagingAdapter
import com.qihuan.photowidget.feature.main.databinding.ActivityMainBinding
import com.qihuan.photowidget.feature.main.viewmodel.MainViewModel
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivityMainBinding::inflate)
    private val viewModel by viewModels<MainViewModel>()
    private val jobManager by inject<JobManager>()
    private val widgetAdapter by lazy { WidgetPagingAdapter() }
    private val tipAdapter by lazy { TipAdapter() }
    private val adapter by lazy {
        ConcatAdapter(ConcatAdapter.Config.Builder().setIsolateViewTypes(false).build()).apply {
            addAdapter(tipAdapter)
            addAdapter(widgetAdapter.withLoadStateFooter(DefaultLoadStateAdapter(widgetAdapter)))
        }
    }

    private val ignoringBatteryOptimizationsLauncher =
        registerForActivityResult(IgnoringBatteryOptimizationsContract()) {
            viewModel.loadIgnoreBatteryOptimizations()
        }

    private var spanCount: Int = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(binding.root)
        binding.activity = this
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        binding.rvList.paddingNavigationBar()

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            spanCount = 4
        } else if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            spanCount = 2
        }

        bindView()
        bindData()
    }

    override fun onStart() {
        super.onStart()
        viewModel.loadIgnoreBatteryOptimizations()
    }

    private fun bindView() {
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.force_refresh_widget -> {
                    binding.toolbar.performHapticFeedback()
                    forceRefreshWidget()
                }

                R.id.settings -> {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("qihuan://photowidget/settings/main")
                        )
                    )
                }
            }
            true
        }

        val gridLayoutManager = GridLayoutManager(this, spanCount)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (adapter.getItemViewType(position)) {
                    TipsType.IGNORE_BATTERY_OPTIMIZATIONS.code -> spanCount
                    TipsType.ADD_WIDGET.code -> spanCount
                    else -> 1
                }
            }
        }
        binding.rvList.layoutManager = gridLayoutManager
        binding.rvList.adapter = adapter

        tipAdapter.setOnPositiveButtonClickListener {
            when (it) {
                TipsType.IGNORE_BATTERY_OPTIMIZATIONS -> {
                    try {
                        ignoringBatteryOptimizationsLauncher.launch(packageName)
                    } catch (e: Exception) {
                        logE("MainActivity", "申请关闭电池优化异常", e)
                    }
                }

                else -> {
                }
            }
        }

        widgetAdapter.setOnItemClickListener { position, _ ->
            val item = widgetAdapter.peek(position)?.widgetInfo
            launchConfigActivity(item)
        }

        widgetAdapter.addLoadStateListener { loadState ->
            if (loadState.source.refresh is LoadState.NotLoading && loadState.append.endOfPaginationReached && widgetAdapter.itemCount < 1) {
                viewModel.loadAddWidgetTip(true)
            } else {
                viewModel.loadAddWidgetTip(false)
            }
        }
    }

    private fun launchConfigActivity(item: WidgetInfo?) {
        if (item == null) {
            return
        }

        val widgetId = item.widgetId
        when (item.widgetType) {
            WidgetType.NORMAL -> {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("qihuan://photowidget/config/normal")
                    ).apply {
                        val extras = Bundle().apply {
                            putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                        }
                        putExtras(extras)
                    }
                )
            }

            WidgetType.GIF -> {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("qihuan://photowidget/config/gif")
                    ).apply {
                        val extras = Bundle().apply {
                            putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                        }
                        putExtras(extras)
                    }
                )
            }
        }
    }

    private fun bindData() {
        viewModel.widgetPagingData.observe(this) {
            widgetAdapter.submitData(lifecycle, it)
        }

        viewModel.tipList.observe(this) {
            tipAdapter.submitList(it.toMutableList())
        }
    }

    private fun forceRefreshWidget() {
        jobManager.scheduleUpdateWidgetJob()
    }
}