package com.example.inventario.ui.Kardex

import android.content.Context
import com.example.inventario.data.bodega.Kardex
import com.example.inventario.ui.branding.BrandingExports
import com.example.inventario.ui.export.ExportShareUtil
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

fun exportarKardexExcel(
    context: Context,
    movimientos: List<Kardex>,
    etiquetaBodega: String = "Bodega",
    usuario: String = ""
) {
    try {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Kardex")

        val headers = listOf(
            "Fecha", "Código", "Producto", "Tipo", "Cantidad",
            "Saldo Ant.", "Saldo Nuevo", "Costo Unit.", "Total",
            "Usuario", "Factura", "Vale", "Destino", "Notas"
        )
        val sub = buildString {
            append("Bodega: $etiquetaBodega")
            if (usuario.isNotBlank()) append(" · Usuario: $usuario")
            append(" · Registros: ${movimientos.size}")
        }
        val startRow = BrandingExports.applyExcelBrandHeader(
            workbook, sheet, "Reporte Kardex", headers, sub
        )

        movimientos.forEachIndexed { index, k ->
            val row = sheet.createRow(startRow + index)
            row.createCell(0).setCellValue(k.fechaMovimiento)
            row.createCell(1).setCellValue(k.codigoProducto)
            row.createCell(2).setCellValue(k.descripcion)
            row.createCell(3).setCellValue(k.tipoMovimiento)
            row.createCell(4).setCellValue(k.cantidad.toDouble())
            row.createCell(5).setCellValue(k.saldoAnterior.toDouble())
            row.createCell(6).setCellValue(k.saldoNuevo.toDouble())
            row.createCell(7).setCellValue(k.costoUnitario)
            row.createCell(8).setCellValue(k.totalMovimiento)
            row.createCell(9).setCellValue(k.usuario)
            row.createCell(10).setCellValue(k.numeroFactura)
            row.createCell(11).setCellValue(k.numeroVale)
            row.createCell(12).setCellValue(k.destino)
            row.createCell(13).setCellValue(k.notas)
        }

        val totalRow = sheet.createRow(startRow + movimientos.size + 1)
        val totalStyle: XSSFCellStyle = workbook.createCellStyle().apply {
            val font = workbook.createFont().apply { bold = true }
            setFont(font)
            fillForegroundColor = IndexedColors.LIGHT_GREEN.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
        }
        totalRow.createCell(0).apply {
            setCellValue("TOTAL MOVIMIENTOS")
            cellStyle = totalStyle
        }
        totalRow.createCell(4).apply {
            setCellValue(movimientos.sumOf { it.cantidad }.toDouble())
            cellStyle = totalStyle
        }
        totalRow.createCell(8).apply {
            setCellValue(movimientos.sumOf { it.totalMovimiento })
            cellStyle = totalStyle
        }

        headers.indices.forEach { sheet.autoSizeColumn(it) }

        val file = File(context.cacheDir, "kardex_${System.currentTimeMillis()}.xlsx")
        FileOutputStream(file).use { workbook.write(it) }
        workbook.close()
        ExportShareUtil.abrirExcel(context, file)
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "Error Excel: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
    }
}
