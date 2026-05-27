package com.example.inventario.ui.Movimientos

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.inventario.data.bodega.Salida
import com.example.inventario.ui.branding.BrandingExports
import java.io.File
import java.io.FileOutputStream

fun exportarValePDF(
    context: Context,
    salida: Salida,
    etiquetaBodega: String = "Bodega"
) {
    try {
        val file = File(context.cacheDir, "Vale_${salida.codigoSalida}.pdf")
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(900, 1200, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val subtituloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 18f
            isFakeBoldText = true
        }
        val textoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 16f }
        val lineaPaint = Paint().apply { strokeWidth = 2f }

        var y = BrandingExports.drawPdfHeader(
            context = context,
            canvas = canvas,
            pageWidth = pageInfo.pageWidth,
            reportTitle = "Vale de Salida",
            subtitle = "$etiquetaBodega · ${salida.codigoSalida}"
        ) + 16f

        fun escribir(titulo: String, valor: String) {
            canvas.drawText("$titulo:", 70f, y, subtituloPaint)
            canvas.drawText(valor, 300f, y, textoPaint)
            y += 56f
        }

        escribir("Producto", salida.descripcion)
        escribir("Cantidad", salida.cantidad.toString())
        escribir("Destino", salida.destino)
        escribir("Responsable", salida.responsable)
        escribir("Quien lo lleva", salida.vehiculo)
        escribir("Fecha", salida.fechaSalida)
        if (salida.numeroVale.isNotBlank()) {
            escribir("N° Vale", salida.numeroVale)
        }
        if (salida.notas.isNotBlank()) {
            escribir("Notas", salida.notas)
        }

        y += 80f
        canvas.drawLine(250f, y, 650f, y, lineaPaint)
        canvas.drawText("Firma Responsable", 330f, y + 36f, textoPaint)

        pdfDocument.finishPage(page)

        FileOutputStream(file).use { pdfDocument.writeTo(it) }
        pdfDocument.close()

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
