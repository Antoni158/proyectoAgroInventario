package com.example.inventario.ui.branding

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.xssf.usermodel.XSSFFont
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BrandingExports {

    const val BRAND_NAME = "INVENTARIO AGRÍCOLA"
    private const val VERDE_PDF = "#1B5E20"
    private const val VERDE_CLARO_PDF = "#E8F5E9"
    private const val LOGO_PDF_WIDTH_PX = 240

    /**
     * Encabezado corporativo con el logo completo (gráfico + INVENTARIO AGRÍCOLA).
     * Retorna la coordenada Y inicial para el contenido del reporte.
     */
    fun drawPdfHeader(
        context: Context,
        canvas: Canvas,
        pageWidth: Int,
        reportTitle: String,
        subtitle: String? = null
    ): Float {
        val verde = Color.parseColor(VERDE_PDF)
        val verdeClaro = Color.parseColor(VERDE_CLARO_PDF)

        val barPaint = Paint().apply { color = verde }
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 10f, barPaint)

        val logo = decodeLogoForPdf(context)
        val logoTop = 22f
        val logoLeft = 36f
        val logoHeight = logo?.height?.toFloat() ?: 120f
        val headerBottom = logoTop + logoHeight + 24f

        canvas.drawRect(0f, 10f, pageWidth.toFloat(), headerBottom, Paint().apply { color = verdeClaro })

        if (logo != null) {
            val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            canvas.drawBitmap(logo, logoLeft, logoTop, bitmapPaint)
        }

        val textX = logoLeft + (logo?.width?.toFloat() ?: LOGO_PDF_WIDTH_PX.toFloat()) + 32f
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = verde
            textSize = 24f
            isFakeBoldText = true
        }
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#33691E")
            textSize = 15f
        }
        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.GRAY
            textSize = 12f
        }

        var textY = logoTop + 36f
        canvas.drawText(reportTitle, textX, textY, titlePaint)
        textY += 32f
        subtitle?.let {
            canvas.drawText(it, textX, textY, subPaint)
            textY += 26f
        }
        val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Generado: $fecha", textX, textY, datePaint)

        val lineY = headerBottom + 8f
        val linePaint = Paint().apply {
            color = verde
            strokeWidth = 2f
        }
        canvas.drawLine(36f, lineY, pageWidth - 36f, lineY, linePaint)

        return lineY + 28f
    }

    fun decodeLogo(context: Context): Bitmap? =
        LogoBitmapUtil.decodeLogoTransparent(context)

    fun decodeLogoForPdf(context: Context): Bitmap? =
        LogoBitmapUtil.decodeLogoForExport(context, LOGO_PDF_WIDTH_PX)

    /** Fila de título verde + fila de encabezados de columna en Excel. */
    fun applyExcelBrandHeader(
        workbook: XSSFWorkbook,
        sheet: XSSFSheet,
        reportTitle: String,
        columnHeaders: List<String>,
        subtitle: String? = null
    ): Int {
        val verdeRgb = byteArrayOf(0x1B.toByte(), 0x5E.toByte(), 0x20.toByte())
        val verdeClaroRgb = byteArrayOf(0xE8.toByte(), 0xF5.toByte(), 0xE9.toByte())

        val titleStyle: XSSFCellStyle = workbook.createCellStyle().apply {
            val font: XSSFFont = workbook.createFont().apply {
                bold = true
                fontHeightInPoints = 16
                color = IndexedColors.WHITE.index
            }
            setFont(font)
            setFillForegroundColor(XSSFColor(verdeRgb, null))
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.CENTER
            verticalAlignment = VerticalAlignment.CENTER
        }

        val subStyle: XSSFCellStyle = workbook.createCellStyle().apply {
            val font: XSSFFont = workbook.createFont().apply {
                fontHeightInPoints = 11
                color = IndexedColors.WHITE.index
            }
            setFont(font)
            setFillForegroundColor(XSSFColor(verdeRgb, null))
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.CENTER
        }

        val headerStyle: XSSFCellStyle = workbook.createCellStyle().apply {
            val font: XSSFFont = workbook.createFont().apply {
                bold = true
                color = IndexedColors.WHITE.index
            }
            setFont(font)
            setFillForegroundColor(XSSFColor(verdeRgb, null))
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.CENTER
            verticalAlignment = VerticalAlignment.CENTER
        }

        val brandRow = sheet.createRow(0)
        brandRow.heightInPoints = 28f
        val brandCell = brandRow.createCell(0)
        brandCell.setCellValue("$BRAND_NAME — $reportTitle")
        brandCell.cellStyle = titleStyle
        sheet.addMergedRegion(
            org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, (columnHeaders.size - 1).coerceAtLeast(0))
        )

        var nextRow = 1
        if (subtitle != null) {
            val subRow = sheet.createRow(1)
            subRow.heightInPoints = 18f
            val subCell = subRow.createCell(0)
            subCell.setCellValue(subtitle)
            subCell.cellStyle = subStyle
            sheet.addMergedRegion(
                org.apache.poi.ss.util.CellRangeAddress(1, 1, 0, (columnHeaders.size - 1).coerceAtLeast(0))
            )
            nextRow = 2
        }

        val headerRow = sheet.createRow(nextRow)
        headerRow.heightInPoints = 22f
        columnHeaders.forEachIndexed { index, header ->
            val cell = headerRow.createCell(index)
            cell.setCellValue(header)
            cell.cellStyle = headerStyle
        }

        val decorRow = sheet.createRow(nextRow + 1)
        decorRow.heightInPoints = 4f
        val decorStyle = workbook.createCellStyle().apply {
            setFillForegroundColor(XSSFColor(verdeClaroRgb, null))
            fillPattern = FillPatternType.SOLID_FOREGROUND
        }
        columnHeaders.indices.forEach { i ->
            decorRow.createCell(i).cellStyle = decorStyle
        }

        return nextRow + 2
    }
}
