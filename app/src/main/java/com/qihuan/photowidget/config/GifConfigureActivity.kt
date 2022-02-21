package com.qihuan.photowidget.config

import android.Manifest
import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.qihuan.photowidget.R
import com.qihuan.photowidget.bean.LinkInfo
import com.qihuan.photowidget.bean.createAlbumLink
import com.qihuan.photowidget.bean.createFileLink
import com.qihuan.photowidget.common.LinkType
import com.qihuan.photowidget.common.RadiusUnit
import com.qihuan.photowidget.common.SaveWidgetException
import com.qihuan.photowidget.common.TEMP_DIR_NAME
import com.qihuan.photowidget.databinding.ActivityGifConfigureBinding
import com.qihuan.photowidget.ktx.*
import com.qihuan.photowidget.link.InstalledAppActivity
import com.qihuan.photowidget.link.UrlInputActivity
import com.qihuan.photowidget.view.ItemSelectionDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * The configuration screen for the [com.qihuan.photowidget.GifPhotoWidgetProvider] AppWidget.
 */
class GifConfigureActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivityGifConfigureBinding::inflate)
    private val viewModel by viewModels<GifConfigureViewModel> {
        GifConfigureViewModelFactory(
            application,
            appWidgetId
        )
    }

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    private val processImageDialog by lazy(LazyThreadSafetyMode.NONE) {
        createLoadingDialog(R.string.processing)
    }

    private val saveImageDialog by lazy(LazyThreadSafetyMode.NONE) {
        createLoadingDialog(R.string.saving)
    }

    private val deleteLinkDialog by lazy(LazyThreadSafetyMode.NONE) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.alert_title_default)
            .setMessage(R.string.conform_delete_photo_link)
            .setPositiveButton(R.string.sure) { _, _ ->
                viewModel.deleteLink()
            }
            .setNegativeButton(R.string.cancel) { _, _ -> }
            .create()
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
                LinkType.OPEN_ALBUM -> widgetOpenAlbum()
                LinkType.OPEN_FILE -> launchOpenFile()
            }
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

    private val selectPicForResult =
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            if (it != null) {
                addPhoto(it)
            }
        }

    @SuppressLint("MissingPermission")
    private val externalStorageResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                val wallpaperManager = WallpaperManager.getInstance(this)
                binding.ivWallpaper.setImageDrawable(wallpaperManager.drawable)
            }
        }

    private val appSelectResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                it.data?.apply {
                    val linkInfo = getParcelableExtra<LinkInfo>("linkInfo")
                    viewModel.updateLinkInfo(linkInfo)
                }
            }
        }

    private val getOpenFileLink =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            if (it != null) {
                // keep permission
                contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                viewModel.updateLinkInfo(createFileLink(appWidgetId, it))
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setResult(RESULT_CANCELED)
        setContentView(binding.root)
        handleIntent(intent)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.activity = this

        binding.root.paddingStatusBar()
        binding.scrollViewInfo.paddingNavigationBar()
        binding.fabAddPhoto.marginNavigationBar()

        bindView()
        initView()
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

        binding.fabAddPhoto.setOnClickListener {
            selectPicForResult.launch("image/gif")
        }
    }

    override fun finish() {
        super.finish()
        val tempDir = File(cacheDir, TEMP_DIR_NAME)
        tempDir.deleteRecursively()
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
    }

    private fun addPhoto(vararg uris: Uri) {
        if (uris.isNullOrEmpty()) {
            return
        }
        lifecycleScope.launch {
            processImageDialog.show()
            for (uri in uris) {
                val outDir = File(cacheDir, TEMP_DIR_NAME)
                withContext(Dispatchers.IO) { outDir.deleteRecursively() }
                if (!outDir.exists()) {
                    outDir.mkdirs()
                }

                val tempOutFile = File(outDir, "${System.currentTimeMillis()}.gif").also { file ->
                    copyFile(uri, file.toUri())
                }

                try {
                    viewModel.addImage(tempOutFile.toUri())
                } catch (e: NoSuchFileException) {
                    logE("ConfigureActivity", e.message, e)
                }
            }
            processImageDialog.dismiss()
        }
    }

    private fun saveWidget() {
        if (viewModel.uiState.value == BaseConfigViewModel.UIState.LOADING) {
            return
        }
        if (viewModel.imageUriList.value.isNullOrEmpty()) {
            Snackbar.make(binding.root, R.string.warning_select_picture, Snackbar.LENGTH_SHORT)
                .setAnchorView(binding.fabAddPhoto)
                .show()
            return
        }
        lifecycleScope.launch {
            saveImageDialog.show()
            try {
                viewModel.saveWidget()
            } catch (e: SaveWidgetException) {
                saveImageDialog.dismiss()
                logE("GifConfigureActivity", e.message, e)
                Snackbar.make(binding.root, R.string.save_widget_error_gif, Snackbar.LENGTH_SHORT)
                    .setAnchorView(binding.fabAddPhoto)
                    .show()
                return@launch
            }
            saveImageDialog.dismiss()

            setResult(RESULT_OK, Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            })
            finish()
        }
    }

    fun showLinkTypeSelector() {
        linkTypeDialog.show()
    }

    fun showDeleteLinkAlert() {
        deleteLinkDialog.show()
    }

    fun showChangeRadiusUnitSelector() {
        radiusUnitDialog.show()
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

    private fun widgetOpenAlbum() {
        viewModel.updateLinkInfo(createAlbumLink(appWidgetId))
    }

    private fun launchOpenFile() {
        getOpenFileLink.launch(arrayOf("*/*"))
    }
}