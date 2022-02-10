package com.qihuan.photowidget.about

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.view.WindowCompat
import com.google.android.material.chip.Chip
import com.qihuan.photowidget.BuildConfig
import com.qihuan.photowidget.R
import com.qihuan.photowidget.common.License
import com.qihuan.photowidget.databinding.ActivityAboutBinding
import com.qihuan.photowidget.databinding.ItemAboutLicenseBinding
import com.qihuan.photowidget.ktx.dp
import com.qihuan.photowidget.ktx.logE
import com.qihuan.photowidget.ktx.viewBinding

/**
 * AboutActivity
 * @author qi
 * @since 2021/10/14
 */
class AboutActivity : AppCompatActivity() {
    private val binding by viewBinding(ActivityAboutBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.privacy_policy -> startActivity(
                    Intent(
                        this,
                        PrivacyPolicyActivity::class.java
                    )
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
        binding.tvAppVersion.text = "${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE})"
    }

    private fun addAppLinks() {
        mutableListOf(
            LinkItem(
                getString(R.string.about_title_link_coolapk),
                getString(R.string.about_app_coolapk_app_url),
                R.color.coolapk,
                R.drawable.ic_coolapk
            ),
            LinkItem(
                getString(R.string.about_title_link_github),
                getString(R.string.about_open_source_url),
                R.color.github,
                R.drawable.ic_github
            ),
        ).forEach {
            binding.chipGroupApp.addView(createLinkChip(it))
        }
    }

    private fun addDeveloperLinks() {
        mutableListOf(
            LinkItem(
                getString(R.string.about_title_link_coolapk),
                getString(R.string.about_developer_coolapk_profile_url),
                R.color.coolapk,
                R.drawable.ic_coolapk
            ),
            LinkItem(
                getString(R.string.about_title_link_github),
                getString(R.string.about_developer_github),
                R.color.github,
                R.drawable.ic_github
            ),
            LinkItem(
                getString(R.string.about_title_link_weibo),
                getString(R.string.about_developer_weibo),
                R.color.weibo,
                R.drawable.ic_weibo
            ),
            LinkItem(
                getString(R.string.about_title_link_telegram),
                getString(R.string.about_developer_telegram),
                R.color.telegram,
                R.drawable.ic_telegram
            ),
        ).forEach {
            binding.chipGroupDeveloper.addView(createLinkChip(it))
        }
    }

    private fun addLicenses() {
        mutableListOf(
            LicenseItem(
                "kotlin",
                "JetBrains",
                License.APACHE_2,
                "https://github.com/JetBrains/kotlin"
            ),
            LicenseItem(
                "AndroidX",
                "Google",
                License.APACHE_2,
                "https://source.google.com"
            ),
            LicenseItem(
                "Android Jetpack",
                "Google",
                License.APACHE_2,
                "https://source.google.com"
            ),
            LicenseItem(
                "material-components-android",
                "Google",
                License.APACHE_2,
                "https://github.com/material-components/material-components-android"
            ),
            LicenseItem(
                "uCrop",
                "Yalantis",
                License.APACHE_2,
                "https://github.com/Yalantis/uCrop"
            ),
            LicenseItem(
                "Compressor",
                "zetbaitsu",
                License.APACHE_2,
                "https://github.com/zetbaitsu/Compressor"
            ),
            LicenseItem(
                "glide",
                "bumptech",
                License.APACHE_2,
                "https://github.com/bumptech/glide"
            ),
        ).forEach {
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

    private data class LinkItem(
        val name: String,
        val link: String,
        @ColorRes val color: Int,
        @DrawableRes val icon: Int,
    )

    private data class LicenseItem(
        val name: String,
        val author: String,
        val type: String,
        val url: String,
    )
}