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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DashboardDonutChart(

    normales: Int,
    bajos: Int,
    criticos: Int

) {

    val total =
        normales + bajos + criticos

    val normalAngle =
        if (total == 0)
            0f
        else
            (normales.toFloat() / total) * 360f

    val bajoAngle =
        if (total == 0)
            0f
        else
            (bajos.toFloat() / total) * 360f

    val criticoAngle =
        if (total == 0)
            0f
        else
            (criticos.toFloat() / total) * 360f

    Card(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(24.dp),

        colors =
            CardDefaults.cardColors(

                containerColor =
                    Color(0xFF16161F)
            )

    ) {

        Column(

            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),

            horizontalAlignment =
                Alignment.CenterHorizontally

        ) {

            Text(

                text =
                    "Estado Inventario",

                color =
                    Color.White,

                fontWeight =
                    FontWeight.Bold,

                fontSize =
                    20.sp
            )

            Spacer(
                modifier =
                    Modifier.height(24.dp)
            )

            Canvas(

                modifier =
                    Modifier.size(240.dp)

            ) {

                val strokeWidth = 70f

                drawArc(

                    color =
                        Color(0xFF4CAF50),

                    startAngle =
                        -90f,

                    sweepAngle =
                        normalAngle,

                    useCenter =
                        false,

                    style =
                        Stroke(

                            width =
                                strokeWidth,

                            cap =
                                StrokeCap.Round
                        ),

                    size =
                        Size(
                            size.width,
                            size.height
                        )
                )

                drawArc(

                    color =
                        Color(0xFFFF9800),

                    startAngle =
                        -90f + normalAngle,

                    sweepAngle =
                        bajoAngle,

                    useCenter =
                        false,

                    style =
                        Stroke(

                            width =
                                strokeWidth,

                            cap =
                                StrokeCap.Round
                        ),

                    size =
                        Size(
                            size.width,
                            size.height
                        )
                )

                drawArc(

                    color =
                        Color.Red,

                    startAngle =
                        -90f +
                                normalAngle +
                                bajoAngle,

                    sweepAngle =
                        criticoAngle,

                    useCenter =
                        false,

                    style =
                        Stroke(

                            width =
                                strokeWidth,

                            cap =
                                StrokeCap.Round
                        ),

                    size =
                        Size(
                            size.width,
                            size.height
                        )
                )
            }

            Spacer(
                modifier =
                    Modifier.height(20.dp)
            )

            Row(

                horizontalArrangement =
                    Arrangement.spacedBy(20.dp)

            ) {

                LegendDonutItem(
                    Color(0xFF4CAF50),
                    "Normal",
                    normales
                )

                LegendDonutItem(
                    Color(0xFFFF9800),
                    "Bajo",
                    bajos
                )

                LegendDonutItem(
                    Color.Red,
                    "Crítico",
                    criticos
                )
            }
        }
    }
}

@Composable
fun LegendDonutItem(

    color: Color,
    titulo: String,
    cantidad: Int

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

            text =
                "$titulo ($cantidad)",

            color =
                Color.White
        )
    }
}