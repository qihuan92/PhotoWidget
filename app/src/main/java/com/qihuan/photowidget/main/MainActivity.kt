package com.qihuan.photowidget.main

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.paging.LoadState
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.qihuan.photowidget.R
import com.qihuan.photowidget.adapter.DefaultLoadStateAdapter
import com.qihuan.photowidget.adapter.TipAdapter
import com.qihuan.photowidget.adapter.WidgetPagingAdapter
import com.qihuan.photowidget.bean.TipsType
import com.qihuan.photowidget.bean.WidgetInfo
import com.qihuan.photowidget.bean.WidgetType
import com.qihuan.photowidget.common.MAIN_PAGE_SPAN_COUNT
import com.qihuan.photowidget.common.WorkTags
import com.qihuan.photowidget.config.ConfigureActivity
import com.qihuan.photowidget.config.GifConfigureActivity
import com.qihuan.photowidget.databinding.ActivityMainBinding
import com.qihuan.photowidget.ktx.IgnoringBatteryOptimizationsContract
import com.qihuan.photowidget.ktx.logE
import com.qihuan.photowidget.ktx.paddingNavigationBar
import com.qihuan.photowidget.ktx.viewBinding
import com.qihuan.photowidget.settings.SettingsActivity
import com.qihuan.photowidget.worker.ForceUpdateWidgetWorker

class MainActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivityMainBinding::inflate)
    private val viewModel by viewModels<MainViewModel>()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(binding.root)
        binding.activity = this
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        binding.rvList.paddingNavigationBar()

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
                R.id.force_refresh_widget -> forceRefreshWidget()
                R.id.settings -> startActivity(Intent(this, SettingsActivity::class.java))
            }
            true
        }

        val gridLayoutManager = GridLayoutManager(this, MAIN_PAGE_SPAN_COUNT)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (adapter.getItemViewType(position)) {
                    TipsType.IGNORE_BATTERY_OPTIMIZATIONS.code -> MAIN_PAGE_SPAN_COUNT
                    TipsType.ADD_WIDGET.code -> MAIN_PAGE_SPAN_COUNT
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
                    Intent(this, ConfigureActivity::class.java).apply {
                        val extras = Bundle().apply {
                            putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                        }
                        putExtras(extras)
                    }
                )
            }
            WidgetType.GIF -> {
                startActivity(
                    Intent(this, GifConfigureActivity::class.java).apply {
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
        val workRequest = OneTimeWorkRequestBuilder<ForceUpdateWidgetWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()
        WorkManager.getInstance(applicationContext)
            .enqueueUniqueWork(
                WorkTags.ONE_TIME_REFRESH_WIDGET,
                ExistingWorkPolicy.KEEP,
                workRequest
            )
    }
}