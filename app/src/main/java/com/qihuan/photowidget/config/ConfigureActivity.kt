package com.qihuan.photowidget.config

import android.Manifest
import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.core.view.*
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.qihuan.photowidget.R
import com.qihuan.photowidget.adapter.PreviewPhotoAdapter
import com.qihuan.photowidget.adapter.PreviewPhotoAddAdapter
import com.qihuan.photowidget.adapter.WidgetPhotoAdapter
import com.qihuan.photowidget.bean.LinkType
import com.qihuan.photowidget.bean.PhotoScaleType
import com.qihuan.photowidget.bean.PlayInterval
import com.qihuan.photowidget.common.TEMP_DIR_NAME
import com.qihuan.photowidget.crop.CropPictureContract
import com.qihuan.photowidget.databinding.ActivityConfigureBinding
import com.qihuan.photowidget.ktx.*
import com.qihuan.photowidget.link.InstalledAppActivity
import com.qihuan.photowidget.link.UrlInputActivity
import com.qihuan.photowidget.view.ItemSelectionDialog
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

/**
 * The configuration screen for the [com.qihuan.photowidget.PhotoWidgetProvider] AppWidget.
 */
class ConfigureActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivityConfigureBinding::inflate)
    private val viewModel by viewModels<ConfigureViewModel>()

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    private val processImageDialog by lazy(LazyThreadSafetyMode.NONE) {
        MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Rounded)
            .setTitle(R.string.processing)
            .setCancelable(false)
            .setView(ProgressBar(this).apply {
                updatePadding(top = 10f.dp, bottom = 10f.dp)
            })
            .create()
    }

    private val saveImageDialog by lazy(LazyThreadSafetyMode.NONE) {
        MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Rounded)
            .setTitle(R.string.saving)
            .setCancelable(false)
            .setView(ProgressBar(this).apply {
                updatePadding(top = 10f.dp, bottom = 10f.dp)
            })
            .create()
    }

    private val deleteLinkDialog by lazy(LazyThreadSafetyMode.NONE) {
        MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Rounded)
            .setTitle(R.string.alert_title_default)
            .setMessage(R.string.conform_delete_photo_link)
            .setPositiveButton(R.string.sure) { _, _ ->
                viewModel.deleteLink()
            }
            .setNegativeButton(R.string.cancel) { _, _ -> }
            .create()
    }

    private var currentDeletePhotoIndex: Int? = null

    private val deletePhotoDialog by lazy(LazyThreadSafetyMode.NONE) {
        MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_Rounded)
            .setTitle(R.string.alert_title_default)
            .setMessage(R.string.conform_delete_photo)
            .setPositiveButton(R.string.sure) { _, _ ->
                currentDeletePhotoIndex?.let {
                    viewModel.deleteImage(it)
                }
            }
            .setNegativeButton(R.string.cancel) { _, _ -> }
            .setOnDismissListener { currentDeletePhotoIndex = null }
            .create()
    }

    private val scaleTypeDialog by lazy(LazyThreadSafetyMode.NONE) {
        ItemSelectionDialog(
            this,
            getString(R.string.alert_title_scale_type),
            PhotoScaleType.values().toList()
        ) { dialog, item ->
            viewModel.photoScaleType.value = item
            dialog.dismiss()
        }
    }

    private val linkTypeDialog by lazy(LazyThreadSafetyMode.NONE) {
        ItemSelectionDialog(
            this,
            getString(R.string.alert_title_link_type),
            LinkType.values().toList()
        ) { dialog, item ->
            when (item) {
                LinkType.OPEN_APP -> launchOpenAppActivity()
                LinkType.OPEN_URL -> launchOpenLinkActivity()
            }
            dialog.dismiss()
        }
    }

    private val intervalDialog by lazy(LazyThreadSafetyMode.NONE) {
        ItemSelectionDialog(
            this,
            getString(R.string.alert_title_interval),
            PlayInterval.values().toList()
        ) { dialog, item ->
            viewModel.autoPlayInterval.value = item
            dialog.dismiss()
        }
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
        previewAdapter.setOnItemDeleteListener { position, _ ->
            showDeletePhotoAlert(position)
        }
        previewAddAdapter.setOnItemAddListener {
            selectPicForResult.launch("image/*")
        }
        bindDragHelper()

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

        binding.layoutPhotoWidget.photoWidgetInfo.areaLeft.setOnClickListener {
            binding.layoutPhotoWidget.vfPicture.showPrevious()
        }
        binding.layoutPhotoWidget.photoWidgetInfo.areaRight.setOnClickListener {
            binding.layoutPhotoWidget.vfPicture.showNext()
        }
    }

    private fun bindDragHelper() {
        ItemTouchHelper(object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                if (viewHolder !is PreviewPhotoAdapter.ViewHolder) {
                    return makeMovementFlags(0, 0)
                }
                return makeMovementFlags(ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT, 0)
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.bindingAdapterPosition
                val toPosition = target.bindingAdapterPosition
                val list = viewModel.imageUriList.value ?: mutableListOf()
                Collections.swap(list, fromPosition, toPosition)
                previewAdapter.submitList(list)
                viewModel.imageUriList.value = list
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }
        }).attachToRecyclerView(binding.rvPreviewList)
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
            Snackbar.make(binding.root, R.string.warning_select_picture, Snackbar.LENGTH_SHORT)
                .show()
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
        intervalDialog.show()
    }

    fun showLinkTypeSelector() {
        linkTypeDialog.show()
    }

    fun showScaleTypeSelector() {
        scaleTypeDialog.show()
    }

    fun showDeleteLinkAlert() {
        deleteLinkDialog.show()
    }

    private fun showDeletePhotoAlert(position: Int) {
        currentDeletePhotoIndex = position
        deletePhotoDialog.show()
    }

    private fun launchOpenAppActivity() {
        appSelectResult.launch(
            Intent(this, InstalledAppActivity::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            })
    }

    private fun launchOpenLinkActivity() {
        appSelectResult.launch(
            Intent(this, UrlInputActivity::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                val linkInfo = viewModel.linkInfo.value
                if (linkInfo != null && linkInfo.type == LinkType.OPEN_URL) {
                    putExtra("openUrl", linkInfo.link)
                }
            })
    }
}