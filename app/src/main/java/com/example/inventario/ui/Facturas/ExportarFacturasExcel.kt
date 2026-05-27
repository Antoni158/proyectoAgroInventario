package com.example.inventario.ui.Facturas

import android.content.Context
import android.widget.Toast
import com.example.inventario.data.bodega.Factura
import com.example.inventario.ui.branding.BrandingExports
import com.example.inventario.ui.export.ExportShareUtil
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

fun exportarFacturasExcel(
    context: Context,
    facturas: List<Factura>,
    periodo: String,
    etiquetaBodega: String = ""
) {
    try {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Facturas")
        val headers = listOf(
            "Fecha", "Nº Factura", "Proveedor", "Código",
            "Descripción", "Total", "Usuario", "Notas"
        )
        val sub = if (etiquetaBodega.isNotBlank()) "Periodo: $periodo · Bodega: $etiquetaBodega"
        else "Periodo: $periodo"
        val startRow = BrandingExports.applyExcelBrandHeader(
            workbook, sheet, "Reporte de Facturas", headers, sub
        )

        facturas.forEachIndexed { index, factura ->
            val row = sheet.createRow(startRow + index)
            row.createCell(0).setCellValue(factura.fecha)
            row.createCell(1).setCellValue(factura.numeroFactura)
            row.createCell(2).setCellValue(factura.proveedor)
            row.createCell(3).setCellValue(factura.codigo)
            row.createCell(4).setCellValue(factura.descripcion)
            row.createCell(5).setCellValue(factura.total)
            row.createCell(6).setCellValue(factura.usuario)
            row.createCell(7).setCellValue(factura.notas)
        }

        val totalRow = sheet.createRow(startRow + facturas.size + 1)
        val totalStyle: XSSFCellStyle = workbook.createCellStyle().apply {
            val font = workbook.createFont().apply { bold = true }
            setFont(font)
            fillForegroundColor = IndexedColors.LIGHT_GREEN.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
        }
        totalRow.createCell(0).apply { setCellValue("TOTAL"); cellStyle = totalStyle }
        totalRow.createCell(5).apply {
            setCellValue(facturas.sumOf { it.total })
            cellStyle = totalStyle
        }

        headers.indices.forEach { sheet.autoSizeColumn(it) }

        val file = File(context.cacheDir, "facturas_${System.currentTimeMillis()}.xlsx")
        FileOutputStream(file).use { workbook.write(it) }
        workbook.close()
        ExportShareUtil.abrirExcel(context, file)
    } catch (e: Exception) {
        Toast.makeText(context, "Error Excel: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
