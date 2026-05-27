package com.example.inventario.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventario.data.bodega.Producto
import com.example.inventario.ui.theme.BrandColors

@Composable
fun DashboardAlerts(
    productos: List<Producto>
) {
    val context = LocalContext.current
    val claves = remember(productos) { productos.map { it.codigo } }

    var cantidadesPedido by remember(claves) {
        mutableStateOf(
            productos.associate { p ->
                p.codigo to StockPedidoUtil.cantidadSugeridaPedido(p).toString()
            }
        )
    }

    LaunchedEffect(claves) {
        cantidadesPedido = productos.associate { p ->
            val actual = cantidadesPedido[p.codigo]
            p.codigo to (actual?.takeIf { it.isNotBlank() }
                ?: StockPedidoUtil.cantidadSugeridaPedido(p).toString())
        }
    }

    if (productos.isEmpty()) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Stock bajo",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Button(
                    onClick = {
                        val lineas = productos.mapNotNull { p ->
                            val cant = cantidadesPedido[p.codigo]?.toIntOrNull() ?: 0
                            if (cant > 0) LineaPedido(p, cant) else null
                        }
                        if (lineas.isNotEmpty()) {
                            exportarPedidoPDFLineas(context, lineas)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandColors.VerdePrincipal
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Generar pedido", fontWeight = FontWeight.SemiBold)
                }
            }

            Text(
                text = "Edite la cantidad a pedir en cada producto antes de generar el PDF.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            productos.forEach { producto ->
                val sugerido = StockPedidoUtil.cantidadSugeridaPedido(producto)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = producto.descripcion,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = StockPedidoUtil.textoEstadoStock(producto),
                        color = Color(0xFFC62828),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = cantidadesPedido[producto.codigo].orEmpty(),
                            onValueChange = { nuevo ->
                                val filtrado = nuevo.filter { it.isDigit() }
                                cantidadesPedido = cantidadesPedido + (producto.codigo to filtrado)
                            },
                            label = { Text("Pedir (uds)") },
                            modifier = Modifier.width(120.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            supportingText = { Text("Sugerido: $sugerido") }
                        )
                        TextButton(
                            onClick = {
                                cantidadesPedido = cantidadesPedido + (producto.codigo to sugerido.toString())
                            }
                        ) {
                            Text("Restaurar")
                        }
                    }
                }
                if (producto != productos.last()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}

data class LineaPedido(
    val producto: Producto,
    val cantidadPedido: Int
)
