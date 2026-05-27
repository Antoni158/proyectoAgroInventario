package com.example.inventario.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DashboardCharts(
    entradas: Int,
    salidas: Int,
    facturas: Int,
    stockCritico: Int
) {
    val maximo = maxOf(entradas, salidas, facturas, stockCritico, 1)
    val surface = MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            Text(
                text = "Movimientos del inventario",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )

            Spacer(modifier = Modifier.height(26.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                BarraMovimientoChart(
                    valor = entradas,
                    max = maximo,
                    titulo = "Entradas",
                    color1 = Color(0xFF2E7D32),
                    color2 = Color(0xFF66BB6A),
                    trackColor = surface
                )
                BarraMovimientoChart(
                    valor = salidas,
                    max = maximo,
                    titulo = "Salidas",
                    color1 = Color(0xFF7B1FA2),
                    color2 = Color(0xFFBA68C8),
                    trackColor = surface
                )
                BarraMovimientoChart(
                    valor = facturas,
                    max = maximo,
                    titulo = "Facturas",
                    color1 = Color(0xFFF9A825),
                    color2 = Color(0xFFFFCA28),
                    trackColor = surface
                )
                BarraMovimientoChart(
                    valor = stockCritico,
                    max = maximo,
                    titulo = "Crítico",
                    color1 = Color(0xFF1565C0),
                    color2 = Color(0xFF4FC3F7),
                    trackColor = surface
                )
            }
        }
    }
}

@Composable
fun BarraMovimientoChart(
    valor: Int,
    max: Int,
    titulo: String,
    color1: Color,
    color2: Color,
    trackColor: Color
) {
    val porcentaje = if (max == 0) 0f else valor.toFloat() / max.toFloat()
    val minVisible = if (valor == 0) 0.06f else porcentaje.coerceIn(0.08f, 1f)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = valor.toString(),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .width(56.dp)
                .height(180.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Canvas(
                modifier = Modifier
                    .width(56.dp)
                    .height(180.dp)
            ) {
                val barWidth = size.width * 0.72f
                val left = (size.width - barWidth) / 2f
                // Pista de fondo
                drawRoundRect(
                    color = trackColor.copy(alpha = 0.55f),
                    topLeft = Offset(left, 0f),
                    size = Size(barWidth, size.height),
                    cornerRadius = CornerRadius(28f, 28f)
                )
                val barHeight = size.height * minVisible
                drawRoundRect(
                    brush = Brush.verticalGradient(listOf(color1, color2)),
                    topLeft = Offset(left, size.height - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(28f, 28f)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = titulo,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
        )
    }
}
