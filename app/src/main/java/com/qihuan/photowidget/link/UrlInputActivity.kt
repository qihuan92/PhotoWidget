package com.qihuan.photowidget.link

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.qihuan.photowidget.R
import com.qihuan.photowidget.bean.LinkInfo
import com.qihuan.photowidget.databinding.ActivityUrlInputBinding
import com.qihuan.photowidget.ktx.paddingStatusBar
import com.qihuan.photowidget.ktx.viewBinding

class UrlInputActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivityUrlInputBinding::inflate)
    private var linkInfo: LinkInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(binding.root)
        binding.root.paddingStatusBar()

        linkInfo = intent.getParcelableExtra("linkInfo")
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
            binding.etOpenUrl.setText(linkInfo?.link)
            binding.etOpenUrl.requestFocus()
            WindowCompat.getInsetsController(window, binding.etOpenUrl)
                ?.show(WindowInsetsCompat.Type.ime())
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
            putExtra("linkInfo", LinkInfo.of(url))
        })
        finish()
    }
}