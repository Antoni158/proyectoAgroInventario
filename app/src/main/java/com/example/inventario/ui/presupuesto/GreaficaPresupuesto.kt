package com.example.inventario.ui.presupuesto

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.inventario.viewModel.MesFinanciero

@Composable
fun GraficaPresupuesto(
    meses: List<MesFinanciero>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Comparación mensual (12 meses)", fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Leyenda(color = Color(0xFF4CAF50), texto = "Consumo")
                Leyenda(color = Color(0xFFE53935), texto = "Compras")
                Leyenda(color = Color(0xFF1565C0), texto = "Presupuesto")
            }
            val scroll = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scroll)
            ) {
                val anchoBarra = (meses.size.coerceAtLeast(1) * 48).dp
                Canvas(
                    modifier = Modifier
                        .width(anchoBarra)
                        .height(200.dp)
                ) {
                    if (meses.isEmpty()) return@Canvas
                    val max = meses.maxOf {
                        maxOf(it.ingresos, it.gastos, it.presupuestoMeta)
                    }.coerceAtLeast(1.0).toFloat()
                    val minH = size.height * 0.04f
                    val grupo = size.width / meses.size
                    val barWidth = grupo * 0.22f
                    val gap = barWidth * 0.15f

                    meses.forEachIndexed { index, mes ->
                        val xBase = index * grupo + gap
                        val ingH = ((mes.ingresos.toFloat() / max) * size.height * 0.85f).coerceAtLeast(
                            if (mes.ingresos > 0) minH else 0f
                        )
                        val gasH = ((mes.gastos.toFloat() / max) * size.height * 0.85f).coerceAtLeast(
                            if (mes.gastos > 0) minH else 0f
                        )
                        val metaH = ((mes.presupuestoMeta.toFloat() / max) * size.height * 0.85f).coerceAtLeast(
                            if (mes.presupuestoMeta > 0) minH else 0f
                        )
                        drawRoundRect(
                            color = Color(0xFF4CAF50),
                            topLeft = Offset(xBase, size.height - ingH),
                            size = Size(barWidth, ingH),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                        drawRoundRect(
                            color = Color(0xFFE53935),
                            topLeft = Offset(xBase + barWidth + gap / 2, size.height - gasH),
                            size = Size(barWidth, gasH),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                        drawRoundRect(
                            color = Color(0xFF1565C0),
                            topLeft = Offset(xBase + (barWidth + gap / 2) * 2, size.height - metaH),
                            size = Size(barWidth, metaH),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                    }
                }
                Row(
                    modifier = Modifier.width(anchoBarra),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    meses.forEach { Text(it.etiqueta, style = MaterialTheme.typography.labelSmall) }
                }
            }
        }
    }
}

@Composable
private fun Leyenda(color: Color, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, shape = RoundedCornerShape(2.dp))
        )
        Text(texto, style = MaterialTheme.typography.labelSmall)
    }
}
