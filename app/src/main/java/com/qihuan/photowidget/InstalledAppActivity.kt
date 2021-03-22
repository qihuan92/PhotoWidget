package com.qihuan.photowidget

import android.content.pm.ApplicationInfo
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.qihuan.photowidget.adapter.InstalledAppAdapter
import com.qihuan.photowidget.bean.InstalledAppInfo
import com.qihuan.photowidget.databinding.ActivityInstalledAppBinding
import com.qihuan.photowidget.ktx.viewBinding

class InstalledAppActivity : AppCompatActivity() {
    private val binding by viewBinding(ActivityInstalledAppBinding::inflate)
    private val viewModel by viewModels<InstalledAppViewModel>()

    private val installedAppAdapter by lazy { InstalledAppAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.rvList.adapter = installedAppAdapter

        packageManager.getInstalledPackages(0)
            .filter {
                it.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0
            }
            .map {
                InstalledAppInfo(
                    it.applicationInfo.loadIcon(packageManager),
                    it.applicationInfo.loadLabel(packageManager).toString(),
                    it.packageName
                )
            }.let {
                installedAppAdapter.submitList(it)
            }
    }
}