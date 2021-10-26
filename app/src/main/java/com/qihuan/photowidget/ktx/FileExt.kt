package com.qihuan.photowidget.ktx

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.net.toFile
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

/**
 * FileExt
 * @author qi
 * @since 12/10/20
 */
fun File.deleteDir(deleteFiles: Boolean = true) {
    if (!exists()) {
        return
    }
    if (!isDirectory) {
        return
    }
    if (deleteFiles) {
        listFiles()?.forEach {
            it.delete()
        }
    }
    delete()
}

fun copyDir(source: File, target: File, override: Boolean = false) {
    if (override) {
        if (target.exists() && target.isDirectory) {
            target.deleteDir()
        }
    }

    if (!target.exists()) {
        target.mkdirs()
    }

    if (source.exists() && source.isDirectory) {
        val sourceFileList = source.listFiles()
        sourceFileList?.forEach {
            val targetFile = File(target, it.name)
            it.copyTo(targetFile, overwrite = override)
        }
    }
}

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

suspend fun Context.compressImageFile(imageFile: File, destination: File = imageFile): File {
    return Compressor.compress(this, imageFile) {
        default(format = Bitmap.CompressFormat.PNG)
        size(5242880)
        destination(destination)
    }
}

fun File.providerUri(context: Context): Uri =
    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", this)

fun Uri.providerUri(context: Context) = toFile().providerUri(context)