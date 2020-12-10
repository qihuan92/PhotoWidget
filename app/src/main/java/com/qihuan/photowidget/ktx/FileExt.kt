package com.qihuan.photowidget.ktx

import java.io.File

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