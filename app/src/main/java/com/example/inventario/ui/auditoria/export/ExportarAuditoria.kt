package com.example.inventario.ui.auditoria.export

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.inventario.data.Auditoria.Auditoria
import com.example.inventario.ui.branding.BrandingExports
import com.example.inventario.util.FechaFormatter
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

fun exportarAuditoriaPDF(context: Context, auditorias: List<Auditoria>, bodegaNombre: String) {
    try {
        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(1400, 2000, 1).create()
        val page = pdf.startPage(pageInfo)
        val canvas = page.canvas
        val texto = Paint().apply { textSize = 13f }
        val enc = Paint().apply { textSize = 13f; isFakeBoldText = true }
        var y = BrandingExports.drawPdfHeader(
            context, canvas, pageInfo.pageWidth,
            "Centro de Auditoría",
            bodegaNombre
        ) + 16f
        auditorias.forEach { a ->
            if (y > 1900f) return@forEach
            canvas.drawText("${a.codigo} ${a.descripcion} · ${a.estado}", 40f, y, enc)
            y += 18f
            canvas.drawText(
                "Sist:${a.stockSistema} Fís:${a.stockFisico} Dif:${a.diferencia} · ${FechaFormatter.formatear(a.fecha)}",
                40f, y, texto
            )
            y += 22f
        }
        pdf.finishPage(page)
        val file = File(context.cacheDir, "auditoria_${System.currentTimeMillis()}.pdf")
        pdf.writeTo(FileOutputStream(file))
        pdf.close()
        abrirArchivo(context, file, "application/pdf")
    } catch (e: Exception) {
        Toast.makeText(context, "Error PDF: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

fun exportarAuditoriaExcel(context: Context, auditorias: List<Auditoria>, bodegaNombre: String) {
    try {
        val wb = XSSFWorkbook()
        val sheet = wb.createSheet("Auditoría")
        val headers = listOf(
            "Fecha", "Código", "Descripción", "Categoría", "Stock Sistema",
            "Stock Físico", "Diferencia", "Estado", "Auditor", "Observación"
        )
        var row = BrandingExports.applyExcelBrandHeader(
            wb, sheet, "Auditoría Empresarial", headers, bodegaNombre
        )
        auditorias.forEach { a ->
            val r = sheet.createRow(row++)
            r.createCell(0).setCellValue(FechaFormatter.formatear(a.fecha))
            r.createCell(1).setCellValue(a.codigo)
            r.createCell(2).setCellValue(a.descripcion)
            r.createCell(3).setCellValue(a.categoria)
            r.createCell(4).setCellValue(a.stockSistema)
            r.createCell(5).setCellValue(a.stockFisico)
            r.createCell(6).setCellValue(a.diferencia)
            r.createCell(7).setCellValue(a.estado)
            r.createCell(8).setCellValue(a.auditorNombre)
            r.createCell(9).setCellValue(a.observacion)
        }
        val file = File(context.cacheDir, "auditoria_${System.currentTimeMillis()}.xlsx")
        FileOutputStream(file).use { wb.write(it) }
        wb.close()
        abrirArchivo(context, file, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    } catch (e: Exception) {
        Toast.makeText(context, "Error Excel: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

private fun abrirArchivo(context: Context, file: File, mime: String) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mime)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(Intent.createChooser(intent, "Abrir"))
}
