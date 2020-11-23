package com.qihuan.albumwidget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.ImageDecoder
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
        registerForActivityResult(ActivityResultContracts.GetContent()) { result ->
            val outFile = File(filesDir, "widget_${appWidgetId}.png")
            cropPicForResult.launch(CropPictureInfo(result, Uri.fromFile(outFile)))
        }

    private val cropPicForResult =
        registerForActivityResult(CropPictureContract()) { result ->
            if (result != null) {
                bindImage(result)
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
                bindImage(widgetInfo.uri)
                bindRadius(widgetInfo.widgetRadius)
                bindPadding(widgetInfo.verticalPadding, widgetInfo.horizontalPadding)
            }
        }

        binding.btnSelectPicture.setOnClickListener {
            selectPicForResult.launch("image/*")
        }

        binding.btnConfirm.setOnClickListener {
            val verticalPadding = binding.sliderVerticalPadding.value.dp
            val horizontalPadding = binding.sliderHorizontalPadding.value.dp
            val widgetRadius = binding.sliderWidgetRadius.value.dp

            if (binding.ivPicturePrev.tag == null) {
                Toast.makeText(this, getString(R.string.warning_select_picture), Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val uri = binding.ivPicturePrev.tag as Uri
            val widgetInfo = WidgetInfo(
                appWidgetId, uri, verticalPadding, horizontalPadding, widgetRadius
            )
            addWidget(widgetInfo)
        }
    }

    private fun bindImage(uri: Uri) {
        val source = ImageDecoder.createSource(contentResolver, uri)
        binding.ivPicturePrev.setImageBitmap(ImageDecoder.decodeBitmap(source))
        binding.ivPicturePrev.tag = uri
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