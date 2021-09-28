package com.qihuan.photowidget.about

import android.widget.ImageView
import android.widget.TextView
import com.drakeet.about.*
import com.qihuan.photowidget.BuildConfig
import com.qihuan.photowidget.R


/**
 * AboutActivity
 * @author qi
 * @since 2021/9/28
 */
class AboutActivity : AbsAboutActivity() {

    override fun onCreateHeader(icon: ImageView, slogan: TextView, version: TextView) {
        icon.setImageResource(R.mipmap.ic_launcher)
        slogan.text = getString(R.string.app_name)
        version.text = String.format(getString(R.string.about_version), BuildConfig.VERSION_NAME)
    }

    override fun onItemsCreated(items: MutableList<Any>) {
        addIntroduction(items)
        addDeveloper(items)
        addOpenSourceUrl(items)
        addLicenses(items)
    }

    private fun addOpenSourceUrl(items: MutableList<Any>) {
        items.add(Category(getString(R.string.about_title_open_source_url)))
        items.add(Card(getString(R.string.about_open_source_url)))
    }

    private fun addLicenses(items: MutableList<Any>) {
        items.add(Category(getString(R.string.about_title_open_source_licenses)))
        items.add(
            License(
                "kotlin",
                "JetBrains",
                License.APACHE_2,
                "https://github.com/JetBrains/kotlin"
            )
        )
        items.add(
            License(
                "AndroidX",
                "Google",
                License.APACHE_2,
                "https://source.google.com"
            )
        )
        items.add(
            License(
                "Android Jetpack",
                "Google",
                License.APACHE_2,
                "https://source.google.com"
            )
        )
        items.add(
            License(
                "material-components-android",
                "Google",
                License.APACHE_2,
                "https://github.com/material-components/material-components-android"
            )
        )
        items.add(
            License(
                "uCrop",
                "Yalantis",
                License.APACHE_2,
                "https://github.com/Yalantis/uCrop"
            )
        )
        items.add(
            License(
                "Compressor",
                "zetbaitsu",
                License.APACHE_2,
                "https://github.com/zetbaitsu/Compressor"
            )
        )
        items.add(
            License(
                "glide",
                "bumptech",
                License.APACHE_2,
                "https://github.com/bumptech/glide"
            )
        )
        items.add(
            License(
                "MultiType",
                "drakeet",
                License.APACHE_2,
                "https://github.com/drakeet/MultiType"
            )
        )
        items.add(
            License(
                "about-page",
                "drakeet",
                License.APACHE_2,
                "https://github.com/drakeet/about-page"
            )
        )
    }

    private fun addDeveloper(items: MutableList<Any>) {
        items.add(Category(getString(R.string.about_title_developer)))
        items.add(
            Contributor(
                R.drawable.ic_developer_avatar,
                getString(R.string.about_developer_name),
                getString(R.string.about_developer_github),
                getString(R.string.about_developer_coolapk_profile_url)
            )
        )
    }

    private fun addIntroduction(items: MutableList<Any>) {
        items.add(Category(getString(R.string.about_title_introduction)))
        items.add(Card(getString(R.string.about_introduction)))
    }
}