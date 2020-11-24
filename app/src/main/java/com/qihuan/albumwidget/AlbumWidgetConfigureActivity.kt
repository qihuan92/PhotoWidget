package com.qihuan.albumwidget

import android.Manifest
import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.qihuan.albumwidget.bean.CropPictureInfo
import com.qihuan.albumwidget.bean.WidgetInfo
import com.qihuan.albumwidget.databinding.AlbumWidgetConfigureBinding
import com.qihuan.albumwidget.db.AppDatabase
import com.qihuan.albumwidget.ktx.dp
import com.qihuan.albumwidget.ktx.toDp
import com.qihuan.albumwidget.ktx.viewBinding
import com.qihuan.albumwidget.result.CropPictureContract
import kotlinx.coroutines.launch
import java.io.File

/**
 * The configuration screen for the [AlbumWidget] AppWidget.
 */
class AlbumWidgetConfigureActivity : AppCompatActivity() {

    private val binding by viewBinding(AlbumWidgetConfigureBinding::inflate)

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private val selectPicForResult =
        registerForActivityResult(ActivityResultContracts.GetContent()) {
            if (it != null) {
                val outFile = File(filesDir, "widget_${appWidgetId}.png")
                cropPicForResult.launch(CropPictureInfo(it, Uri.fromFile(outFile)))
            }
        }

    private val cropPicForResult =
        registerForActivityResult(CropPictureContract()) {
            val radius = binding.sliderWidgetRadius.value.dp
            if (it != null) {
                bindImage(it, radius)
            }
        }

    private val externalStorageResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                val wallpaperManager = WallpaperManager.getInstance(this)
                val wallpaperDrawable = wallpaperManager.drawable
                binding.ivWallpaper.setImageDrawable(wallpaperDrawable)
            } else {
                binding.cardPicture.strokeColor = getColor(R.color.colorDivider)
                binding.cardPicture.strokeWidth = 1F.dp
            }
        }

    private val widgetInfoDao by lazy {
        AppDatabase.getDatabase(this).widgetInfoDao()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)
        setContentView(binding.root)

        // Find the widget id from the intent.
        val extras = intent.extras
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
            val widgetInfo = widgetInfoDao.selectById(appWidgetId)
            if (widgetInfo != null) {
                bindImage(widgetInfo.uri, widgetInfo.widgetRadius)
                bindRadius(widgetInfo.widgetRadius)
                bindPadding(widgetInfo.verticalPadding, widgetInfo.horizontalPadding)
            }
        }

        externalStorageResult.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

        binding.btnSelectPicture.setOnClickListener {
            selectPicForResult.launch("image/*")
        }

        binding.btnConfirm.setOnClickListener {
            val verticalPadding = binding.sliderVerticalPadding.value.dp
            val horizontalPadding = binding.sliderHorizontalPadding.value.dp
            val widgetRadius = binding.sliderWidgetRadius.value.dp

            val ivPicture = binding.layoutAlbumWidget.ivPicture
            if (ivPicture.tag == null) {
                Toast.makeText(this, getString(R.string.warning_select_picture), Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val uri = ivPicture.tag as Uri
            val widgetInfo = WidgetInfo(
                appWidgetId, uri, verticalPadding, horizontalPadding, widgetRadius
            )
            addWidget(widgetInfo)
        }
    }

    private fun bindImage(uri: Uri, radius: Int) {
        val ivPicture = binding.layoutAlbumWidget.ivPicture
        ivPicture.setImageBitmap(createWidgetBitmap(this, uri, radius))
        ivPicture.tag = uri
    }

    private fun bindRadius(radius: Int) {
        binding.sliderWidgetRadius.value = radius.toDp(this)
    }

    private fun bindPadding(verticalPadding: Int, horizontalPadding: Int) {
        binding.sliderVerticalPadding.value = verticalPadding.toDp(this)
        binding.sliderHorizontalPadding.value = horizontalPadding.toDp(this)
    }

    private fun addWidget(widgetInfo: WidgetInfo) {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        lifecycleScope.launch {
            widgetInfoDao.save(widgetInfo)
            updateAppWidget(this@AlbumWidgetConfigureActivity, appWidgetManager, widgetInfo)

            // Make sure we pass back the original appWidgetId
            val resultValue = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(RESULT_OK, resultValue)
            finish()
        }
    }
}