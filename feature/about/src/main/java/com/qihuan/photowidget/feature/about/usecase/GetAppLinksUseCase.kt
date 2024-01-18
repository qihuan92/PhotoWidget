package com.qihuan.photowidget.feature.about.usecase

import android.content.Context
import com.qihuan.photowidget.feature.about.R
import com.qihuan.photowidget.feature.about.model.LinkItem

/**
 * 获取应用链接
 *
 * @author Qi
 * @since 2022/10/3
 */
class GetAppLinksUseCase(private val context: Context) {

    operator fun invoke(): List<LinkItem> = listOf(
        LinkItem(
            context.getString(R.string.about_title_link_coolapk),
            context.getString(R.string.about_app_coolapk_app_url),
            R.color.coolapk,
            R.drawable.ic_coolapk
        ),
        LinkItem(
            context.getString(R.string.about_title_link_github),
            context.getString(R.string.about_open_source_url),
            R.color.github,
            R.drawable.ic_github
        )
    )
}