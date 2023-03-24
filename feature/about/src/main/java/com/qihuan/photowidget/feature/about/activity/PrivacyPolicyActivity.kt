package com.qihuan.photowidget.feature.about.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.qihuan.photowidget.core.common.ktx.viewBinding
import com.qihuan.photowidget.core.common.navigation.AboutNavigation
import com.qihuan.photowidget.feature.about.databinding.ActivityPrivacyPolicyBinding
import com.therouter.router.Route

/**
 * PrivacyPolicyActivity
 * @author qi
 * @since 2021/12/14
 */
@Route(path = AboutNavigation.PATH_PRIVACY)
class PrivacyPolicyActivity : AppCompatActivity() {
    private val binding by viewBinding(ActivityPrivacyPolicyBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.webView.loadUrl("file:///android_asset/privacy_policy.html")
    }
}