package com.example.inventario.ui.dashboard

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.inventario.data.bodega.Producto
import com.example.inventario.ui.branding.BrandingExports
import java.io.File
import java.io.FileOutputStream

fun exportarPedidoPDFLineas(
    context: Context,
    lineas: List<LineaPedido>
) {
    if (lineas.isEmpty()) return
    try {
        val file = File(context.cacheDir, "Pedido_${System.currentTimeMillis()}.pdf")
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(1200, 1800, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val encabezadoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 18f
            isFakeBoldText = true
        }
        val textoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 16f }
        val lineaPaint = Paint().apply { strokeWidth = 2f }

        var y = BrandingExports.drawPdfHeader(
            context = context,
            canvas = canvas,
            pageWidth = pageInfo.pageWidth,
            reportTitle = "Pedido de Productos",
            subtitle = "${lineas.size} artículo(s) · ${lineas.sumOf { it.cantidadPedido }} unidades totales"
        ) + 20f

        canvas.drawText("Código", 50f, y, encabezadoPaint)
        canvas.drawText("Producto", 220f, y, encabezadoPaint)
        canvas.drawText("Stock act.", 620f, y, encabezadoPaint)
        canvas.drawText("Pedir", 780f, y, encabezadoPaint)
        canvas.drawText("Costo est.", 930f, y, encabezadoPaint)
        y += 16f
        canvas.drawLine(40f, y, 1160f, y, lineaPaint)
        y += 36f

        var totalGeneral = 0.0
        lineas.forEach { linea ->
            val producto = linea.producto
            val cantidadPedido = linea.cantidadPedido
            val totalProducto = producto.costo * cantidadPedido
            totalGeneral += totalProducto

            canvas.drawText(producto.codigo, 50f, y, textoPaint)
            canvas.drawText(producto.descripcion.take(28), 220f, y, textoPaint)
            canvas.drawText("${producto.cantidad}/${producto.stockMinimo}", 620f, y, textoPaint)
            canvas.drawText(cantidadPedido.toString(), 800f, y, textoPaint)
            canvas.drawText("Q %.2f".format(totalProducto), 930f, y, textoPaint)
            y += 42f
        }

        y += 40f
        canvas.drawLine(40f, y, 1160f, y, lineaPaint)
        y += 48f
        canvas.drawText("TOTAL ESTIMADO:", 680f, y, encabezadoPaint)
        canvas.drawText("Q %.2f".format(totalGeneral), 950f, y, encabezadoPaint)

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
        e.printStackTrace()
    }
}

/** Compatibilidad: genera pedido con cantidades sugeridas automáticas. */
fun exportarPedidoPDF(context: Context, productos: List<Producto>) {
    exportarPedidoPDFLineas(
        context,
        productos.map { LineaPedido(it, StockPedidoUtil.cantidadSugeridaPedido(it)) }
    )
}
