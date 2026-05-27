package com.example.inventario.data.estadistica


import com.example.inventario.data.bodega.Producto

fun calcularDiasRestantes(

    producto: Producto,

    consumoPromedioDiario: Int

): Int {

    if (

        consumoPromedioDiario <= 0

    ) {

        return 999
    }

    return (

            producto.cantidad /
                    consumoPromedioDiario
            )
        .coerceAtLeast(0)
}

fun calcularConsumoPromedio(

    salidasUltimosDias: List<Int>

): Int {

    if (

        salidasUltimosDias.isEmpty()

    ) {

        return 0
    }

    return (

            salidasUltimosDias.sum() /
                    salidasUltimosDias.size
            )
}

fun obtenerEstadoPrediccion(

    diasRestantes: Int

): String {

    return when {

        diasRestantes <= 3 ->

            "CRÍTICO"

        diasRestantes <= 7 ->

            "BAJO"

        diasRestantes <= 15 ->

            "MEDIO"

        else ->

            "NORMAL"
    }
}