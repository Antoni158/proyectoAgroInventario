package com.example.inventario.ui.StockBajo

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.inventario.data.bodega.Producto
import com.example.inventario.ui.branding.BrandingExports
import com.example.inventario.ui.export.ExportShareUtil
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

fun exportarStockBajoExcel(
    context: Context,
    productos: List<Producto>,
    etiquetaBodega: String = "Bodega"
) {
    try {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Stock Bajo")
        val headers = listOf(
            "Código", "Descripción", "Categoría", "Cantidad",
            "Stock Mín.", "Estado", "Proveedor", "Ubicación", "Costo"
        )
        val startRow = BrandingExports.applyExcelBrandHeader(
            workbook, sheet, "Reporte Stock Bajo / Crítico", headers,
            "Bodega: $etiquetaBodega · Productos: ${productos.size}"
        )

        productos.forEachIndexed { index, p ->
            val row = sheet.createRow(startRow + index)
            val estado = when {
                p.cantidad <= 0 -> "SIN EXISTENCIA"
                p.cantidad <= 5 -> "CRÍTICO"
                else -> "BAJO"
            }
            row.createCell(0).setCellValue(p.codigo)
            row.createCell(1).setCellValue(p.descripcion)
            row.createCell(2).setCellValue(p.categoria)
            row.createCell(3).setCellValue(p.cantidad.toDouble())
            row.createCell(4).setCellValue(p.stockMinimo.toDouble())
            row.createCell(5).setCellValue(estado)
            row.createCell(6).setCellValue(p.proveedor)
            row.createCell(7).setCellValue(p.ubicacion)
            row.createCell(8).setCellValue(p.costo)
        }

        val totalRow = sheet.createRow(startRow + productos.size + 1)
        val style: XSSFCellStyle = workbook.createCellStyle().apply {
            val font = workbook.createFont().apply { bold = true }
            setFont(font)
            fillForegroundColor = IndexedColors.LIGHT_ORANGE.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
        }
        totalRow.createCell(0).apply { setCellValue("TOTAL ALERTAS"); cellStyle = style }
        totalRow.createCell(3).apply { setCellValue(productos.size.toDouble()); cellStyle = style }

        headers.indices.forEach { sheet.autoSizeColumn(it) }
        val file = File(context.cacheDir, "stock_bajo_${System.currentTimeMillis()}.xlsx")
        FileOutputStream(file).use { workbook.write(it) }
        workbook.close()
        ExportShareUtil.abrirExcel(context, file)
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
    }
}

fun exportarStockBajoPDF(
    context: Context,
    productos: List<Producto>,
    etiquetaBodega: String = "Bodega",
    usuario: String = ""
) {
    try {
        val pdf = PdfDocument()
        val pageWidth = 1800
        val pageHeight = 2200
        var pageNum = 1
        var page = pdf.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create())
        var canvas = page.canvas

        val encabezado = Paint().apply { textSize = 14f; isFakeBoldText = true }
        val texto = Paint().apply { textSize = 13f }
        val linea = Paint().apply { strokeWidth = 1f }

        fun nuevaPagina() {
            pdf.finishPage(page)
            pageNum++
            page = pdf.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create())
            canvas = page.canvas
        }

        var y = BrandingExports.drawPdfHeader(
            context, canvas, pageWidth,
            "Alertas de Stock",
            "Bodega: $etiquetaBodega${if (usuario.isNotBlank()) " · $usuario" else ""}"
        ) + 16f

        fun dibujarEncabezados() {
            canvas.drawText("Código", 40f, y, encabezado)
            canvas.drawText("Descripción", 180f, y, encabezado)
            canvas.drawText("Actual", 520f, y, encabezado)
            canvas.drawText("Mínimo", 620f, y, encabezado)
            canvas.drawText("Estado", 740f, y, encabezado)
            canvas.drawText("Proveedor", 900f, y, encabezado)
            y += 8f
            canvas.drawLine(40f, y, pageWidth - 40f, y, linea)
            y += 28f
        }

        dibujarEncabezados()

        productos.forEach { p ->
            if (y > pageHeight - 100) {
                nuevaPagina()
                y = 60f
                dibujarEncabezados()
            }
            val estado = when {
                p.cantidad <= 0 -> "SIN EXISTENCIA"
                p.cantidad <= 5 -> "CRÍTICO"
                else -> "BAJO"
            }
            canvas.drawText(p.codigo, 40f, y, texto)
            canvas.drawText(p.descripcion.take(35), 180f, y, texto)
            canvas.drawText(p.cantidad.toString(), 520f, y, texto)
            canvas.drawText(p.stockMinimo.toString(), 620f, y, texto)
            canvas.drawText(estado, 740f, y, texto)
            canvas.drawText(p.proveedor.take(20), 900f, y, texto)
            y += 26f
        }

        y += 20f
        canvas.drawLine(40f, y, pageWidth - 40f, y, linea)
        y += 30f
        canvas.drawText("Total alertas: ${productos.size}", 40f, y, encabezado)

        pdf.finishPage(page)
        val file = File(context.cacheDir, "stock_bajo_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { pdf.writeTo(it) }
        pdf.close()
        ExportShareUtil.abrirPdf(context, file)
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "Error PDF: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
    }
}
