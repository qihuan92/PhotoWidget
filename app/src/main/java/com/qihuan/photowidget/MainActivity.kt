package com.qihuan.photowidget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.qihuan.photowidget.adapter.DefaultLoadStateAdapter
import com.qihuan.photowidget.adapter.WidgetPagingAdapter
import com.qihuan.photowidget.databinding.ActivityMainBinding
import com.qihuan.photowidget.ktx.viewBinding

class MainActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivityMainBinding::inflate)
    private val viewModel by viewModels<MainViewModel>()
    private val widgetAdapter by lazy { WidgetPagingAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(binding.root)
        binding.viewModel = viewModel
        adaptBars()

        bindView()
        bindData()
    }

    private fun adaptBars() {
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
        WindowCompat.getInsetsController(window, binding.root)?.apply {
            when (resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
                Configuration.UI_MODE_NIGHT_NO -> isAppearanceLightStatusBars = true
                Configuration.UI_MODE_NIGHT_YES -> isAppearanceLightStatusBars = false
            }
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
}