package com.qihuan.photowidget.config

import android.Manifest
import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.qihuan.photowidget.R
import com.qihuan.photowidget.adapter.PreviewPhotoAdapter
import com.qihuan.photowidget.adapter.PreviewPhotoAddAdapter
import com.qihuan.photowidget.adapter.WidgetFrameResourceAdapter
import com.qihuan.photowidget.adapter.WidgetPhotoAdapter
import com.qihuan.photowidget.bean.LinkInfo
import com.qihuan.photowidget.bean.createAlbumLink
import com.qihuan.photowidget.bean.createFileLink
import com.qihuan.photowidget.common.*
import com.qihuan.photowidget.crop.CropPictureContract
import com.qihuan.photowidget.databinding.ActivityConfigureBinding
import com.qihuan.photowidget.ktx.*
import com.qihuan.photowidget.link.InstalledAppActivity
import com.qihuan.photowidget.link.UrlInputActivity
import com.qihuan.photowidget.view.ItemSelectionDialog
import com.qihuan.photowidget.view.MaterialColorPickerDialog
import com.qihuan.photowidget.view.RoundedViewOutlineProvider
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import kotlinx.coroutines.launch
import java.io.File

/**
 * AppWidget base config activity.
 *
 * @author qi
 * @since 2022/02/23
 */
