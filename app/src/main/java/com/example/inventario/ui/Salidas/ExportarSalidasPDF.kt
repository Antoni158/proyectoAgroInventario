package com.example.inventario.ui.salidas

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.inventario.data.bodega.Salida
import com.example.inventario.ui.branding.BrandingExports
import com.example.inventario.ui.export.ExportShareUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

suspend fun generarSalidasPdfFile(
    context: Context,
    salidas: List<Salida>,
    periodo: String,
    etiquetaBodega: String = "Bodega",
    onProgress: (Float) -> Unit = {}
): File = withContext(Dispatchers.IO) {
    onProgress(0.1f)
    val file = File(context.cacheDir, "salidas_${System.currentTimeMillis()}.pdf")
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
        "Reporte Detallado de Salidas",
        "Periodo: $periodo · $etiquetaBodega"
    ) + 20f

    canvas.drawText("Fecha", 40f, y, encabezadoPaint)
    canvas.drawText("Código", 150f, y, encabezadoPaint)
    canvas.drawText("Descripción", 270f, y, encabezadoPaint)
    canvas.drawText("Cant.", 550f, y, encabezadoPaint)
    canvas.drawText("Destino", 630f, y, encabezadoPaint)
    canvas.drawText("Responsable", 800f, y, encabezadoPaint)
    canvas.drawText("Vehículo", 950f, y, encabezadoPaint)
    canvas.drawText("Notas", 1100f, y, encabezadoPaint)

    y += 10f
    canvas.drawLine(40f, y, 1360f, y, lineaPaint)
    y += 35f

    onProgress(0.4f)
    val total = salidas.size.coerceAtLeast(1)
    salidas.forEachIndexed { index, s ->
        if (y > 1900) return@forEachIndexed
        canvas.drawText(s.fechaSalida.take(10), 40f, y, textoPaint)
        canvas.drawText(s.codigoProducto.ifBlank { s.codigoSalida }.take(12), 150f, y, textoPaint)
        canvas.drawText(s.descripcion.take(25), 270f, y, textoPaint)
        canvas.drawText(s.cantidad.toString(), 550f, y, textoPaint)
        canvas.drawText(s.destino.take(15), 630f, y, textoPaint)
        canvas.drawText(s.responsable.take(15), 800f, y, textoPaint)
        canvas.drawText(s.vehiculo.take(15), 950f, y, textoPaint)
        canvas.drawText(s.notas.take(30), 1100f, y, textoPaint)
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

/** @deprecated Usar generarSalidasPdfFile + launchPdfExport desde UI */
fun exportarSalidasPDF(
    context: Context,
    salidas: List<Salida>,
    periodo: String,
    etiquetaBodega: String = "Bodega"
) {
    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
        try {
            val file = generarSalidasPdfFile(context, salidas, periodo, etiquetaBodega)
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                ExportShareUtil.abrirPdf(context, file)
            }
        } catch (e: Exception) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }
}
