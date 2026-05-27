package com.example.inventario.ui.Kardex




import android.content.Context
import android.content.Intent

import android.graphics.Paint
import android.graphics.pdf.PdfDocument

import android.widget.Toast

import androidx.core.content.FileProvider
import com.example.inventario.data.bodega.Kardex
import com.example.inventario.ui.branding.BrandingExports


import java.io.File
import java.io.FileOutputStream

import java.text.SimpleDateFormat

import java.util.Date
import java.util.Locale

fun exportarKardexPDF(
    context: Context,
    movimientos: List<Kardex>,
    etiquetaBodega: String = "Bodega"
) {

    val pdfDocument =
        PdfDocument()

    val pageInfo =

        PdfDocument.PageInfo
            .Builder(

                1800,
                2200,
                1

            ).create()

    val page =

        pdfDocument
            .startPage(pageInfo)

    val canvas =
        page.canvas

    val tituloPaint =
        Paint().apply {

            textSize = 35f

            isFakeBoldText = true
        }

    val textoPaint =
        Paint().apply {

            textSize = 15f
        }

    val encabezadoPaint =
        Paint().apply {

            textSize = 15f

            isFakeBoldText = true
        }

    val lineaPaint =
        Paint().apply {

            strokeWidth = 1f
        }

    var y = BrandingExports.drawPdfHeader(
        context, canvas, pageInfo.pageWidth,
        "Reporte Kardex",
        "Bodega: $etiquetaBodega"
    ) + 12f

    // ENCABEZADOS

    canvas.drawText(
        "Código",
        40f,
        y,
        encabezadoPaint
    )

    canvas.drawText(
        "Producto",
        160f,
        y,
        encabezadoPaint
    )

    canvas.drawText(
        "Tipo",
        400f,
        y,
        encabezadoPaint
    )

    canvas.drawText(
        "Cantidad",
        520f,
        y,
        encabezadoPaint
    )

    canvas.drawText(
        "Saldo Ant.",
        650f,
        y,
        encabezadoPaint
    )

    canvas.drawText(
        "Saldo Nuevo",
        800f,
        y,
        encabezadoPaint
    )

    canvas.drawText(
        "Costo",
        980f,
        y,
        encabezadoPaint
    )

    canvas.drawText(
        "Total",
        1080f,
        y,
        encabezadoPaint
    )

    canvas.drawText(
        "Usuario",
        1200f,
        y,
        encabezadoPaint
    )

    canvas.drawText(
        "Fecha",
        1380f,
        y,
        encabezadoPaint
    )

    canvas.drawText(
        "Factura",
        1520f,
        y,
        encabezadoPaint
    )

    y += 10f

    canvas.drawLine(
        40f,
        y,
        1750f,
        y,
        lineaPaint
    )

    y += 35f

    movimientos.forEach {

            movimiento ->

        canvas.drawText(
            movimiento.codigoProducto,
            40f,
            y,
            textoPaint
        )

        canvas.drawText(
            movimiento.descripcion.take(20),
            160f,
            y,
            textoPaint
        )

        canvas.drawText(
            movimiento.tipoMovimiento,
            400f,
            y,
            textoPaint
        )

        canvas.drawText(
            movimiento.cantidad.toString(),
            520f,
            y,
            textoPaint
        )

        canvas.drawText(
            movimiento.saldoAnterior.toString(),
            650f,
            y,
            textoPaint
        )

        canvas.drawText(
            movimiento.saldoNuevo.toString(),
            800f,
            y,
            textoPaint
        )

        canvas.drawText(
            "Q ${movimiento.costoUnitario}",
            980f,
            y,
            textoPaint
        )

        canvas.drawText(
            "Q ${movimiento.totalMovimiento}",
            1080f,
            y,
            textoPaint
        )

        canvas.drawText(
            movimiento.usuario.take(15),
            1200f,
            y,
            textoPaint
        )

        canvas.drawText(
            movimiento.fechaMovimiento,
            1380f,
            y,
            textoPaint
        )

        canvas.drawText(
            movimiento.numeroFactura.take(10),
            1520f,
            y,
            textoPaint
        )

        y += 30f
    }

    pdfDocument.finishPage(page)

    val file = File(

        context.cacheDir,

        "kardex_${System.currentTimeMillis()}.pdf"
    )

    try {

        val os =
            FileOutputStream(file)

        pdfDocument.writeTo(os)

        os.close()

        pdfDocument.close()

        val uri =

            FileProvider.getUriForFile(

                context,

                "${context.packageName}.fileprovider",

                file
            )

        context.startActivity(

            Intent(
                Intent.ACTION_VIEW
            ).apply {

                setDataAndType(

                    uri,

                    "application/pdf"
                )

                addFlags(

                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                            or
                            Intent.FLAG_ACTIVITY_NEW_TASK
                )
            }
        )

    } catch (

        e: Exception

    ) {

        e.printStackTrace()

        Toast.makeText(

            context,

            "Error PDF: ${e.message}",

            Toast.LENGTH_LONG

        ).show()
    }
}