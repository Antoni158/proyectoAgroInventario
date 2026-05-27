package com.example.inventario.ui.presupuesto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventario.viewModel.ResumenFinancieroState

@Composable
fun ResumenFinanciero(
    state: ResumenFinancieroState,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            "Resumen financiero",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FinCard(
                titulo = "Ingresos",
                valor = "Q %.2f".format(state.ingresosTotales),
                icon = Icons.Default.ArrowUpward,
                color = Color(0xFF2E7D32),
                modifier = Modifier.weight(1f)
            )
            FinCard(
                titulo = "Egresos",
                valor = "Q %.2f".format(state.egresosTotales),
                icon = Icons.Default.ArrowDownward,
                color = Color(0xFFC62828),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FinCard(
                titulo = "Utilidad",
                valor = "Q %.2f".format(state.utilidad),
                icon = Icons.Default.TrendingUp,
                color = if (state.utilidad >= 0) Color(0xFF1565C0) else Color(0xFFC62828),
                modifier = Modifier.weight(1f)
            )
            FinCard(
                titulo = "Consumo operativo",
                valor = "Q %.2f".format(state.consumoOperativo),
                icon = Icons.Default.ShoppingCart,
                color = Color(0xFF6A1B9A),
                modifier = Modifier.weight(1f)
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            )
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountBalance, contentDescription = null)
                    Text("Presupuesto y balance", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
                }
                FinRow("Inicial", "Q %.2f".format(state.presupuestoInicial))
                if (state.presupuestoMetaPeriodo > 0) {
                    FinRow(
                        "Meta ${state.tipoPeriodoActivo.lowercase()} ${state.anioActivo}",
                        "Q %.2f".format(state.presupuestoMetaPeriodo)
                    )
                }
                FinRow("Actual (inventario)", "Q %.2f".format(state.presupuestoActual))
                FinRow("Proyectado final", "Q %.2f".format(state.presupuestoFinal))
                FinRow("Balance", "Q %.2f".format(state.balance))
                Text("Progreso financiero", style = MaterialTheme.typography.labelMedium)
                LinearProgressIndicator(
                    progress = { state.progresoPct / 100f },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text("%.0f%% del presupuesto inicial".format(state.progresoPct), fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun FinCard(
    titulo: String,
    valor: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, contentDescription = null, tint = color)
            Text(valor, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = color)
            Text(titulo, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun FinRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}
