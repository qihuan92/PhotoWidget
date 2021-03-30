package com.qihuan.photowidget

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.qihuan.photowidget.adapter.InstalledAppAdapter
import com.qihuan.photowidget.databinding.ActivityInstalledAppBinding
import com.qihuan.photowidget.ktx.parseLink
import com.qihuan.photowidget.ktx.viewBinding
import kotlinx.coroutines.FlowPreview

@FlowPreview
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
                        "openApp/$appName/$packageName".parseLink()
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