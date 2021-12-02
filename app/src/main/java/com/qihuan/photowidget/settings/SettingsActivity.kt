package com.qihuan.photowidget.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.qihuan.photowidget.R
import com.qihuan.photowidget.about.AboutActivity
import com.qihuan.photowidget.common.AutoRefreshInterval
import com.qihuan.photowidget.common.PhotoScaleType
import com.qihuan.photowidget.common.RadiusUnit
import com.qihuan.photowidget.databinding.ActivitySettingsBinding
import com.qihuan.photowidget.ktx.IgnoringBatteryOptimizationsContract
import com.qihuan.photowidget.ktx.logE
import com.qihuan.photowidget.ktx.viewBinding
import com.qihuan.photowidget.view.ItemSelectionDialog

/**
 * SettingsActivity
 * @author qi
 * @since 2021/11/8
 */
class SettingsActivity : AppCompatActivity() {
    private val binding by viewBinding(ActivitySettingsBinding::inflate)
    private val viewModel by viewModels<SettingsViewModel>()

    private val scaleTypeDialog by lazy(LazyThreadSafetyMode.NONE) {
        ItemSelectionDialog(
            this,
            getString(R.string.alert_title_scale_type),
            PhotoScaleType.values().toList()
        ) { dialog, item ->
            viewModel.updatePhotoScaleType(item)
            dialog.dismiss()
        }
    }

    private val radiusUnitDialog by lazy(LazyThreadSafetyMode.NONE) {
        ItemSelectionDialog(
            this,
            getString(R.string.alert_title_radius_unit),
            RadiusUnit.values().toList()
        ) { dialog, item ->
            viewModel.updateRadiusUnit(item)
            dialog.dismiss()
        }
    }

    private val ignoringBatteryOptimizationsLauncher =
        registerForActivityResult(IgnoringBatteryOptimizationsContract()) {
            viewModel.loadIgnoreBatteryOptimizations()
        }

    private val intervalDialog by lazy(LazyThreadSafetyMode.NONE) {
        ItemSelectionDialog(
            this,
            getString(R.string.alert_title_auto_refresh_interval),
            AutoRefreshInterval.values().toList()
        ) { dialog, item ->
            viewModel.updateAutoRefreshInterval(item)
            dialog.dismiss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(binding.root)

        binding.lifecycleOwner = this
        binding.activity = this
        binding.viewModel = viewModel

        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    fun switchAutoRefresh(view: View) {
        intervalDialog.show()
    }

    fun launchAboutActivity(view: View) {
        startActivity(Intent(this, AboutActivity::class.java))
    }

    fun ignoreBatteryOptimizations(view: View) {
        try {
            ignoringBatteryOptimizationsLauncher.launch(packageName)
        } catch (e: Exception) {
            logE("SettingsActivity", "申请关闭电池优化异常", e)
        }
    }

    fun showScaleTypeSelector(view: View) {
        scaleTypeDialog.show()
    }

    fun showDefaultRadiusUnitSelector(view: View) {
        radiusUnitDialog.show()
    }
}