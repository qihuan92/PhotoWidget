package com.qihuan.photowidget

import android.Manifest
import android.animation.ObjectAnimator
import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.addListener
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.*
import androidx.lifecycle.lifecycleScope
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.ConcatAdapter
import com.qihuan.photowidget.adapter.PreviewPhotoAdapter
import com.qihuan.photowidget.adapter.PreviewPhotoAddAdapter
import com.qihuan.photowidget.adapter.WidgetPhotoAdapter
import com.qihuan.photowidget.bean.*
import com.qihuan.photowidget.databinding.PhotoWidgetConfigureBinding
import com.qihuan.photowidget.ktx.*
import com.qihuan.photowidget.result.CropPictureContract
import kotlinx.coroutines.launch
import java.io.File

/**
 * The configuration screen for the [PhotoWidgetProvider] AppWidget.
 */
class PhotoWidgetConfigureActivity : AppCompatActivity() {

    companion object {
        const val TEMP_DIR_NAME = "temp"
    }

    private enum class UIState {
        LOADING, SHOW_CONTENT
    }

    private val binding by viewBinding(PhotoWidgetConfigureBinding::inflate)
    private val viewModel by viewModels<ConfigureViewModel>()

    var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    private val previewAdapter by lazy { PreviewPhotoAdapter() }
    private val previewAddAdapter by lazy {
        val previewPhotoAddAdapter = PreviewPhotoAddAdapter()
        previewPhotoAddAdapter.submitList(listOf(1))
        previewPhotoAddAdapter
    }
    private val widgetAdapter by lazy { WidgetPhotoAdapter(this) }
    private val screenSize by lazy {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        ScreenSize(displayMetrics.widthPixels, displayMetrics.heightPixels)
    }
    private val defAnimTime by lazy {
        resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
    }
    private val intervalItems by lazy {
        listOf(
            Pair("无", null),
            Pair("3秒", 3000),
            Pair("5秒", 5000),
            Pair("10秒", 10000),
            Pair("30秒", 30000),
        )
    }
    private var tempOutFile: File? = null

    private val selectPicForResult =
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            if (it != null) {
                val outDir = File(cacheDir, TEMP_DIR_NAME)
                if (!outDir.exists()) {
                    outDir.mkdirs()
                }

                tempOutFile = File(outDir, "${System.currentTimeMillis()}.png")
                cropPicForResult.launch(CropPictureInfo(it, Uri.fromFile(tempOutFile)))
            }
        }

    private val cropPicForResult =
        registerForActivityResult(CropPictureContract()) {
            if (it != null) {
                viewModel.addImage(it)
            } else {
                tempOutFile?.delete()
            }
        }

    private val externalStorageResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            val wallpaper = if (it) {
                val wallpaperManager = WallpaperManager.getInstance(this)
                wallpaperManager.drawable.toBitmap()
            } else {
                ContextCompat.getDrawable(this, R.drawable.wallpaper_def)?.toBitmap(
                    screenSize.width, screenSize.height
                )
            }
            if (wallpaper != null) {
                rootAnimIn(wallpaper)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        adaptBars()
        setResult(RESULT_CANCELED)
        setContentView(binding.root)
        binding.viewModel = viewModel
        binding.activity = this
        bindView()
        handleIntent(intent)
    }

    private fun bindView() {
        // 获取背景权限
        externalStorageResult.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

        binding.layoutPhotoWidget.vfPicture.adapter = widgetAdapter
        binding.rvPreviewList.adapter = ConcatAdapter(previewAddAdapter, previewAdapter)
        previewAdapter.setOnItemDeleteListener { uri ->
            viewModel.deleteImage(uri)
        }
        previewAddAdapter.setOnItemAddListener {
            selectPicForResult.launch("image/*")
        }

        viewModel.imageUriList.observe(this) {
            if (it.size <= 1) {
                viewModel.autoPlayInterval.value = null
                binding.layoutAutoPlayInterval.isGone = true
            } else {
                binding.layoutAutoPlayInterval.isGone = false
            }

            previewAdapter.submitList(it.toList())
            binding.layoutPhotoWidget.vfPicture.adapter = widgetAdapter
            val widgetRadius = viewModel.widgetRadius.get().dp
            widgetAdapter.setData(it, widgetRadius)
        }

        viewModel.horizontalPadding.observe {
            val horizontalPadding = it.dp
            binding.layoutPhotoWidget.root.updatePadding(
                left = horizontalPadding,
                right = horizontalPadding
            )
        }

        viewModel.verticalPadding.observe {
            val verticalPadding = it.dp
            binding.layoutPhotoWidget.root.updatePadding(
                top = verticalPadding,
                bottom = verticalPadding
            )
        }

        viewModel.widgetRadius.observe {
            val imageUriList = viewModel.imageUriList.value.orEmpty()
            binding.layoutPhotoWidget.vfPicture.adapter = widgetAdapter
            widgetAdapter.setData(imageUriList, it.dp)
        }

        viewModel.reEdit.observe {
            binding.layoutPhotoWidget.photoWidgetInfo.ivInfo.isVisible = it
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

        viewModel.isLoading.observe(this) {
            if (it != null) {
                if (it) {
                    changeUIState(UIState.LOADING)
                } else {
                    changeUIState(UIState.SHOW_CONTENT)
                }
            }
        }

        viewModel.isDone.observe(this) {
            if (it != null && it) {
                setResult(RESULT_OK, Intent().apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                })
                finish()
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
                view.updatePadding(bottom = barInsets.bottom + binding.btnConfirm.height)
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

    private fun rootAnimIn(wallpaperDrawable: Bitmap) {
        val alphaAnimator = ObjectAnimator.ofFloat(binding.root, View.ALPHA, 0.0f, 1.0f)
        alphaAnimator.addListener(
            onStart = {
                setBackground(wallpaperDrawable)
            }
        )
        alphaAnimator.duration = defAnimTime
        alphaAnimator.interpolator = AccelerateInterpolator()
        alphaAnimator.start()
    }

    private fun setBackground(wallpaper: Bitmap) {
        // 设置壁纸背景
        binding.root.background = BitmapDrawable(resources, wallpaper)
        // 状态栏文字颜色适配
        adaptStatusBarTextColor(wallpaper)
        // 设置设置区域背景
        binding.scrollViewInfo.apply {
            post {
                val translateY = wallpaper.height - height
                lifecycleScope.launch {
                    val blurBitmap = wallpaper.blur(
                        this@apply.context,
                        width = width,
                        height = height,
                        translateY = translateY
                    )
                    background = BitmapDrawable(resources, blurBitmap)
                }
            }
        }
    }

    private fun changeUIState(uiState: UIState) {
        when (uiState) {
            UIState.LOADING -> {
                binding.layoutInfo.visibility = View.GONE
                binding.loadingView.visibility = View.VISIBLE
            }
            UIState.SHOW_CONTENT -> {
                binding.layoutInfo.visibility = View.VISIBLE
                binding.layoutInfo.scheduleLayoutAnimation()

                binding.loadingView.visibility = View.GONE
            }
        }
    }

    fun showIntervalSelector() {
        val itemNameList = intervalItems.map { it.first }.toTypedArray()
        val itemValueList = intervalItems.map { it.second }.toTypedArray()
        AlertDialog.Builder(this)
            .setSingleChoiceItems(
                itemNameList,
                itemValueList.indexOf(viewModel.autoPlayInterval.value)
            ) { dialog, i ->
                viewModel.autoPlayInterval.value = intervalItems[i].second
                dialog.dismiss()
            }.show()
    }
}