package com.example.inventario.ui.reportes

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.inventario.ui.branding.BrandingExports
import com.example.inventario.ui.export.ExportShareUtil
import com.example.inventario.viewModel.CostoDetalleLinea
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

fun exportarCostosOperativosExcel(
    context: Context,
    lineas: List<CostoDetalleLinea>,
    etiquetaBodega: String,
    periodo: String
) {
    try {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Costos operativos")
        val headers = listOf(
            "Mes", "Fecha", "Concepto", "Vehículo", "Destino", "Área", "Monto (Q)"
        )
        val startRow = BrandingExports.applyExcelBrandHeader(
            workbook, sheet,
            "Detalle de costos operativos",
            headers,
            "$etiquetaBodega · $periodo · ${lineas.size} movimientos"
        )

        lineas.forEachIndexed { index, l ->
            val row = sheet.createRow(startRow + index)
            row.createCell(0).setCellValue(l.mesEtiqueta)
            row.createCell(1).setCellValue(l.fecha)
            row.createCell(2).setCellValue(l.concepto)
            row.createCell(3).setCellValue(l.vehiculo)
            row.createCell(4).setCellValue(l.destino)
            row.createCell(5).setCellValue(l.area)
            row.createCell(6).setCellValue(l.monto)
        }

        val total = lineas.sumOf { it.monto }
        val totalRow = sheet.createRow(startRow + lineas.size + 1)
        val style: XSSFCellStyle = workbook.createCellStyle().apply {
            val font = workbook.createFont().apply { bold = true }
            setFont(font)
            fillForegroundColor = IndexedColors.LIGHT_GREEN.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
        }
        totalRow.createCell(2).apply { setCellValue("TOTAL"); cellStyle = style }
        totalRow.createCell(6).apply {
            setCellValue(total)
            cellStyle = style
        }

        headers.indices.forEach { sheet.autoSizeColumn(it) }
        val file = File(context.cacheDir, "costos_operativos_${System.currentTimeMillis()}.xlsx")
        FileOutputStream(file).use { workbook.write(it) }
        workbook.close()
        ExportShareUtil.abrirExcel(context, file)
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "Error Excel: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
    }
}

fun exportarCostosOperativosPDF(
    context: Context,
    lineas: List<CostoDetalleLinea>,
    etiquetaBodega: String,
    periodo: String
) {
    try {
        val pdf = PdfDocument()
        val pageWidth = 1800
        val pageHeight = 2200
        val marginBottom = 200f
        var pageNum = 1
        var page = pdf.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create())
        var canvas = page.canvas
        val textPaint = Paint().apply { textSize = 13f }
        val headerPaint = Paint().apply { textSize = 13f; isFakeBoldText = true }
        val linePaint = Paint().apply { strokeWidth = 1f }

        fun nuevaPagina() {
            pdf.finishPage(page)
            pageNum++
            page = pdf.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create())
            canvas = page.canvas
        }

        var y = BrandingExports.drawPdfHeader(
            context, canvas, pageWidth,
            "Detalle costos operativos",
            "$etiquetaBodega · $periodo"
        ) + 16f

        canvas.drawText("Mes", 40f, y, headerPaint)
        canvas.drawText("Concepto", 200f, y, headerPaint)
        canvas.drawText("Vehículo", 520f, y, headerPaint)
        canvas.drawText("Destino", 720f, y, headerPaint)
        canvas.drawText("Área", 950f, y, headerPaint)
        canvas.drawText("Monto", 1200f, y, headerPaint)
        y += 8f
        canvas.drawLine(40f, y, pageWidth - 40f, y, linePaint)
        y += 28f

        var mesAnterior = ""
        lineas.forEach { l ->
            if (y > pageHeight - marginBottom) {
                nuevaPagina()
                y = 80f
            }
            if (l.mesEtiqueta != mesAnterior) {
                canvas.drawText(l.mesEtiqueta, 40f, y, headerPaint)
                mesAnterior = l.mesEtiqueta
                y += 22f
            }
            canvas.drawText(l.fecha.take(10), 40f, y, textPaint)
            canvas.drawText(l.concepto.take(28), 200f, y, textPaint)
            canvas.drawText(l.vehiculo.take(18), 520f, y, textPaint)
            canvas.drawText(l.destino.take(18), 720f, y, textPaint)
            canvas.drawText(l.area.take(18), 950f, y, textPaint)
            canvas.drawText(
                "Q ${String.format(Locale.US, "%.2f", l.monto)}",
                1200f, y, textPaint
            )
            y += 26f
        }

        y += 12f
        canvas.drawText(
            "TOTAL: Q ${String.format(Locale.US, "%.2f", lineas.sumOf { it.monto })}",
            950f, y,
            Paint().apply { textSize = 18f; isFakeBoldText = true }
        )

        pdf.finishPage(page)
        val file = File(context.cacheDir, "costos_operativos_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { pdf.writeTo(it) }
        pdf.close()
        ExportShareUtil.abrirPdf(context, file)
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "Error PDF: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
    }
}
