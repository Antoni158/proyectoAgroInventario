package com.example.inventario.ui.export

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

object ExportShareUtil {

    fun abrirExcel(context: Context, file: File) {
        abrirArchivo(context, file, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "Abrir Excel")
    }

    fun abrirPdf(context: Context, file: File) {
        abrirArchivo(context, file, "application/pdf", "Abrir PDF")
    }

    fun compartirArchivo(context: Context, file: File, mimeType: String, titulo: String) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, titulo))
        } catch (e: Exception) {
            Toast.makeText(context, "Error al compartir: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun abrirArchivo(context: Context, file: File, mimeType: String, titulo: String) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, titulo))
            Toast.makeText(context, "Archivo generado correctamente", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
