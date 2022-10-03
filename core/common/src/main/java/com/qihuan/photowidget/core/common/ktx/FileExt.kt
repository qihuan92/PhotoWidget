package com.qihuan.photowidget.core.common.ktx

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import com.qihuan.photowidget.core.common.CopyFileException
import com.qihuan.photowidget.core.model.CompressFormatCompat
import com.qihuan.photowidget.core.model.DEFAULT_COMPRESSION_QUALITY
import com.qihuan.photowidget.core.model.FileExtension
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
fun File.createOrExistsDir(): Boolean {
    return if (exists()) isDirectory else mkdirs()
}

@Throws(CopyFileException::class)
fun Context.copyFileSmart(inputUri: Uri, outputFile: File) {
    val path = inputUri.path ?: return
    var inputStream: InputStream? = null
    var outputStream: OutputStream? = null
    try {
        inputStream = if (path.startsWith("/android_asset")) {
            val assetsName = path.substringAfterLast("android_asset/")
            assets.open(assetsName)
        } else {
            contentResolver.openInputStream(inputUri)
        }

        checkNotNull(inputStream) { "InputStream for given input Uri is null" }

        outputStream = FileOutputStream(outputFile)

        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } > 0) {
            outputStream.write(buffer, 0, length)
        }
    } catch (e: Exception) {
        logE("FileExt", "copyFileSmart() Exception", e)
        throw CopyFileException(e.message ?: "保存文件失败")
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
    return if ("content" == scheme) {
        val mimeType = context.contentResolver.getType(this)
        MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
    } else {
        path?.substringAfterLast(".")
    }
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
    return walkBottomUp().fold(0L) { acc, file -> acc + file.length() }
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