package com.qihuan.photowidget.config

import android.Manifest
import android.animation.ObjectAnimator
import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.core.view.*
import androidx.lifecycle.lifecycleScope
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.ConcatAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.qihuan.photowidget.R
import com.qihuan.photowidget.adapter.PreviewPhotoAdapter
import com.qihuan.photowidget.adapter.PreviewPhotoAddAdapter
import com.qihuan.photowidget.adapter.WidgetPhotoAdapter
import com.qihuan.photowidget.bean.PhotoScaleType
import com.qihuan.photowidget.bean.PlayInterval
import com.qihuan.photowidget.common.TEMP_DIR_NAME
import com.qihuan.photowidget.crop.CropPictureContract
import com.qihuan.photowidget.databinding.ActivityConfigureBinding
import com.qihuan.photowidget.ktx.*
import com.qihuan.photowidget.link.InstalledAppActivity
import com.qihuan.photowidget.link.UrlInputActivity
import kotlinx.coroutines.launch
import java.io.File

/**
 * The configuration screen for the [com.qihuan.photowidget.PhotoWidgetProvider] AppWidget.
 */
class ConfigureActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivityConfigureBinding::inflate)
    private val viewModel by viewModels<ConfigureViewModel>()

    var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    private val processImageDialog by lazy {
        MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Crane)
            .setTitle(R.string.processing)
            .setCancelable(false)
            .setView(ProgressBar(this).apply {
                updatePadding(top = 10f.dp, bottom = 10f.dp)
            })
            .create()
    }

    private val saveImageDialog by lazy {
        MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Crane)
            .setTitle(R.string.saving)
            .setCancelable(false)
            .setView(ProgressBar(this).apply {
                updatePadding(top = 10f.dp, bottom = 10f.dp)
            })
            .create()
    }

    private val previewAdapter by lazy { PreviewPhotoAdapter() }
    private val previewAddAdapter by lazy {
        val previewPhotoAddAdapter = PreviewPhotoAddAdapter()
        previewPhotoAddAdapter.submitList(listOf(1))
        previewPhotoAddAdapter
    }
    private val widgetAdapter by lazy { WidgetPhotoAdapter(this) }

    private val selectPicForResult =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) {
            if (it.isNullOrEmpty()) {
                return@registerForActivityResult
            }
            if (it.size == 1) {
                cropPicForResult.launch(it[0])
            } else {
                addPhoto(*it.toTypedArray())
            }
        }

    private val cropPicForResult =
        registerForActivityResult(CropPictureContract()) {
            if (it != null) {
                addPhoto(it)
            }
        }

    private val externalStorageResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                val wallpaperManager = WallpaperManager.getInstance(this)
                val wallpaper = wallpaperManager.drawable.toBitmap()
                // 设置壁纸背景
                binding.ivWallpaper.setImageBitmap(wallpaper)
                // 状态栏文字颜色适配
                adaptStatusBarTextColor(wallpaper)
            }
        }

    private val appSelectResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                it.data?.apply {
                    viewModel.linkInfo.set(getParcelableExtra("linkInfo"))
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        adaptBars()
        setResult(RESULT_CANCELED)
        setContentView(binding.root)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.activity = this
        bindView()
        initView()
        handleIntent(intent)
    }

    private fun initView() {
        // 隐藏操作区
        binding.scrollViewInfo.post {
            binding.scrollViewInfo.translationY = binding.scrollViewInfo.height.toFloat()
        }
    }

    private fun bindView() {
        // 获取背景权限
        externalStorageResult.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

        binding.layoutPhotoWidget.vfPicture.adapter = widgetAdapter
        binding.rvPreviewList.adapter = ConcatAdapter(previewAddAdapter, previewAdapter)
        previewAdapter.setOnItemDeleteListener { position, view ->
            view.isEnabled = false
            viewModel.deleteImage(position)
        }
        previewAddAdapter.setOnItemAddListener {
            selectPicForResult.launch("image/*")
        }

        viewModel.imageUriList.observe(this) {
            previewAdapter.submitList(it.toList())
            binding.layoutPhotoWidget.vfPicture.adapter = widgetAdapter
            widgetAdapter.setData(it)

            if (it.size <= 1) {
                viewModel.autoPlayInterval.value = null
                binding.layoutAutoPlayInterval.isGone = true
            } else {
                binding.layoutAutoPlayInterval.isGone = false
            }

            binding.layoutPhotoWidgetPreview.strokeWidth = if (it.isEmpty()) 2f.dp else 0
        }

        viewModel.autoPlayInterval.observe(this) {
            val vfPicture = binding.layoutPhotoWidget.vfPicture
            if (it == null) {
                vfPicture.isAutoStart = false
                vfPicture.stopFlipping()

                binding.tvAutoPlayInterval.text = getString(R.string.auto_play_interval_empty)
            } else {
                vfPicture.isAutoStart = true
                vfPicture.flipInterval = it
                vfPicture.startFlipping()

                binding.tvAutoPlayInterval.text =
                    String.format(
                        getString(
                            R.string.auto_play_interval_content,
                            (it / 1000).toString()
                        )
                    )
            }
        }

        viewModel.photoScaleType.observe(this) {
            binding.tvPhotoScaleType.text = PhotoScaleType.getDescription(it)
            binding.layoutPhotoWidget.vfPicture.adapter = widgetAdapter
            widgetAdapter.setScaleType(it)
        }

        viewModel.uiState.observe(this) {
            if (it == ConfigureViewModel.UIState.SHOW_CONTENT) {
                ObjectAnimator.ofFloat(binding.scrollViewInfo, "translationY", 0f).apply {
                    duration =
                        resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
                    interpolator = DecelerateInterpolator()
                    start()
                }
            }
        }

        viewModel.message.observe(this) {
            if (it != null) {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        binding.layoutPhotoWidget.photoWidgetInfo.areaLeft.setOnClickListener {
            binding.layoutPhotoWidget.vfPicture.showPrevious()
        }
        binding.layoutPhotoWidget.photoWidgetInfo.areaRight.setOnClickListener {
            binding.layoutPhotoWidget.vfPicture.showNext()
        }
    }

    override fun finish() {
        super.finish()
        val tempDir = File(cacheDir, TEMP_DIR_NAME)
        tempDir.deleteDir()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val extras = intent?.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        viewModel.loadWidget(appWidgetId)
    }

    private fun adaptBars() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.scrollViewInfo) { view, insets ->
            val barInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            view.post {
                view.updatePadding(bottom = barInsets.bottom)
            }
            insets
        }

        val fabTopMarginBottom = binding.btnConfirm.marginBottom
        ViewCompat.setOnApplyWindowInsetsListener(binding.btnConfirm) { view, insets ->
            val navigationBarInserts = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            view.updateLayoutParams {
                (this as ViewGroup.MarginLayoutParams).setMargins(
                    leftMargin,
                    topMargin,
                    rightMargin,
                    fabTopMarginBottom + navigationBarInserts.bottom
                )
            }
            insets
        }
    }

    private fun adaptStatusBarTextColor(wallpaper: Bitmap) {
        val statusBarSize = 30F.dp
        val statusBarAreaBitmap =
            Bitmap.createBitmap(wallpaper, 0, 0, wallpaper.width, statusBarSize)
        Palette.from(statusBarAreaBitmap).generate {
            if (it != null) {
                val dominantColor = it.getDominantColor(Color.WHITE)
                WindowCompat.getInsetsController(window, binding.root)?.apply {
                    isAppearanceLightStatusBars = !dominantColor.isDark()
                }
            }
        }
    }

    private fun addPhoto(vararg uris: Uri) {
        if (uris.isNullOrEmpty()) {
            return
        }
        lifecycleScope.launch {
            processImageDialog.show()
            for (uri in uris) {
                val tempOutFile = if (uris.size == 1) {
                    uri.toFile()
                } else {
                    val outDir = File(cacheDir, TEMP_DIR_NAME)
                    if (!outDir.exists()) {
                        outDir.mkdirs()
                    }
                    File(outDir, "${System.currentTimeMillis()}.png").also { file ->
                        copyFile(uri, file.toUri())
                    }
                }
                try {
                    viewModel.addImage(compressImageFile(tempOutFile).toUri())
                } catch (e: NoSuchFileException) {
                    logE("ConfigureActivity", e.message, e)
                }
            }
            processImageDialog.dismiss()
        }
    }

    fun saveWidget() {
        lifecycleScope.launch {
            saveImageDialog.show()
            viewModel.saveWidget(appWidgetId)
            saveImageDialog.dismiss()

            setResult(RESULT_OK, Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            })
            finish()
        }
    }

    fun showIntervalSelector() {
        val itemList = PlayInterval.values()
        MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Crane)
            .setTitle(R.string.alert_title_interval)
            .setSingleChoiceItems(
                itemList.map { it.description }.toTypedArray(),
                itemList.indexOfFirst { it.interval == viewModel.autoPlayInterval.value }
            ) { dialog, i ->
                viewModel.autoPlayInterval.value = itemList[i].interval
                dialog.dismiss()
            }.show()
    }

    fun showLinkTypeSelector() {
        MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Crane)
            .setTitle(R.string.alert_title_link_type)
            .setItems(R.array.open_link_types) { dialog, i ->
                when (i) {
                    0 -> appSelectResult.launch(Intent(this, InstalledAppActivity::class.java))
                    1 -> appSelectResult.launch(Intent(this, UrlInputActivity::class.java).apply {
                        viewModel.linkInfo.get()?.let {
                            if (!it.link.isOpenAppLink()) {
                                putExtra("url", it.link)
                            }
                        }
                    })
                }
                dialog.dismiss()
            }.show()
    }

    fun showScaleTypeSelector() {
        val scaleTypeList = PhotoScaleType.values()
        MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Crane)
            .setTitle(R.string.alert_title_scale_type)
            .setSingleChoiceItems(
                scaleTypeList.map { it.description }.toTypedArray(),
                scaleTypeList.indexOfFirst { it.scaleType == viewModel.photoScaleType.value }
            ) { dialog, i ->
                viewModel.photoScaleType.value = scaleTypeList[i].scaleType
                dialog.dismiss()
            }.show()
    }
}