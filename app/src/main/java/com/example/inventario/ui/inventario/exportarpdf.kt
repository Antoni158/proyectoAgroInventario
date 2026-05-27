package com.example.inventario.ui.inventario

import android.content.Context
import android.content.Intent

import android.graphics.Paint
import android.graphics.pdf.PdfDocument

import android.widget.Toast

import androidx.core.content.FileProvider

import com.example.inventario.data.bodega.Producto
import com.example.inventario.ui.branding.BrandingExports

import java.io.File
import java.io.FileOutputStream

import java.text.SimpleDateFormat

import java.util.Date
import java.util.Locale

fun exportarInventarioPDF(

    context: Context,

    productos: List<Producto>,

    bodega: String

) {

    try {

        // PDF

        val pdfDocument =
            PdfDocument()

        // PAGINA

        val pageInfo =

            PdfDocument.PageInfo.Builder(

                1800,

                2200,

                1

            ).create()

        val page =
            pdfDocument.startPage(
                pageInfo
            )

        val canvas =
            page.canvas

        // PAINTS

        val tituloPaint = Paint().apply {

            textSize = 36f

            isFakeBoldText = true
        }

        val subtituloPaint = Paint().apply {

            textSize = 18f

            isFakeBoldText = true
        }

        val encabezadoPaint = Paint().apply {

            textSize = 15f

            isFakeBoldText = true
        }

        val textoPaint = Paint().apply {

            textSize = 14f
        }

        val lineaPaint = Paint().apply {

            strokeWidth = 1.5f
        }

        val contentStart = BrandingExports.drawPdfHeader(
            context = context,
            canvas = canvas,
            pageWidth = pageInfo.pageWidth,
            reportTitle = "Reporte General Inventario",
            subtitle = "Bodega: $bodega"
        )

        // ENCABEZADOS

        var y = contentStart + 12f

        canvas.drawText(
            "Código",
            40f,
            y,
            encabezadoPaint
        )

        canvas.drawText(
            "Descripción",
            160f,
            y,
            encabezadoPaint
        )

        canvas.drawText(
            "Categoría",
            360f,
            y,
            encabezadoPaint
        )

        canvas.drawText(
            "Cant.",
            500f,
            y,
            encabezadoPaint
        )

        canvas.drawText(
            "Unidad",
            580f,
            y,
            encabezadoPaint
        )

        canvas.drawText(
            "Status",
            690f,
            y,
            encabezadoPaint
        )

        canvas.drawText(
            "Lote",
            820f,
            y,
            encabezadoPaint
        )

        canvas.drawText(
            "Ubicación",
            930f,
            y,
            encabezadoPaint
        )

        canvas.drawText(
            "Proveedor",
            1080f,
            y,
            encabezadoPaint
        )

        canvas.drawText(
            "Costo",
            1240f,
            y,
            encabezadoPaint
        )

        canvas.drawText(
            "C. costo",
            1340f,
            y,
            encabezadoPaint
        )

        canvas.drawText(
            "Presupuesto",
            1470f,
            y,
            encabezadoPaint
        )

        canvas.drawText(
            "Fecha",
            1640f,
            y,
            encabezadoPaint
        )

        // LINEA

        y += 10f

        canvas.drawLine(

            40f,

            y,

            1760f,

            y,

            lineaPaint
        )

        y += 35f

        // TOTAL

        var totalInventario =
            0.0

        // PRODUCTOS

        productos.forEach {

                p ->

            val totalProducto =

                p.cantidad *
                        p.costo

            totalInventario +=
                totalProducto

            canvas.drawText(

                p.codigo,

                40f,

                y,

                textoPaint
            )

            canvas.drawText(

                p.descripcion.take(22),

                160f,

                y,

                textoPaint
            )

            canvas.drawText(

                p.categoria.take(14),

                360f,

                y,

                textoPaint
            )

            canvas.drawText(

                p.cantidad.toString(),

                500f,

                y,

                textoPaint
            )

            canvas.drawText(

                p.unidad.take(8),

                580f,

                y,

                textoPaint
            )

            canvas.drawText(

                p.status.take(12),

                690f,

                y,

                textoPaint
            )

            canvas.drawText(

                p.lote.take(10),

                820f,

                y,

                textoPaint
            )

            canvas.drawText(

                p.ubicacion.take(12),

                930f,

                y,

                textoPaint
            )

            canvas.drawText(

                p.proveedor.take(12),

                1080f,

                y,

                textoPaint
            )

            canvas.drawText(

                "Q ${
                    String.format(
                        Locale.US,
                        "%.2f",
                        p.costo
                    )
                }",

                1240f,

                y,

                textoPaint
            )

            canvas.drawText(
                (p.centroCosto.ifBlank { p.usoOperativo }).take(12),
                1340f,
                y,
                textoPaint
            )

            canvas.drawText(

                "Q ${
                    String.format(
                        Locale.US,
                        "%.2f",
                        p.presupuesto
                    )
                }",

                1470f,

                y,

                textoPaint
            )

            canvas.drawText(

                p.fechaIngreso.take(10),

                1640f,

                y,

                textoPaint
            )

            y += 30f
        }

        // TOTAL FINAL

        y += 20f

        canvas.drawLine(

            40f,

            y,

            1760f,

            y,

            lineaPaint
        )

        y += 45f

        val totalPaint = Paint().apply {

            textSize = 24f

            isFakeBoldText = true
        }

        canvas.drawText(

            "VALOR TOTAL INVENTARIO: Q ${
                String.format(
                    Locale.US,
                    "%.2f",
                    totalInventario
                )
            }",

            1100f,

            y,

            totalPaint
        )

        // FINALIZAR

        pdfDocument.finishPage(
            page
        )

        // ARCHIVO

        val file = File(

            context.cacheDir,

            "inventario_${
                System.currentTimeMillis()
            }.pdf"
        )

        val outputStream =

            FileOutputStream(file)

        pdfDocument.writeTo(
            outputStream
        )

        outputStream.flush()

        outputStream.close()

        pdfDocument.close()

        // URI

        val uri =

            FileProvider.getUriForFile(

                context,

                "${context.packageName}.fileprovider",

                file
            )

        // INTENT

        val intent = Intent(

            Intent.ACTION_VIEW

        ).apply {

            setDataAndType(

                uri,

                "application/pdf"
            )

            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )
        }

        context.startActivity(

            Intent.createChooser(

                intent,

                "Abrir PDF"
            )
        )

        Toast.makeText(

            context,

            "PDF generado correctamente",

            Toast.LENGTH_LONG

        ).show()

    } catch (

        e: Exception

    ) {

        e.printStackTrace()

        Toast.makeText(

            context,

            "Error al generar PDF: ${e.message}",

            Toast.LENGTH_LONG

        ).show()
    }
}