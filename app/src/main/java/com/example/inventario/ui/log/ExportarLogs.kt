package com.example.inventario.ui.log

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.inventario.data.administracion.Log
import com.example.inventario.ui.branding.BrandingExports
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import com.example.inventario.util.FechaFormatter

fun exportarLogsExcel(context: Context, logs: List<Log>) {
    try {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Logs")
        val headers = listOf("Usuario", "Rol", "Módulo", "Acción", "Descripción", "Fecha")
        val startRow = BrandingExports.applyExcelBrandHeader(
            workbook, sheet, "Registro de Auditoría / Logs", headers
        )
        logs.forEachIndexed { index, log ->
            val row = sheet.createRow(startRow + index)
            row.createCell(0).setCellValue(log.username)
            row.createCell(1).setCellValue(log.rol)
            row.createCell(2).setCellValue(log.modulo)
            row.createCell(3).setCellValue(log.accion)
            row.createCell(4).setCellValue(log.descripcion)
            row.createCell(5).setCellValue(FechaFormatter.formatear(log.fecha))
        }
        val file = File(context.cacheDir, "logs_${System.currentTimeMillis()}.xlsx")
        FileOutputStream(file).use { workbook.write(it) }
        workbook.close()
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Abrir Excel"))
    } catch (e: Exception) {
        Toast.makeText(context, "Error Excel: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

fun exportarLogsTexto(context: Context, logs: List<Log>): File? {
    return try {
        val sb = StringBuilder("AUDITORÍA - LOGS DE ACTIVIDAD\n\n")
        logs.forEach { log ->
            sb.appendLine("${FechaFormatter.formatear(log.fecha)} | ${log.username} (${log.rol})")
            sb.appendLine("  ${log.modulo} / ${log.accion}: ${log.descripcion}")
            sb.appendLine()
        }
        val file = File(context.cacheDir, "logs_${System.currentTimeMillis()}.txt")
        file.writeText(sb.toString())
        file
    } catch (e: Exception) {
        Toast.makeText(context, "Error exportación: ${e.message}", Toast.LENGTH_LONG).show()
        null
    }
}
