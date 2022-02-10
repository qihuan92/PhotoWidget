package com.qihuan.photowidget.ktx

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import com.qihuan.photowidget.common.CompressFormatCompat
import com.qihuan.photowidget.common.DEFAULT_COMPRESSION_QUALITY
import com.qihuan.photowidget.common.FileExtension
import com.yalantis.ucrop.util.BitmapLoadUtils
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.default
import id.zelory.compressor.constraint.destination
import id.zelory.compressor.constraint.size
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

/**
 * FileExt
 * @author qi
 * @since 12/10/20
 */

@Suppress("BlockingMethodInNonBlockingContext")
suspend fun Context.copyFile(inputUri: Uri, outputUri: Uri) = withContext(Dispatchers.IO) {
    var inputStream: InputStream? = null
    var outputStream: OutputStream? = null
    try {
        inputStream = contentResolver.openInputStream(inputUri)
        outputStream = FileOutputStream(File(checkNotNull(outputUri.path)))
        checkNotNull(inputStream, { "InputStream for given input Uri is null" })
        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } > 0) {
            outputStream.write(buffer, 0, length)
        }
    } catch (e: Exception) {
        logE("FileExt", "copyFile() Exception", e)
    } finally {
        BitmapLoadUtils.close(outputStream)
        BitmapLoadUtils.close(inputStream)
    }
}

suspend fun Context.compressImageFile(imageFile: File): File {
    val destination = File(
        imageFile.parentFile,
        "${imageFile.nameWithoutExtension}.${FileExtension.WEBP}"
    )
    val compressedFile = Compressor.compress(this, imageFile) {
        default(format = CompressFormatCompat.WEBP_LOSSY, quality = DEFAULT_COMPRESSION_QUALITY)
        size(5242880)
        destination(destination)
    }
    withContext(Dispatchers.IO) {
        imageFile.delete()
        File(cacheDir, "compressor").deleteRecursively()
    }
    return compressedFile
}

fun File.providerUri(context: Context): Uri =
    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", this)

fun Uri.providerUri(context: Context) = toFile().providerUri(context)

fun Uri.getExtension(context: Context): String? {
    val mimeType = context.contentResolver.getType(this)
    return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
}

fun createFile(parent: File, nameWithoutExtension: String, extension: String? = null): File {
    if (!parent.exists()) {
        parent.mkdirs()
    }

    val fileName = if (extension != null) {
        "$nameWithoutExtension.$extension"
    } else {
        nameWithoutExtension
    }
    return File(parent, fileName)
}

fun File.calculateSizeRecursively(): Long {
    return walkBottomUp().fold(0L, { acc, file -> acc + file.length() })
}

fun File.calculateFormatSizeRecursively(): String {
    val size = calculateSizeRecursively()
    if (size <= 0) {
        return "0Bytes"
    }
    val unitArray = arrayOf("Bytes", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(size.toDouble()) / log10(1024f)).toInt()
    return DecimalFormat("#,##0.#").format(size / 1024.0.pow(digitGroups.toDouble())) + unitArray[digitGroups]
}