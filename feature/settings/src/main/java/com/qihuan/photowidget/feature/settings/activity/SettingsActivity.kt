package com.qihuan.photowidget.feature.settings.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.qihuan.photowidget.core.common.battery.IgnoringBatteryOptimizationsContract
import com.qihuan.photowidget.core.common.ktx.logE
import com.qihuan.photowidget.core.common.ktx.performHapticFeedback
import com.qihuan.photowidget.core.common.ktx.viewBinding
import com.qihuan.photowidget.core.common.navigation.AboutNavigation
import com.qihuan.photowidget.core.common.navigation.SettingsNavigation
import com.qihuan.photowidget.core.common.view.ItemSelectionDialog
import com.qihuan.photowidget.core.model.AutoRefreshInterval
import com.qihuan.photowidget.core.model.PhotoScaleType
import com.qihuan.photowidget.core.model.RadiusUnit
import com.qihuan.photowidget.feature.settings.R
import com.qihuan.photowidget.feature.settings.databinding.ActivitySettingsBinding
import com.qihuan.photowidget.feature.settings.viewmodel.SettingsViewModel
import com.therouter.TheRouter
import com.therouter.router.Route
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * SettingsActivity
 * @author qi
 * @since 2021/11/8
 */
@Route(path = SettingsNavigation.PATH)
class SettingsActivity : AppCompatActivity() {
    private val binding by viewBinding(ActivitySettingsBinding::inflate)
    private val viewModel by viewModel<SettingsViewModel>()

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

        binding.sliderDefaultRadius.addOnChangeListener { slider, _, fromUser ->
            if (fromUser) {
                slider.performHapticFeedback()
            }
        }
    }

    fun switchAutoRefresh() {
        intervalDialog.show()
    }

    fun launchAboutActivity() {
        TheRouter.build(AboutNavigation.PATH_ABOUT)
            .navigation(this)
    }

    fun ignoreBatteryOptimizations() {
        try {
            ignoringBatteryOptimizationsLauncher.launch(packageName)
        } catch (e: Exception) {
            logE("SettingsActivity", "申请关闭电池优化异常", e)
        }
    }

    fun showScaleTypeSelector() {
        scaleTypeDialog.show()
    }

    fun showDefaultRadiusUnitSelector() {
        radiusUnitDialog.show()
    }
}