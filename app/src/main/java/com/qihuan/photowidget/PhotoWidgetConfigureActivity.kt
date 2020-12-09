package com.qihuan.photowidget

import android.Manifest
import android.animation.ObjectAnimator
import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.addListener
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.core.text.buildSpannedString
import androidx.core.text.italic
import androidx.core.text.scale
import androidx.core.view.*
import androidx.lifecycle.lifecycleScope
import com.qihuan.photowidget.bean.CropPictureInfo
import com.qihuan.photowidget.bean.WidgetInfo
import com.qihuan.photowidget.databinding.PhotoWidgetConfigureBinding
import com.qihuan.photowidget.db.AppDatabase
import com.qihuan.photowidget.ktx.blur
import com.qihuan.photowidget.ktx.dp
import com.qihuan.photowidget.ktx.viewBinding
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
        const val TEMP_FILE_NAME = "temp.png"
    }

    private enum class UIState {
        LOADING, SHOW_CONTENT
    }

    private val binding by viewBinding(PhotoWidgetConfigureBinding::inflate)

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private val failAnimation by lazy {
        val animation = AnimationUtils.loadAnimation(this, R.anim.item_anim_fall_down)
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                binding.btnSelectPicture.visibility = View.GONE
            }

            override fun onAnimationEnd(animation: Animation?) {
                binding.btnSelectPicture.show()
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }
        })
        animation
    }

    private val defAnimTime by lazy {
        resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
    }

    private val selectPicForResult =
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            if (it != null) {
                val outFile = File(cacheDir, TEMP_FILE_NAME)
                cropPicForResult.launch(CropPictureInfo(it, Uri.fromFile(outFile)))
            }
        }

    private val cropPicForResult =
        registerForActivityResult(CropPictureContract()) {
            if (it != null) {
                val widgetLayout = binding.layoutPhotoWidget.root
                widgetLayout.startAnimation(failAnimation)
                bindImage(it)
            }
        }

    private val externalStorageResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            val wallpaperDrawable = if (it) {
                val wallpaperManager = WallpaperManager.getInstance(this)
                wallpaperManager.drawable
            } else {
                ContextCompat.getDrawable(this, R.drawable.wallpaper_def)
            }
            if (wallpaperDrawable != null) {
                rootAnimIn(wallpaperDrawable)
            }
        }

    private val widgetInfoDao by lazy {
        AppDatabase.getDatabase(this).widgetInfoDao()
    }

    private val vibrator by lazy {
        getSystemService(Vibrator::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        adaptBars()
        setResult(RESULT_CANCELED)
        setContentView(binding.root)
        handleIntent(intent)
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

    private fun rootAnimIn(wallpaperDrawable: Drawable) {
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

    private fun setBackground(wallpaper: Drawable) {
        // 设置壁纸背景
        binding.root.background = wallpaper
        // 设置设置区域背景
        binding.scrollViewInfo.apply {
            post {
                val wallpaperBitmap = wallpaper.toBitmap()
                val translateY = wallpaperBitmap.height - height
                lifecycleScope.launch {
                    val blurBitmap = wallpaperBitmap.blur(
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

    private fun bindView() {
        lifecycleScope.launch {
            changeUIState(UIState.LOADING)
            val widgetInfo = widgetInfoDao.selectById(appWidgetId)
            if (widgetInfo != null) {
                bindRadius(widgetInfo.widgetRadius)
                bindPadding(widgetInfo.verticalPadding, widgetInfo.horizontalPadding)
                bindImage(widgetInfo.uri)
            }
            changeUIState(UIState.SHOW_CONTENT)
        }

        externalStorageResult.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

        binding.btnSelectPicture.setOnClickListener {
            selectPicForResult.launch("image/*")
        }

        binding.btnConfirm.setOnClickListener {
            doneEffect()

            val verticalPadding = binding.sliderVerticalPadding.value
            val horizontalPadding = binding.sliderHorizontalPadding.value
            val widgetRadius = binding.sliderWidgetRadius.value

            val uri = getUriFromWidget()
            if (uri == null) {
                Toast.makeText(this, getString(R.string.warning_select_picture), Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val widgetInfo = WidgetInfo(
                appWidgetId, uri, verticalPadding, horizontalPadding, widgetRadius
            )
            addWidget(widgetInfo)
        }

        binding.sliderWidgetRadius.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                sliderEffect()
            }
            val uri = getUriFromWidget()
            if (uri != null) {
                bindImage(uri)
            }
            setTitleAndProp(
                binding.tvWidgetRadius,
                getString(R.string.widget_radius),
                value
            )
        }

        binding.sliderHorizontalPadding.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                sliderEffect()
            }
            val uri = getUriFromWidget()
            if (uri != null) {
                bindImage(uri)
            }
            setTitleAndProp(
                binding.tvHorizontalPadding,
                getString(R.string.horizontal_padding),
                value
            )
        }

        binding.sliderVerticalPadding.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                sliderEffect()
            }
            val uri = getUriFromWidget()
            if (uri != null) {
                bindImage(uri)
            }
            setTitleAndProp(
                binding.tvVerticalPadding,
                getString(R.string.vertical_padding),
                value
            )
        }
    }

    private fun setTitleAndProp(textView: TextView, title: String, value: Float) {
        textView.text = buildSpannedString {
            append(title)
            scale(0.8F) { italic { append(" ${value.toInt()} dp ") } }
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

    private fun getUriFromWidget(): Uri? {
        val ivPicture = binding.layoutPhotoWidget.ivPicture
        if (ivPicture.tag == null) {
            return null
        }
        return ivPicture.tag as Uri
    }

    private fun bindImage(uri: Uri) {
        val verticalPadding = binding.sliderVerticalPadding.value.dp
        val horizontalPadding = binding.sliderHorizontalPadding.value.dp
        val widgetRadius = binding.sliderWidgetRadius.value.dp

        val ivPicture = binding.layoutPhotoWidget.ivPicture
        ivPicture.setImageBitmap(createWidgetBitmap(this, uri, widgetRadius))
        ivPicture.tag = uri

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
            val widgetId = widgetInfo.widgetId
            val uri = saveWidgetPhotoFile(widgetId)
            widgetInfo.uri = uri
            widgetInfoDao.save(widgetInfo)

            updateAppWidget(this@PhotoWidgetConfigureActivity, appWidgetManager, widgetInfo)

            // Make sure we pass back the original appWidgetId
            val resultValue = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(RESULT_OK, resultValue)
            finish()
        }
    }

    private suspend fun saveWidgetPhotoFile(widgetId: Int): Uri {
        return withContext(Dispatchers.IO) {
            val tempFile = File(cacheDir, TEMP_FILE_NAME)
            val widgetPhotoFile = File(filesDir, "widget_${widgetId}.png")
            if (tempFile.exists()) {
                tempFile.copyTo(widgetPhotoFile, overwrite = true)
                tempFile.delete()
            }
            return@withContext widgetPhotoFile.toUri()
        }
    }

    override fun finish() {
        super.finish()
        val tempFile = File(cacheDir, TEMP_FILE_NAME)
        if (tempFile.exists()) {
            tempFile.delete()
        }
    }
}