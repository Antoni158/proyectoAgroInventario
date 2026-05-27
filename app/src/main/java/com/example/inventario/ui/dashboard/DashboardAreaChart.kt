package com.example.inventario.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DashboardAreaChart(

    entradas: List<Float>,
    salidas: List<Float>,
    stock: List<Float>,
    facturas: List<Float>

) {

    val datosEntradas = DashboardChartUtil.normalizarSerie(
        DashboardChartUtil.rellenarSerie(if (entradas.isEmpty()) listOf(0f) else entradas)
    )
    val datosSalidas = DashboardChartUtil.normalizarSerie(
        DashboardChartUtil.rellenarSerie(if (salidas.isEmpty()) listOf(0f) else salidas)
    )
    val datosStock = DashboardChartUtil.normalizarSerie(
        DashboardChartUtil.rellenarSerie(if (stock.isEmpty()) listOf(0f) else stock)
    )
    val datosFacturas = DashboardChartUtil.normalizarSerie(
        DashboardChartUtil.rellenarSerie(if (facturas.isEmpty()) listOf(0f) else facturas)
    )

    Card(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(28.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color(0xFF111118)
            )

    ) {

        Column(

            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),

            verticalArrangement =
                Arrangement.spacedBy(18.dp)

        ) {

            Text(

                text = "Análisis General",

                color = Color.White,

                fontWeight =
                    FontWeight.Bold,

                fontSize = 24.sp
            )

            Text(
                text = "Comparación por módulos · tendencias normalizadas",
                color = Color.LightGray
            )

            Canvas(

                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)

            ) {

                val maximo = 1f

                val espacioX =
                    size.width /
                            (
                                    datosEntradas.size
                                        .coerceAtLeast(2) - 1
                                    )

                fun dibujarAreaChart(

                    datos: List<Float>,
                    color1: Color,
                    color2: Color

                ) {

                    val linea =
                        Path()

                    val area =
                        Path()

                    datos.forEachIndexed {

                            index,
                            valor ->

                        val x =
                            index * espacioX

                        val y =
                            size.height -
                                    (
                                            valor /
                                                    maximo
                                            ) * size.height

                        if (index == 0) {

                            linea.moveTo(x, y)

                            area.moveTo(
                                x,
                                size.height
                            )

                            area.lineTo(x, y)

                        } else {

                            linea.lineTo(x, y)

                            area.lineTo(x, y)
                        }
                    }

                    area.lineTo(
                        size.width,
                        size.height
                    )

                    area.close()

                    drawPath(

                        path = area,

                        brush =
                            Brush.verticalGradient(

                                colors = listOf(

                                    color1.copy(
                                        alpha = 0.4f
                                    ),

                                    Color.Transparent
                                )
                            ),

                        style = Fill
                    )

                    drawPath(

                        path = linea,

                        brush =
                            Brush.horizontalGradient(

                                colors = listOf(
                                    color1,
                                    color2
                                )
                            ),

                        style =
                            Stroke(
                                width = 8f
                            )
                    )

                    datos.forEachIndexed {

                            index,
                            valor ->

                        val x =
                            index * espacioX

                        val y =
                            size.height -
                                    (
                                            valor /
                                                    maximo
                                            ) * size.height

                        drawCircle(

                            color = color2,

                            radius = 10f,

                            center =
                                Offset(x, y)
                        )
                    }
                }

                dibujarAreaChart(

                    datosEntradas,

                    Color(0xFF7B2FF7),

                    Color(0xFFB06CFF)
                )

                dibujarAreaChart(

                    datosSalidas,

                    Color(0xFFFF416C),

                    Color(0xFFFF4B2B)
                )

                dibujarAreaChart(

                    datosStock,

                    Color(0xFF00C853),

                    Color(0xFF69F0AE)
                )

                dibujarAreaChart(

                    datosFacturas,

                    Color(0xFFFFAB00),

                    Color(0xFFFFD54F)
                )
            }

            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceEvenly

            ) {

                LegendAreaItem(
                    "Entradas",
                    Color(0xFFB06CFF)
                )

                LegendAreaItem(
                    "Salidas",
                    Color(0xFFFF4B2B)
                )

                LegendAreaItem(
                    "Stock",
                    Color(0xFF69F0AE)
                )

                LegendAreaItem(
                    "Facturas",
                    Color(0xFFFFD54F)
                )
            }
        }
    }
}

@Composable
fun LegendAreaItem(

    titulo: String,
    color: Color

) {

    Row(

        verticalAlignment =
            Alignment.CenterVertically

    ) {

        Box(

            modifier = Modifier
                .size(14.dp)
                .background(
                    color,
                    RoundedCornerShape(50)
                )
        )

        Spacer(
            modifier =
                Modifier.width(6.dp)
        )

        Text(

            text = titulo,

            color = Color.White
        )
    }
}