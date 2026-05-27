package com.example.inventario.ui.entradas



import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.inventario.data.bodega.Entrada
import com.example.inventario.ui.branding.BrandingExports


import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

fun exportarEntradasExcel(context: Context, entradas: List<Entrada>, periodo: String, etiquetaBodega: String = "Bodega") {
    val workbook = XSSFWorkbook()
    val sheet = workbook.createSheet("Entradas")

    // Encabezados
    val headers = listOf(
        "Fecha",
        "Código",
        "Descripción",
        "Cantidad",
        "Unidad",
        "Ubicación",
        "Proveedor",
        "Notas"
    )

    val startRow = BrandingExports.applyExcelBrandHeader(
        workbook, sheet, "Reporte de Entradas", headers, "Periodo: $periodo · $etiquetaBodega"
    )

    entradas.forEachIndexed { index, entrada ->
        val row = sheet.createRow(startRow + index)
        row.createCell(0).setCellValue(entrada.fechaIngreso)
        row.createCell(1).setCellValue(entrada.codigoProducto)
        row.createCell(2).setCellValue(entrada.descripcion)
        row.createCell(3).setCellValue(entrada.cantidad.toDouble())
        row.createCell(4).setCellValue(entrada.unidad)
        row.createCell(5).setCellValue(entrada.ubicacion)
        row.createCell(6).setCellValue(entrada.proveedor)
        row.createCell(7).setCellValue(entrada.notas)
    }

    // Ajustar columnas
    for (i in headers.indices) {
        sheet.autoSizeColumn(i)
    }

    val file = File(context.cacheDir, "entradas_${System.currentTimeMillis()}.xlsx")

    try {
        val fos = FileOutputStream(file)
        workbook.write(fos)
        fos.close()
        workbook.close()

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Abrir Excel con:"))
    } catch (e: Exception) {
        Toast.makeText(context, "Error al generar Excel: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
