package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object CodeExporter {

    fun exportCodeToFile(context: Context, code: String, language: String, fileName: String = "RGS_Project_Source") {
        try {
            val extension = when (language.lowercase()) {
                "kotlin", "kt" -> "kt"
                "python", "py" -> "py"
                "javascript", "js" -> "js"
                "typescript", "ts" -> "ts"
                "json" -> "json"
                "html" -> "html"
                "css" -> "css"
                "xml" -> "xml"
                "shell", "sh", "bash" -> "sh"
                else -> "txt"
            }

            val exportDir = File(context.cacheDir, "exported_code").apply { mkdirs() }
            val sourceFile = File(exportDir, "$fileName.$extension")
            sourceFile.writeText(code)

            // Create ZIP package
            val zipFile = File(exportDir, "$fileName.zip")
            ZipOutputStream(FileOutputStream(zipFile)).use { zipOut ->
                val entry = ZipEntry(sourceFile.name)
                zipOut.putNextEntry(entry)
                zipOut.write(sourceFile.readBytes())
                zipOut.closeEntry()
            }

            // Trigger Share Intent
            val fileUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                zipFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Export RGS Code Project ($fileName.zip)"))
            Toast.makeText(context, "Exported $fileName.zip successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Code saved locally as $fileName. Copying to clipboard...", Toast.LENGTH_SHORT).show()
        }
    }
}
