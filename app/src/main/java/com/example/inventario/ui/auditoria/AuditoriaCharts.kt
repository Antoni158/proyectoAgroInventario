package com.example.inventario.ui.auditoria

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class AuditoriaChartData(
    val exactos: Int = 0,
    val faltantes: Int = 0,
    val sobrantes: Int = 0,
    val exactitudPct: Int = 0
)

data class CategoriaAuditoriaResumen(
    val categoria: String,
    val exactos: Int,
    val faltantes: Int,
    val sobrantes: Int
)

@Composable
fun AuditoriaResumenCharts(
    data: AuditoriaChartData,
    porCategoria: List<CategoriaAuditoriaResumen>,
    modifier: Modifier = Modifier
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AuditoriaDonutChart(
                data = data,
                modifier = Modifier.weight(1f)
            )
            AuditoriaBarChart(
                items = porCategoria.take(5),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun AuditoriaDonutChart(
    data: AuditoriaChartData,
    modifier: Modifier = Modifier
) {
    val total = (data.exactos + data.faltantes + data.sobrantes).coerceAtLeast(1)
    val exactoAngle = data.exactos.toFloat() / total * 360f
    val faltanteAngle = data.faltantes.toFloat() / total * 360f
    val sobranteAngle = data.sobrantes.toFloat() / total * 360f
    val colorExacto = Color(0xFF2E7D32)
    val colorFaltante = Color(0xFFC62828)
    val colorSobrante = Color(0xFFEF6C00)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Distribución", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
            Text("${data.exactitudPct}% exactitud", color = MaterialTheme.colorScheme.primary)
            Canvas(Modifier.height(120.dp).fillMaxWidth().padding(8.dp)) {
                val stroke = 28f
                val diameter = size.minDimension - stroke
                val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
                var start = -90f
                if (exactoAngle > 0) {
                    drawArc(colorExacto, start, exactoAngle, false, topLeft, Size(diameter, diameter), style = Stroke(stroke, cap = StrokeCap.Butt))
                    start += exactoAngle
                }
                if (faltanteAngle > 0) {
                    drawArc(colorFaltante, start, faltanteAngle, false, topLeft, Size(diameter, diameter), style = Stroke(stroke, cap = StrokeCap.Butt))
                    start += faltanteAngle
                }
                if (sobranteAngle > 0) {
                    drawArc(colorSobrante, start, sobranteAngle, false, topLeft, Size(diameter, diameter), style = Stroke(stroke, cap = StrokeCap.Butt))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LeyendaItem("Exacto", data.exactos, colorExacto)
                LeyendaItem("Falt.", data.faltantes, colorFaltante)
                LeyendaItem("Sobr.", data.sobrantes, colorSobrante)
            }
        }
    }
}

@Composable
private fun LeyendaItem(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(Modifier.height(8.dp).fillMaxWidth(0.15f)) {
            drawRoundRect(color, cornerRadius = CornerRadius(4f, 4f))
        }
        Text("$label $count", style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun AuditoriaBarChart(
    items: List<CategoriaAuditoriaResumen>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(Modifier.padding(12.dp)) {
            Text("Por categoría", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
            if (items.isEmpty()) {
                Text("Sin datos", style = MaterialTheme.typography.bodySmall)
                return@Column
            }
            val maxTotal = items.maxOf { it.exactos + it.faltantes + it.sobrantes }.coerceAtLeast(1)
            Canvas(Modifier.height(120.dp).fillMaxWidth().padding(top = 8.dp)) {
                val barWidth = size.width / (items.size * 2f).coerceAtLeast(1f)
                items.forEachIndexed { i, item ->
                    val x = i * barWidth * 2 + barWidth * 0.3f
                    val hExacto = (item.exactos.toFloat() / maxTotal) * size.height * 0.85f
                    val hFaltante = (item.faltantes.toFloat() / maxTotal) * size.height * 0.85f
                    var y = size.height
                    if (hFaltante > 0) {
                        y -= hFaltante
                        drawRect(Color(0xFFC62828), Offset(x, y), Size(barWidth * 0.7f, hFaltante))
                    }
                    if (hExacto > 0) {
                        y -= hExacto
                        drawRect(Color(0xFF2E7D32), Offset(x, y), Size(barWidth * 0.7f, hExacto))
                    }
                    val hSobrante = (item.sobrantes.toFloat() / maxTotal) * size.height * 0.85f
                    if (hSobrante > 0) {
                        y -= hSobrante
                        drawRect(Color(0xFFEF6C00), Offset(x, y), Size(barWidth * 0.7f, hSobrante))
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                items.forEach { item ->
                    Text(
                        item.categoria.take(4),
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun AuditoriaComparativaChart(
    sistema: Float,
    fisico: Float,
    modifier: Modifier = Modifier
) {
    val colorSistema = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
    val colorFisico = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text("Sistema vs Físico (promedio)", fontWeight = FontWeight.Bold)
            Canvas(Modifier.height(80.dp).fillMaxWidth().padding(top = 8.dp)) {
                val max = maxOf(sistema, fisico, 1f)
                val w = size.width * 0.35f
                drawRect(
                    colorSistema,
                    Offset(size.width * 0.15f, size.height - (sistema / max) * size.height * 0.8f),
                    Size(w, (sistema / max) * size.height * 0.8f)
                )
                drawRect(
                    colorFisico,
                    Offset(size.width * 0.55f, size.height - (fisico / max) * size.height * 0.8f),
                    Size(w, (fisico / max) * size.height * 0.8f)
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Text("Sistema: ${sistema.toInt()}", style = MaterialTheme.typography.labelSmall)
                Text("Físico: ${fisico.toInt()}", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
