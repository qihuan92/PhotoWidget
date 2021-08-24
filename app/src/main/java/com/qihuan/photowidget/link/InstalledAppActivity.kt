package com.qihuan.photowidget.link

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.WindowCompat
import com.qihuan.photowidget.R
import com.qihuan.photowidget.adapter.InstalledAppAdapter
import com.qihuan.photowidget.bean.LinkInfo
import com.qihuan.photowidget.bean.LinkType
import com.qihuan.photowidget.databinding.ActivityInstalledAppBinding
import com.qihuan.photowidget.ktx.paddingNavigationBar
import com.qihuan.photowidget.ktx.paddingStatusBar
import com.qihuan.photowidget.ktx.viewBinding

class InstalledAppActivity : AppCompatActivity() {
    enum class UIState {
        LOADING, SHOW_CONTENT
    }

    private val binding by viewBinding(ActivityInstalledAppBinding::inflate)
    private val viewModel by viewModels<InstalledAppViewModel>()

    private val installedAppAdapter by lazy { InstalledAppAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(binding.root)
        binding.viewModel = viewModel

        binding.root.paddingStatusBar()
        binding.rvList.paddingNavigationBar()

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

        binding.rvList.adapter = installedAppAdapter
        installedAppAdapter.setOnItemClickListener { position, _ ->
            viewModel.installedAppList.value?.get(position)?.apply {
                val intent = Intent().apply {
                    putExtra(
                        "linkInfo",
                        LinkInfo.of("${LinkType.OPEN_APP.value}/$appName/$packageName")
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