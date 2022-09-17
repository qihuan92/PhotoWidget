package com.qihuan.photowidget.link

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.qihuan.photowidget.R
import com.qihuan.photowidget.bean.LinkInfo
import com.qihuan.photowidget.common.LinkType
import com.qihuan.photowidget.databinding.ActivityUrlInputBinding
import com.qihuan.photowidget.ktx.viewBinding

class UrlInputActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivityUrlInputBinding::inflate)
    private val widgetId by lazy {
        intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
    }
    private val openUrl: String? by lazy { intent.getStringExtra("openUrl") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(binding.root)
        bindView()
    }

    private fun bindView() {
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.confirm -> confirm()
            }
            true
        }
        binding.etOpenUrl.post {
            binding.etOpenUrl.setText(openUrl)
            binding.etOpenUrl.requestFocus()
            WindowCompat.getInsetsController(window, binding.etOpenUrl)
                .show(WindowInsetsCompat.Type.ime())
        }
        binding.etOpenUrl.addTextChangedListener {
            if (!it.isNullOrEmpty()) {
                if (binding.tilUrl.error != null) {
                    binding.tilUrl.error = null
                }
            }
        }
    }

    private fun confirm() {
        val url = binding.etOpenUrl.text.toString().trim()
        if (url.isBlank()) {
            binding.tilUrl.error = getString(R.string.warn_url_empty)
            return
        }
        setResult(RESULT_OK, Intent().apply {
            putExtra("linkInfo", LinkInfo(widgetId, LinkType.OPEN_URL, "打开链接", "地址: $url", url))
        })
        finish()
    }
}