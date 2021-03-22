package com.qihuan.photowidget

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.qihuan.photowidget.adapter.InstalledAppAdapter
import com.qihuan.photowidget.bean.LinkInfo
import com.qihuan.photowidget.bean.LinkType
import com.qihuan.photowidget.databinding.ActivityInstalledAppBinding
import com.qihuan.photowidget.ktx.viewBinding

class InstalledAppActivity : AppCompatActivity() {
    private val binding by viewBinding(ActivityInstalledAppBinding::inflate)
    private val viewModel by viewModels<InstalledAppViewModel>()

    private val installedAppAdapter by lazy { InstalledAppAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(binding.root)
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
        WindowCompat.getInsetsController(window, binding.root)?.apply {
            when (resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
                Configuration.UI_MODE_NIGHT_NO -> isAppearanceLightStatusBars = true
                Configuration.UI_MODE_NIGHT_YES -> isAppearanceLightStatusBars = false
            }

        }
    }

    private fun bindView() {
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        binding.rvList.adapter = installedAppAdapter
        installedAppAdapter.setOnItemListener { position, _ ->
            viewModel.installedAppList.value?.get(position)?.apply {
                val intent = Intent().apply {
                    putExtra(
                        "linkInfo",
                        LinkInfo(
                            LinkType.OPEN_APP,
                            "打开应用: [ $appName ]",
                            "包名: $packageName",
                            "openApp/$appName/${packageName}"
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
    }
}