package com.omerakpul.localpdf.data.service

import java.io.File
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads basic metadata (name, size, page count) from content URIs.
 * Shared by all feature ViewModels.
 */
@Singleton
class FileInfoProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    init {
        PDFBoxResourceLoader.init(context)
    }

    fun getFileName(uri: Uri): String? {
        if (uri.scheme == "file") return uri.path?.let { File(it).name }
        var name: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }

    fun getFileSize(uri: Uri): Long {
        if (uri.scheme == "file") return uri.path?.let { File(it).length() } ?: 0L
        var size = 0L
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst() && sizeIndex >= 0) {
                size = cursor.getLong(sizeIndex)
            }
        }
        return size
    }

    fun getPageCount(uri: Uri): Int {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                PDDocument.load(inputStream).use { document ->
                    document.numberOfPages
                }
            } ?: 0
        } catch (e: Exception) {
            0
        }
    }
}