abstract class BaseConfigureActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivityConfigureBinding::inflate)
    private val viewModel by viewModels<ConfigureViewModel> {
        ConfigureViewModelFactory(application, appWidgetId, widgetType)
    }

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    abstract val widgetType: WidgetType

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

    private var currentDeletePhotoIndex: Int? = null

    private val deletePhotoDialog by lazy(LazyThreadSafetyMode.NONE) {
        MaterialAlertDialogBuilder(this)
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
            viewModel.updatePhotoScaleType(item)
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
                LinkType.OPEN_ALBUM -> widgetOpenAlbum()
                LinkType.OPEN_FILE -> launchOpenFile()
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
            viewModel.updateAutoPlayInterval(item)
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

    private val previewAdapter by lazy { PreviewPhotoAdapter() }
    private val previewAddAdapter by lazy {
        val previewPhotoAddAdapter = PreviewPhotoAddAdapter()
        previewPhotoAddAdapter.submitList(listOf(1))
        previewPhotoAddAdapter
    }
    private val widgetAdapter by lazy { WidgetPhotoAdapter(this) }
    private val widgetFrameResourceAdapter by lazy {
        WidgetFrameResourceAdapter {
            when (it.type) {
                WidgetFrameType.COLOR -> {
                    showWidgetFrameColorSelector()
                }
                WidgetFrameType.IMAGE -> {
                    selectWidgetFrameForResult.launch("image/*")
                }
                else -> {
                    setWidgetFrame(it.type, uri = it.frameUri)
                }
            }
        }
    }

    private val selectPicForResult =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) {
            if (it.isNullOrEmpty()) {
                return@registerForActivityResult
            }
            if (it.size == 1 && widgetType == WidgetType.NORMAL) {
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

    @SuppressLint("MissingPermission")
    private val externalStorageResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                val wallpaperManager = WallpaperManager.getInstance(this)
                binding.ivWallpaper.load(wallpaperManager.drawable)
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

    private val selectWidgetFrameForResult =
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            if (it != null) {
                setWidgetFrame(WidgetFrameType.IMAGE, uri = it)
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

        bindView()
        initView()
    }

    private fun initView() {
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.confirm -> {
                    binding.toolbar.performHapticHeavyClick()
                    saveWidget()
                }
            }
            true
        }

        // Android 12 以上版本，微件预览为圆角矩形
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            binding.containerPhotoWidgetPreview.outlineProvider = RoundedViewOutlineProvider(
                resources.getDimension(R.dimen.widget_radius)
            )
            binding.containerPhotoWidgetPreview.clipToOutline = true
        }

        binding.layoutInfo.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
    }

    private fun bindView() {
        // 获取背景权限
        externalStorageResult.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

        binding.layoutPhotoWidget.vfPicture.adapter = widgetAdapter
        binding.rvPreviewList.adapter = ConcatAdapter(previewAddAdapter, previewAdapter)
        binding.rvPreviewWidgetFrame.adapter = widgetFrameResourceAdapter
        previewAdapter.setOnItemDeleteListener { position, _ ->
            showDeletePhotoAlert(position)
        }
        previewAddAdapter.setOnItemAddListener {
            if (widgetType == WidgetType.GIF && viewModel.imageList.value?.size ?: 0 >= 1) {
                binding.root.showSnackbar(R.string.multi_gif_widget_unsupported)
                return@setOnItemAddListener
            }
            val mimeType = if (widgetType == WidgetType.GIF) "image/gif" else "image/*"
            selectPicForResult.launch(mimeType)
        }
        bindDragHelper()

        viewModel.imageList.observe(this) {
            previewAdapter.submitList(it.toList())
            binding.layoutPhotoWidget.vfPicture.adapter = widgetAdapter
            widgetAdapter.setData(it)

            if (it.size <= 1) {
                viewModel.updateAutoPlayInterval(PlayInterval.NONE)
            }
        }

        viewModel.widgetFrameResourceList.observe(this) {
            widgetFrameResourceAdapter.submitList(it)
        }

        binding.sliderWidgetFrameWidth.addOnChangeListener { slider, _, fromUser ->
            if (fromUser) {
                slider.performHapticFeedback()
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

        binding.layoutPhotoWidget.areaLeft.setOnClickListener {
            binding.layoutPhotoWidget.vfPicture.showPrevious()
        }
        binding.layoutPhotoWidget.areaRight.setOnClickListener {
            binding.layoutPhotoWidget.vfPicture.showNext()
        }

        viewModel.widgetFrameType.observe(this) {
            when (it) {
                WidgetFrameType.NONE -> {
                    binding.containerPhotoWidgetPreview.setBackgroundResource(android.R.color.transparent)
                }
                else -> {
                }
            }
        }

        viewModel.widgetFrameColor.observe(this) {
            if (!it.isNullOrEmpty()) {
                binding.containerPhotoWidgetPreview.setBackgroundColor(Color.parseColor(it))
            }
        }

        viewModel.widgetFrameUri.observe(this) {
            if (it != null) {
                binding.containerPhotoWidgetPreview.loadToBackground(it)
            }
        }
    }

    private fun View.startScaleAnimation(startValue: Float, finalValue: Float) {
        SpringAnimation(this, SpringAnimation.SCALE_X, finalValue)
            .setStartValue(startValue)
            .start()
        SpringAnimation(this, SpringAnimation.SCALE_Y, finalValue)
            .setStartValue(startValue)
            .start()
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
                viewModel.swapImageList(fromPosition, toPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }
        }).attachToRecyclerView(binding.rvPreviewList)
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
        uris.forEach { viewModel.addImage(it) }
    }

    private fun setWidgetFrame(
        type: WidgetFrameType,
        color: String? = null,
        uri: Uri? = null
    ) {
        binding.containerPhotoWidgetPreview.startScaleAnimation(1.1f, 1f)
        viewModel.setWidgetFrame(type, color, uri)
    }

    private fun saveWidget() {
        if (viewModel.uiState.value == ConfigureViewModel.UIState.LOADING) {
            return
        }
        if (viewModel.imageList.value.isNullOrEmpty()) {
            binding.root.showSnackbar(R.string.warning_select_picture)
            return
        }
        lifecycleScope.launch {
            saveImageDialog.show()
            try {
                viewModel.saveWidget()
            } catch (e: SaveWidgetException) {
                saveImageDialog.dismiss()
                binding.root.showSnackbar(e.message ?: getString(R.string.save_fail))
                logE("ConfigureActivity", e.message, e)
                return@launch
            }
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

    fun showChangeRadiusUnitSelector() {
        radiusUnitDialog.show()
    }

    fun showDeleteLinkAlert() {
        deleteLinkDialog.show()
    }

    private fun showWidgetFrameColorSelector() {
        MaterialColorPickerDialog.Builder(this)
            .setTitle(R.string.widget_frame_color_dialog_title)
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.sure, object : ColorEnvelopeListener {
                override fun onColorSelected(envelope: ColorEnvelope?, fromUser: Boolean) {
                    if (envelope != null) {
                        setWidgetFrame(WidgetFrameType.COLOR, color = "#${envelope.hexCode}")
                    }
                }
            })
            .show()
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

    private fun widgetOpenAlbum() {
        viewModel.updateLinkInfo(createAlbumLink(appWidgetId))
    }

    private fun launchOpenFile() {
        getOpenFileLink.launch(arrayOf("*/*"))
    }
}