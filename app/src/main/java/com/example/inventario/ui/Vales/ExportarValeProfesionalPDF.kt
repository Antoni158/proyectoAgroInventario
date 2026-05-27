package com.example.inventario.ui.Vales

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.inventario.data.bodega.ValeConDetalles
import com.example.inventario.ui.branding.BrandingExports
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun exportarValeProfesionalPDF(
    context: Context,
    valeCompleto: ValeConDetalles,
    etiquetaBodega: String = "Bodega"
) {
    try {
        val vale = valeCompleto.vale
        val detalles = valeCompleto.detalles
        val file = File(context.cacheDir, "Vale_${vale.codigoVale}.pdf")
        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(900, 1400, 1).create()
        val page = pdf.startPage(pageInfo)
        val canvas = page.canvas

        val titulo = Paint().apply { textSize = 28f; isFakeBoldText = true }
        val subtitulo = Paint().apply { textSize = 16f; isFakeBoldText = true }
        val texto = Paint().apply { textSize = 14f }
        val encabezado = Paint().apply { textSize = 13f; isFakeBoldText = true }
        val linea = Paint().apply { strokeWidth = 1.5f }

        var y = BrandingExports.drawPdfHeader(
            context, canvas, pageInfo.pageWidth,
            "Vale de Salida",
            "Código: ${vale.codigoVale} · $etiquetaBodega"
        ) + 12f
        canvas.drawText("Responsable: ${vale.responsable}", 50f, y, texto)
        y += 22f
        canvas.drawText("Destino: ${vale.destino}", 50f, y, texto)
        y += 22f
        canvas.drawText("Fecha: ${vale.fecha}", 50f, y, texto)
        y += 22f
        canvas.drawText("Estado: ${vale.estado}", 50f, y, texto)
        y += 30f
        canvas.drawLine(50f, y, 850f, y, linea)
        y += 28f

        canvas.drawText("Código", 50f, y, encabezado)
        canvas.drawText("Descripción", 180f, y, encabezado)
        canvas.drawText("Cat.", 420f, y, encabezado)
        canvas.drawText("Cant.", 500f, y, encabezado)
        canvas.drawText("Salida", 580f, y, encabezado)
        y += 8f
        canvas.drawLine(50f, y, 850f, y, linea)
        y += 24f

        var totalUnidades = 0
        detalles.forEach { d ->
            if (y > 1250f) return@forEach
            canvas.drawText(d.productoCodigo.take(16), 50f, y, texto)
            canvas.drawText(d.productoDescripcion.take(28), 180f, y, texto)
            canvas.drawText(d.categoria.take(10), 420f, y, texto)
            canvas.drawText(d.cantidad.toString(), 500f, y, texto)
            canvas.drawText(d.codigoSalida.take(12), 580f, y, texto)
            totalUnidades += d.cantidad
            y += 22f
        }

        y += 16f
        canvas.drawLine(50f, y, 850f, y, linea)
        y += 28f
        canvas.drawText("Líneas: ${detalles.size}", 50f, y, subtitulo)
        y += 24f
        canvas.drawText("Total unidades: $totalUnidades", 50f, y, subtitulo)

        if (vale.observacion.isNotBlank()) {
            y += 28f
            canvas.drawText("Observaciones:", 50f, y, encabezado)
            y += 22f
            canvas.drawText(vale.observacion.take(120), 50f, y, texto)
        }

        y += 80f
        canvas.drawLine(200f, y, 400f, y, linea)
        canvas.drawLine(500f, y, 700f, y, linea)
        y += 20f
        canvas.drawText("Entrega", 260f, y, texto)
        canvas.drawText("Recibe", 560f, y, texto)

        val fechaGen = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Generado: $fechaGen", 50f, 1320f, texto)

        pdf.finishPage(page)
        FileOutputStream(file).use { pdf.writeTo(it) }
        pdf.close()

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        context.startActivity(
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    } catch (e: Exception) {
        Toast.makeText(context, "Error PDF vale: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
