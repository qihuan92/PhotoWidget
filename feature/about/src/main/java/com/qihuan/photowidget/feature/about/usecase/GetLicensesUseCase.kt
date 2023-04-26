package com.qihuan.photowidget.feature.about.usecase

import com.qihuan.photowidget.feature.about.module.License
import com.qihuan.photowidget.feature.about.module.LicenseItem

/**
 * 三方库协议
 *
 * @author Qi
 * @since 2022/10/3
 */
class GetLicensesUseCase {
    operator fun invoke() = listOf(
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
        LicenseItem(
            "ColorPickerView",
            "skydoves",
            License.APACHE_2,
            "https://github.com/skydoves/ColorPickerView"
        ),
        LicenseItem(
            "koin",
            "InsertKoinIO",
            License.APACHE_2,
            "https://github.com/InsertKoinIO/koin"
        ),
    )
}