package com.qihuan.photowidget.link

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.*
import com.qihuan.photowidget.R
import com.qihuan.photowidget.bean.LinkInfo
import com.qihuan.photowidget.databinding.ActivityUrlInputBinding
import com.qihuan.photowidget.ktx.viewBinding

class UrlInputActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivityUrlInputBinding::inflate)
    private var linkInfo: LinkInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(binding.root)
        adaptBars()

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
            linkInfo?.link = url
            setResult(RESULT_OK, Intent().apply {
                putExtra("linkInfo", linkInfo)
            })
            finish()
        }
    }

    private fun adaptBars() {
        // status bar
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

        // confirm button
        val fabTopMarginBottom = binding.btnConfirm.marginBottom
        ViewCompat.setOnApplyWindowInsetsListener(binding.btnConfirm) { view, insets ->
            val navigationBarInserts = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            val imeInserts = insets.getInsets(WindowInsetsCompat.Type.ime())
            view.updateLayoutParams {
                (this as ViewGroup.MarginLayoutParams).setMargins(
                    leftMargin,
                    topMargin,
                    rightMargin,
                    fabTopMarginBottom + navigationBarInserts.bottom + imeInserts.bottom
                )
            }
            insets
        }

    }
}