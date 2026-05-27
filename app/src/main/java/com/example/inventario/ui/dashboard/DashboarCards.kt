package com.example.inventario.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class DashboardCardItem(
    val titulo: String,
    val valor: String,
    val colores: List<Color>,
    val icono: ImageVector
)

@Composable
fun DashboardCards(
    productos: Int,
    stockBajo: Int,
    existencias: Int,
    presupuesto: Double,
    totalVales: Int = 0
) {
    val datos = listOf(
        DashboardCardItem(
            "Productos", productos.toString(),
            listOf(Color(0xFF2979FF), Color(0xFF651FFF)),
            Icons.Default.Inventory
        ),
        DashboardCardItem(
            "Stock Bajo", stockBajo.toString(),
            listOf(Color(0xFFD50000), Color(0xFF000000)),
            Icons.Default.Warning
        ),
        DashboardCardItem(
            "Existencias", existencias.toString(),
            listOf(Color(0xFF00C853), Color(0xFF1B5E20)),
            Icons.Default.Warehouse
        ),
        DashboardCardItem(
            "Presupuesto", "Q %.0f".format(presupuesto),
            listOf(Color(0xFFAA00FF), Color(0xFF311B92)),
            Icons.Default.AttachMoney
        ),
        DashboardCardItem(
            "Vales", totalVales.toString(),
            listOf(Color(0xFFFF6D00), Color(0xFFE65100)),
            Icons.Default.LocalShipping
        )
    )

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        datos.chunked(2).forEach { fila ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                fila.forEach { item ->
                    DashboardCardCell(item = item, modifier = Modifier.weight(1f))
                }
                if (fila.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DashboardCardCell(item: DashboardCardItem, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp))
                .background(brush = Brush.linearGradient(colors = item.colores))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(imageVector = item.icono, contentDescription = item.titulo, tint = Color.White)
                Column {
                    Text(
                        text = item.valor,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = item.titulo, color = Color.White, fontSize = 14.sp)
                }
            }
        }
    }
}
