package com.example.inventario.ui.Facturas

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.inventario.data.bodega.Factura
import com.example.inventario.ui.branding.BrandingExports
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun exportarFacturasPDF(
    context: Context,
    facturas: List<Factura>,
    periodo: String,
    bodegaId: String = facturas.firstOrNull()?.bodegaId.orEmpty(),
    etiquetaBodega: String = bodegaId
) {
    val file = File(context.cacheDir, "facturas_${System.currentTimeMillis()}.pdf")
    val pdfDocument = PdfDocument()
    
    // Ancho ampliado para reporte completo
    val pageInfo = PdfDocument.PageInfo.Builder(1400, 2000, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas

    val tituloPaint = Paint().apply { textSize = 30f; isFakeBoldText = true }
    val subtituloPaint = Paint().apply { textSize = 20f; isFakeBoldText = false }
    val textoPaint = Paint().apply { textSize = 14f }
    val encabezadoPaint = Paint().apply { textSize = 14f; isFakeBoldText = true }
    val lineaPaint = Paint().apply { strokeWidth = 1f }

    var y = BrandingExports.drawPdfHeader(
        context, canvas, pageInfo.pageWidth,
        "Reporte General de Facturas",
        "Periodo: $periodo · Bodega: $etiquetaBodega"
    ) + 20f
    // Encabezados
    canvas.drawText("N° Factura", 40f, y, encabezadoPaint)
    canvas.drawText("Fecha", 180f, y, encabezadoPaint)
    canvas.drawText("Proveedor", 320f, y, encabezadoPaint)
    canvas.drawText("Código/Prod.", 550f, y, encabezadoPaint)
    canvas.drawText("Categoría", 800f, y, encabezadoPaint)
    canvas.drawText("Notas", 1000f, y, encabezadoPaint)
    canvas.drawText("Total", 1250f, y, encabezadoPaint)

    y += 10f
    canvas.drawLine(40f, y, 1360f, y, lineaPaint)
    y += 35f

    var granTotal = 0.0
    facturas.forEach { f ->
        if (y > 1900) return@forEach
        granTotal += f.total

        canvas.drawText(f.numeroFactura, 40f, y, textoPaint)
        canvas.drawText(f.fecha, 180f, y, textoPaint)
        canvas.drawText(f.proveedor.take(20), 320f, y, textoPaint)
        canvas.drawText("${f.codigo} - ${f.descripcion.take(15)}", 550f, y, textoPaint)
        canvas.drawText(f.categoria, 800f, y, textoPaint)
        canvas.drawText(f.notas.take(25), 1000f, y, textoPaint)
        canvas.drawText("Q${String.format(Locale.US, "%.2f", f.total)}", 1250f, y, textoPaint)
        y += 30f
    }

    y += 20f
    canvas.drawLine(40f, y, 1360f, y, lineaPaint)
    y += 40f
    canvas.drawText("SUMA TOTAL DE FACTURAS: Q ${String.format(Locale.US, "%.2f", granTotal)}", 950f, y, tituloPaint.apply { textSize = 22f })

    pdfDocument.finishPage(page)

    try {
        val fos = FileOutputStream(file)
        pdfDocument.writeTo(fos)
        fos.close()
        pdfDocument.close()

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        context.startActivity(Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    } catch (e: Exception) {
        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
