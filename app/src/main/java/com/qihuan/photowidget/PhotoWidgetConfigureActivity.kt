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
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.addListener
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.core.view.*
import androidx.lifecycle.lifecycleScope
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.ConcatAdapter
import com.qihuan.photowidget.adapter.PreviewPhotoAdapter
import com.qihuan.photowidget.adapter.PreviewPhotoAddAdapter
import com.qihuan.photowidget.adapter.WidgetPhotoAdapter
import com.qihuan.photowidget.bean.CropPictureInfo
import com.qihuan.photowidget.bean.ScreenSize
import com.qihuan.photowidget.bean.WidgetInfo
import com.qihuan.photowidget.databinding.PhotoWidgetConfigureBinding
import com.qihuan.photowidget.db.AppDatabase
import com.qihuan.photowidget.ktx.*
import com.qihuan.photowidget.result.CropPictureContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private val imageUriList by lazy { mutableListOf<Uri>() }
    private val previewAdapter by lazy { PreviewPhotoAdapter() }
    private val previewAddAdapter by lazy {
        val previewPhotoAddAdapter = PreviewPhotoAddAdapter()
        previewPhotoAddAdapter.submitList(listOf(1))
        previewPhotoAddAdapter
    }
    private val widgetAdapter by lazy { WidgetPhotoAdapter(this) }
    private val widgetInfoDao by lazy { AppDatabase.getDatabase(this).widgetInfoDao() }
    private val vibrator by lazy { getSystemService(Vibrator::class.java) }
    private val screenSize by lazy {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        ScreenSize(displayMetrics.widthPixels, displayMetrics.heightPixels)
    }
    private val defAnimTime by lazy {
        resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
    }

    private val selectPicForResult =
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            if (it != null) {
                val outDir = File(cacheDir, TEMP_DIR_NAME)
                if (!outDir.exists()) {
                    outDir.mkdirs()
                }

                val outFile = File(outDir, "${System.currentTimeMillis()}.png")
                cropPicForResult.launch(CropPictureInfo(it, Uri.fromFile(outFile)))
            }
        }

    private val cropPicForResult =
        registerForActivityResult(CropPictureContract()) {
            if (it != null) {
                imageUriList.add(it)
                previewAdapter.submitList(imageUriList.toList())
                bindImage()
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

        binding.rvPreviewList.adapter = ConcatAdapter(previewAddAdapter, previewAdapter)
        previewAdapter.setOnItemDeleteListener { position, uri ->
            imageUriList.removeAt(position)
            previewAdapter.submitList(imageUriList.toList())
            bindImage()

            val tempFile = uri.toFile()
            if (tempFile.exists()) {
                tempFile.delete()
            }
        }
        previewAddAdapter.setOnItemAddListener {
            selectPicForResult.launch("image/*")
        }

        binding.layoutPhotoWidget.vfPicture.adapter = widgetAdapter
        handleIntent(intent)
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

        bindView()
    }

    private fun adaptBars() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.scrollViewInfo) { view, insets ->
            val barInsets = insets.getInsets(
                WindowInsetsCompat.Type.ime() or WindowInsetsCompat.Type.navigationBars()
            )
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

    private fun bindView() {
        lifecycleScope.launch {
            changeUIState(UIState.LOADING)
            val widgetInfo = widgetInfoDao.selectById(appWidgetId)
            if (widgetInfo != null) {
                copyToTempDir(widgetInfo.widgetId)
                bindRadius(widgetInfo.widgetRadius)
                bindPadding(widgetInfo.verticalPadding, widgetInfo.horizontalPadding)
                bindImage()
                previewAdapter.submitList(imageUriList.toList())
            }
            changeUIState(UIState.SHOW_CONTENT)
        }

        externalStorageResult.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

        binding.btnConfirm.setOnClickListener {
            doneEffect()

            val verticalPadding = binding.sliderVerticalPadding.value
            val horizontalPadding = binding.sliderHorizontalPadding.value
            val widgetRadius = binding.sliderWidgetRadius.value

            if (imageUriList.isNullOrEmpty()) {
                Toast.makeText(this, getString(R.string.warning_select_picture), Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val widgetInfo = WidgetInfo(
                appWidgetId, imageUriList, verticalPadding, horizontalPadding, widgetRadius
            )
            addWidget(widgetInfo)
        }

        binding.sliderWidgetRadius.addOnChangeListener { _, _, fromUser ->
            if (fromUser) {
                sliderEffect()
            }
            bindImage()
        }

        binding.sliderHorizontalPadding.addOnChangeListener { _, _, fromUser ->
            if (fromUser) {
                sliderEffect()
            }
            bindImage()
        }

        binding.sliderVerticalPadding.addOnChangeListener { _, _, fromUser ->
            if (fromUser) {
                sliderEffect()
            }
            bindImage()
        }
    }

    private fun sliderEffect() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(1, 1))
        }
    }

    private fun doneEffect() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
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

    private fun bindImage() {
        val verticalPadding = binding.sliderVerticalPadding.value.dp
        val horizontalPadding = binding.sliderHorizontalPadding.value.dp
        val widgetRadius = binding.sliderWidgetRadius.value.dp

        widgetAdapter.setData(imageUriList, widgetRadius)

        val widgetRoot = binding.layoutPhotoWidget.root
        widgetRoot.setPadding(
            horizontalPadding,
            verticalPadding,
            horizontalPadding,
            verticalPadding
        )
    }

    private fun bindRadius(radius: Float) {
        binding.sliderWidgetRadius.value = radius
    }

    private fun bindPadding(verticalPadding: Float, horizontalPadding: Float) {
        binding.sliderVerticalPadding.value = verticalPadding
        binding.sliderHorizontalPadding.value = horizontalPadding
    }

    private fun addWidget(widgetInfo: WidgetInfo) {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        lifecycleScope.launch {
            changeUIState(UIState.LOADING)
            val widgetId = widgetInfo.widgetId
            val uri = saveWidgetPhotoFiles(widgetId)
            widgetInfo.uri = uri
            widgetInfoDao.save(widgetInfo)
            updateAppWidget(this@PhotoWidgetConfigureActivity, appWidgetManager, widgetInfo)
            changeUIState(UIState.SHOW_CONTENT)

            // Make sure we pass back the original appWidgetId
            val resultValue = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(RESULT_OK, resultValue)
            finish()
        }
    }

    private suspend fun saveWidgetPhotoFiles(widgetId: Int): List<Uri> {
        return withContext(Dispatchers.IO) {
            val tempDir = File(cacheDir, TEMP_DIR_NAME)
            val widgetDir = File(filesDir, "widget_${widgetId}")
            copyDir(tempDir, widgetDir, override = true)

            val uriList = mutableListOf<Uri>()
            if (widgetDir.exists()) {
                widgetDir.listFiles()?.forEach {
                    uriList.add(it.toUri())
                }
            }
            return@withContext uriList
        }
    }

    private suspend fun copyToTempDir(widgetId: Int) {
        withContext(Dispatchers.IO) {
            val tempDir = File(cacheDir, TEMP_DIR_NAME)
            val widgetDir = File(filesDir, "widget_${widgetId}")
            copyDir(widgetDir, tempDir, override = true)

            if (tempDir.exists()) {
                tempDir.listFiles()?.forEach {
                    imageUriList.add(it.toUri())
                }
            }
        }
    }
}