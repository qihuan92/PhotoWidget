package com.qihuan.photowidget.link

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.*
import com.qihuan.photowidget.R
import com.qihuan.photowidget.bean.LinkInfo
import com.qihuan.photowidget.databinding.ActivityUrlInputBinding
import com.qihuan.photowidget.ktx.marginNavigationBarAndIme
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
        binding.btnConfirm.marginNavigationBarAndIme()

        linkInfo = intent.getParcelableExtra("linkInfo")
        bindView()
    }

    private fun bindView() {
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        binding.etOpenUrl.post {
            binding.etOpenUrl.setText(linkInfo?.link)
            binding.etOpenUrl.requestFocus()
            WindowCompat.getInsetsController(window, binding.etOpenUrl)
                ?.show(WindowInsetsCompat.Type.ime())
        }
        binding.btnConfirm.setOnClickListener {
            val url = binding.etOpenUrl.text.toString().trim()
            if (url.isBlank()) {
                Toast.makeText(this, R.string.warn_url_empty, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            setResult(RESULT_OK, Intent().apply {
                putExtra("linkInfo", LinkInfo.of(url))
            })
            finish()
        }
    }
}