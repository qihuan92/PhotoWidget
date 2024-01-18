package com.qihuan.photowidget.feature.about.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.view.WindowCompat
import com.google.android.material.chip.Chip
import com.qihuan.photowidget.core.common.AppInfo
import com.qihuan.photowidget.core.common.ktx.dp
import com.qihuan.photowidget.core.common.ktx.logE
import com.qihuan.photowidget.core.common.ktx.viewBinding
import com.qihuan.photowidget.feature.about.R
import com.qihuan.photowidget.feature.about.databinding.ActivityAboutBinding
import com.qihuan.photowidget.feature.about.databinding.ItemAboutLicenseBinding
import com.qihuan.photowidget.feature.about.model.LicenseItem
import com.qihuan.photowidget.feature.about.model.LinkItem
import com.qihuan.photowidget.feature.about.usecase.GetAppLinksUseCase
import com.qihuan.photowidget.feature.about.usecase.GetDeveloperLinksUseCase
import com.qihuan.photowidget.feature.about.usecase.GetLicensesUseCase
import org.koin.android.ext.android.inject
import org.koin.androidx.scope.ScopeActivity

/**
 * AboutActivity
 * @author qi
 * @since 2021/10/14
 */
class AboutActivity : ScopeActivity() {
    private val binding by viewBinding(ActivityAboutBinding::inflate)
    private val appInfo: AppInfo by inject()
    private val getAppLinksUseCase: GetAppLinksUseCase by inject()
    private val getDeveloperLinksUseCase: GetDeveloperLinksUseCase by inject()
    private val getLicensesUseCase: GetLicensesUseCase by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.privacy_policy -> startActivity(
                    Intent(this, PrivacyPolicyActivity::class.java)
                )
            }
            true
        }

        setAppVersion()
        addAppLinks()
        addDeveloperLinks()
        addLicenses()
    }

    @SuppressLint("SetTextI18n")
    private fun setAppVersion() {
        binding.tvAppVersion.text = "${appInfo.versionName}(${appInfo.versionCode})"
    }

    private fun addAppLinks() {
        getAppLinksUseCase().forEach {
            binding.chipGroupApp.addView(createLinkChip(it))
        }
    }

    private fun addDeveloperLinks() {
        getDeveloperLinksUseCase().forEach {
            binding.chipGroupDeveloper.addView(createLinkChip(it))
        }
    }

    private fun addLicenses() {
        getLicensesUseCase().forEach {
            binding.llLicenses.addView(createLicenseView(it))
        }
    }

    private fun createLinkChip(data: LinkItem) =
        Chip(ContextThemeWrapper(this, R.style.Chip_About)).apply {
            val context = this@AboutActivity
            chipIcon = AppCompatResources.getDrawable(context, data.icon)
            chipIconTint = AppCompatResources.getColorStateList(context, data.color)
            chipStrokeColor = AppCompatResources.getColorStateList(context, data.color)
            chipStrokeWidth = 2f.dp.toFloat()
            text = data.name
            setOnClickListener { openLink(data.link) }
        }

    @SuppressLint("SetTextI18n")
    private fun createLicenseView(data: LicenseItem) = ItemAboutLicenseBinding.inflate(
        LayoutInflater.from(this), binding.llLicenses, false
    ).apply {
        tvTitle.text = "${data.name} - ${data.author}"
        tvContent.text = "${data.url}\n${data.type}"
        root.setOnClickListener { openLink(data.url) }
    }.root

    private fun openLink(url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        try {
            startActivity(intent)
        } catch (e: Exception) {
            logE("AboutActivity", "openLink() Exception url=$url", e)
        }
    }
}