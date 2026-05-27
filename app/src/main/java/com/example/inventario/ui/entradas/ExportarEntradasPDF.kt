package com.example.inventario.ui.entradas

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.inventario.data.bodega.Entrada
import com.example.inventario.ui.branding.BrandingExports
import com.example.inventario.ui.export.ExportShareUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

suspend fun generarEntradasPdfFile(
    context: Context,
    entradas: List<Entrada>,
    periodo: String,
    etiquetaBodega: String = "Bodega",
    onProgress: (Float) -> Unit = {}
): File = withContext(Dispatchers.IO) {
    onProgress(0.1f)
    val file = File(context.cacheDir, "entradas_${System.currentTimeMillis()}.pdf")
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(1400, 2000, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas

    val textoPaint = Paint().apply { textSize = 14f }
    val encabezadoPaint = Paint().apply { textSize = 14f; isFakeBoldText = true }
    val lineaPaint = Paint().apply { strokeWidth = 1f }

    onProgress(0.25f)
    var y = BrandingExports.drawPdfHeader(
        context, canvas, pageInfo.pageWidth,
        "Reporte Detallado de Entradas",
        "Periodo: $periodo · $etiquetaBodega"
    ) + 20f

    canvas.drawText("Fecha", 40f, y, encabezadoPaint)
    canvas.drawText("Código", 150f, y, encabezadoPaint)
    canvas.drawText("Descripción", 270f, y, encabezadoPaint)
    canvas.drawText("Cant.", 550f, y, encabezadoPaint)
    canvas.drawText("Unidad", 630f, y, encabezadoPaint)
    canvas.drawText("Ubicación", 730f, y, encabezadoPaint)
    canvas.drawText("Proveedor", 880f, y, encabezadoPaint)
    canvas.drawText("Notas", 1100f, y, encabezadoPaint)

    y += 10f
    canvas.drawLine(40f, y, 1360f, y, lineaPaint)
    y += 35f

    onProgress(0.4f)
    val total = entradas.size.coerceAtLeast(1)
    entradas.forEachIndexed { index, e ->
        if (y > 1900) return@forEachIndexed
        canvas.drawText(e.fechaIngreso.take(10), 40f, y, textoPaint)
        canvas.drawText(e.codigoProducto, 150f, y, textoPaint)
        canvas.drawText(e.descripcion.take(25), 270f, y, textoPaint)
        canvas.drawText(e.cantidad.toString(), 550f, y, textoPaint)
        canvas.drawText(e.unidad, 630f, y, textoPaint)
        canvas.drawText(e.ubicacion, 730f, y, textoPaint)
        canvas.drawText(e.proveedor.take(20), 880f, y, textoPaint)
        canvas.drawText(e.notas.take(30), 1100f, y, textoPaint)
        y += 30f
        onProgress(0.4f + (index + 1) * 0.5f / total)
    }

    pdfDocument.finishPage(page)
    onProgress(0.95f)
    FileOutputStream(file).use { fos -> pdfDocument.writeTo(fos) }
    pdfDocument.close()
    onProgress(1f)
    file
}

fun exportarEntradasPDF(
    context: Context,
    entradas: List<Entrada>,
    periodo: String,
    etiquetaBodega: String = "Bodega"
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val file = generarEntradasPdfFile(context, entradas, periodo, etiquetaBodega)
            withContext(Dispatchers.Main) {
                ExportShareUtil.abrirPdf(context, file)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }
}
