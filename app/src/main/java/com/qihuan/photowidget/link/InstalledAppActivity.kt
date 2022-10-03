package com.qihuan.photowidget.link

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.qihuan.photowidget.R
import com.qihuan.photowidget.adapter.InstalledAppAdapter
import com.qihuan.photowidget.core.database.model.LinkInfo
import com.qihuan.photowidget.core.model.LinkType
import com.qihuan.photowidget.databinding.ActivityInstalledAppBinding
import com.qihuan.photowidget.core.common.ktx.paddingNavigationBar
import com.qihuan.photowidget.core.common.ktx.viewBinding

class InstalledAppActivity : AppCompatActivity() {
    enum class UIState {
        LOADING, SHOW_CONTENT
    }

    private val binding by viewBinding(ActivityInstalledAppBinding::inflate)
    private val viewModel by viewModels<InstalledAppViewModel>()
    private val widgetId by lazy {
        intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
    }

    private val installedAppAdapter by lazy { InstalledAppAdapter() }
    private var spanCount: Int = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(binding.root)
        binding.viewModel = viewModel

        binding.rvList.paddingNavigationBar()

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            spanCount = 5
        } else if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            spanCount = 1
        }

        bindView()
        bindData()
    }

    private fun bindView() {
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        binding.toolbar.inflateMenu(R.menu.menu_installed_app)
        binding.toolbar.findViewById<SearchView>(R.id.search).apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    viewModel.queryKeyWord.value = query ?: ""
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    viewModel.queryKeyWord.value = newText ?: ""
                    return true
                }
            })
        }
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.show_system_apps -> {
                    val curr = viewModel.showSystemApps.value ?: true
                    viewModel.showSystemApps.value = !curr
                    it.isChecked = !curr
                }
                else -> {
                }
            }
            true
        }

        binding.rvList.layoutManager = GridLayoutManager(this, spanCount)
        binding.rvList.adapter = installedAppAdapter
        installedAppAdapter.setOnItemClickListener { position, _ ->
            viewModel.installedAppList.value?.get(position)?.apply {
                val intent = Intent().apply {
                    putExtra(
                        "linkInfo",
                        LinkInfo(
                            widgetId,
                            LinkType.OPEN_APP,
                            "打开应用: [ $appName ]",
                            "包名: $packageName",
                            packageName
                        )
                    )
                }
                setResult(RESULT_OK, intent)
                finish()
            }
        }
    }

    private fun bindData() {
        viewModel.installedAppList.observe(this) {
            installedAppAdapter.submitList(it)
        }
        viewModel.showSystemApps.observe(this) {
            viewModel.loadInstalledApp()
        }
    }
}