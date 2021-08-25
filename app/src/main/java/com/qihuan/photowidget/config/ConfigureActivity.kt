package com.qihuan.photowidget.config

import android.Manifest
import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

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
            }
        }

    private val appSelectResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                it.data?.apply {
                    viewModel.linkInfo.value = getParcelableExtra("linkInfo")
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setResult(RESULT_CANCELED)
        setContentView(binding.root)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.activity = this

        binding.root.paddingStatusBar()
        binding.scrollViewInfo.paddingNavigationBar()

        bindView()
        initView()
        handleIntent(intent)
    }

    private fun initView() {
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.confirm -> saveWidget()
            }
            true
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
                viewModel.autoPlayInterval.value = PlayInterval.NONE
            }
        }

        viewModel.autoPlayInterval.observe(this) {
            val vfPicture = binding.layoutPhotoWidget.vfPicture
            val interval = it.interval
            if (interval < 0) {
                vfPicture.isAutoStart = false
                vfPicture.stopFlipping()
            } else {
                vfPicture.isAutoStart = true
                vfPicture.flipInterval = interval
                vfPicture.startFlipping()
            }
        }

        viewModel.photoScaleType.observe(this) {
            binding.layoutPhotoWidget.vfPicture.adapter = widgetAdapter
            widgetAdapter.setScaleType(it.scaleType)
        }

        viewModel.uiState.observe(this) {
            if (it == ConfigureViewModel.UIState.SHOW_CONTENT) {
                binding.layoutContent.circularRevealAnimator()
                    .setDuration(resources.androidMediumAnimTime)
                    .start()
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

    private fun saveWidget() {
        if (viewModel.uiState.value == ConfigureViewModel.UIState.LOADING) {
            return
        }
        if (viewModel.imageUriList.value.isNullOrEmpty()) {
            Toast.makeText(this, R.string.warning_select_picture, Toast.LENGTH_SHORT).show()
            return
        }
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
                itemList.indexOfFirst { it == viewModel.autoPlayInterval.value }
            ) { dialog, i ->
                viewModel.autoPlayInterval.value = itemList[i]
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
                        putExtra("linkInfo", viewModel.linkInfo.value)
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
                scaleTypeList.indexOfFirst { it == viewModel.photoScaleType.value }
            ) { dialog, i ->
                viewModel.photoScaleType.value = scaleTypeList[i]
                dialog.dismiss()
            }.show()
    }
}