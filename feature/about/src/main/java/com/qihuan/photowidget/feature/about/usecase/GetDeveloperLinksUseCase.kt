package com.qihuan.photowidget.feature.about.usecase

import android.content.Context
import com.qihuan.photowidget.feature.about.R
import com.qihuan.photowidget.feature.about.module.LinkItem

/**
 * 开发者链接
 *
 * @author Qi
 * @since 2022/10/3
 */
class GetDeveloperLinksUseCase(private val context: Context) {
    operator fun invoke() = listOf(
        LinkItem(
            context.getString(R.string.about_title_link_coolapk),
            context.getString(R.string.about_developer_coolapk_profile_url),
            R.color.coolapk,
            R.drawable.ic_coolapk
        ),
        LinkItem(
            context.getString(R.string.about_title_link_github),
            context.getString(R.string.about_developer_github),
            R.color.github,
            R.drawable.ic_github
        ),
        LinkItem(
            context.getString(R.string.about_title_link_weibo),
            context.getString(R.string.about_developer_weibo),
            R.color.weibo,
            R.drawable.ic_weibo
        ),
        LinkItem(
            context.getString(R.string.about_title_link_telegram),
            context.getString(R.string.about_developer_telegram),
            R.color.telegram,
            R.drawable.ic_telegram
        ),
    )
}