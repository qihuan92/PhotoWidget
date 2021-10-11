package com.qihuan.photowidget.main

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import com.qihuan.photowidget.R
import com.qihuan.photowidget.about.AboutActivity
import com.qihuan.photowidget.adapter.DefaultLoadStateAdapter
import com.qihuan.photowidget.adapter.TipAdapter
import com.qihuan.photowidget.adapter.WidgetPagingAdapter
import com.qihuan.photowidget.bean.TipsType
import com.qihuan.photowidget.config.ConfigureActivity
import com.qihuan.photowidget.databinding.ActivityMainBinding
import com.qihuan.photowidget.ktx.*

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
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            viewModel.refreshTipList()
        }

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

    @SuppressLint("BatteryLife")
    private fun bindView() {
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.about -> startActivity(Intent(this, AboutActivity::class.java))
            }
            true
        }
        val gridLayoutManager = GridLayoutManager(this, 2)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (adapter.getItemViewType(position)) {
                    TipsType.IGNORE_BATTERY_OPTIMIZATIONS.code -> 2
                    TipsType.ADD_WIDGET.code -> 2
                    else -> 1
                }
            }
        }
        binding.rvList.layoutManager = gridLayoutManager
        binding.rvList.adapter = adapter
        tipAdapter.setOnIgnoreTipClickListener {

        }

        tipAdapter.setOnPositiveButtonClickListener {
            when (it) {
                TipsType.IGNORE_BATTERY_OPTIMIZATIONS -> {
                    try {
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                        intent.data = Uri.parse("package:$packageName")
                        ignoringBatteryOptimizationsLauncher.launch(intent)
                    } catch (e: Exception) {
                        logE("MainActivity", "申请关闭电池优化异常", e)
                    }
                }
                else -> {
                }
            }
        }

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

        binding.refreshLayout.setOnRefreshListener {
            refresh()
        }
    }

    private fun bindData() {
        viewModel.widgetPagingData.observe(this) {
            binding.refreshLayout.isRefreshing = false
            widgetAdapter.submitData(lifecycle, it)
        }

        viewModel.tipList.observe(this) {
            tipAdapter.submitList(it)
        }
    }

    private fun refresh() {
        widgetAdapter.refresh()
    }
}