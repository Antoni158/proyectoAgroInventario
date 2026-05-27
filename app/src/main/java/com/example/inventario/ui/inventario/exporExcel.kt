package com.example.inventario.ui.inventario

import android.content.Context
import android.widget.Toast

import com.example.inventario.ui.export.ExportShareUtil

import com.example.inventario.data.bodega.Producto
import com.example.inventario.ui.branding.BrandingExports

import org.apache.poi.xssf.usermodel.XSSFWorkbook

import java.io.File
import java.io.FileOutputStream

fun exportarExcel(

    context: Context,

    productos: List<Producto>

) {

    try {

        // WORKBOOK

        val workbook =
            XSSFWorkbook()

        // HOJA

        val sheet =
            workbook.createSheet(
                "Inventario"
            )

        // ENCABEZADOS

        val headers = listOf(

            "Código",

            "Descripción",

            "Categoría",

            "Cantidad",

            "Unidad",

            "Ubicación",

            "Proveedor",

            "Costo Compra",

            "Centro costo",

            "Stock Mínimo",

            "Status",

            "Lote",

            "Fecha Ingreso",

            "Presupuesto",

            "Notas"
        )

        val startRow = BrandingExports.applyExcelBrandHeader(
            workbook = workbook,
            sheet = sheet,
            reportTitle = "Inventario de Productos",
            columnHeaders = headers,
            subtitle = "Inventario Agrícola"
        )

        // PRODUCTOS

        productos.forEachIndexed { index, producto ->
            val row = sheet.createRow(startRow + index)

            row.createCell(0)
                .setCellValue(
                    producto.codigo
                )

            row.createCell(1)
                .setCellValue(
                    producto.descripcion
                )

            row.createCell(2)
                .setCellValue(
                    producto.categoria
                )

            row.createCell(3)
                .setCellValue(
                    producto.cantidad.toDouble()
                )

            row.createCell(4)
                .setCellValue(
                    producto.unidad
                )

            row.createCell(5)
                .setCellValue(
                    producto.ubicacion
                )

            row.createCell(6)
                .setCellValue(
                    producto.proveedor
                )

            row.createCell(7)
                .setCellValue(
                    producto.costo
                )

            row.createCell(8)
                .setCellValue(
                    producto.centroCosto.ifBlank { producto.usoOperativo }
                )

            row.createCell(9)
                .setCellValue(
                    producto.stockMinimo.toDouble()
                )

            row.createCell(10)
                .setCellValue(
                    producto.status
                )

            row.createCell(11)
                .setCellValue(
                    producto.lote
                )

            row.createCell(12)
                .setCellValue(
                    producto.fechaIngreso
                )

            row.createCell(13)
                .setCellValue(
                    producto.presupuesto
                )

            row.createCell(14)
                .setCellValue(
                    producto.notas
                )
        }

        // TOTALES
        val totalRow = sheet.createRow(startRow + productos.size + 1)
        val totalStyle = workbook.createCellStyle().apply {
            val font = workbook.createFont().apply { bold = true }
            setFont(font)
        }
        totalRow.createCell(0).apply { setCellValue("TOTALES"); cellStyle = totalStyle }
        totalRow.createCell(3).apply {
            setCellValue(productos.sumOf { it.cantidad }.toDouble())
            cellStyle = totalStyle
        }
        totalRow.createCell(7).apply {
            setCellValue(productos.sumOf { it.costo * it.cantidad })
            cellStyle = totalStyle
        }
        totalRow.createCell(14).apply {
            setCellValue(productos.sumOf { it.presupuesto })
            cellStyle = totalStyle
        }

        // AJUSTAR COLUMNAS

        for (

        i in headers.indices

        ) {

            sheet.autoSizeColumn(i)
        }

        // ARCHIVO

        val file = File(

            context.cacheDir,

            "inventario_${
                System.currentTimeMillis()
            }.xlsx"
        )

        // GUARDAR

        val outputStream =

            FileOutputStream(file)

        workbook.write(
            outputStream
        )

        outputStream.flush()

        outputStream.close()

        workbook.close()

        ExportShareUtil.abrirExcel(context, file)

    } catch (

        e: Exception

    ) {

        e.printStackTrace()

        Toast.makeText(

            context,

            "Error al generar Excel: ${e.message}",

            Toast.LENGTH_LONG

        ).show()
    }
}