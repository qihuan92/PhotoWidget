package com.qihuan.photowidget.about

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.qihuan.photowidget.databinding.ActivityPrivacyPolicyBinding
import com.qihuan.photowidget.core.common.ktx.viewBinding

/**
 * PrivacyPolicyActivity
 * @author qi
 * @since 2021/12/14
 */
class PrivacyPolicyActivity: AppCompatActivity() {
    private val binding by viewBinding(ActivityPrivacyPolicyBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        binding.webView.loadUrl("file:///android_asset/privacy_policy.html")
    }
}