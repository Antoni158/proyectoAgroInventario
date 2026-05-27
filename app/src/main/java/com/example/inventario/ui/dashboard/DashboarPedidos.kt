package com.example.inventario.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.inventario.data.bodega.Producto

import androidx.compose.material3.MaterialTheme
import com.example.inventario.ui.theme.BrandColors

@Composable
fun DashboardPedidos(
    productos: List<Producto>
) {
    val context = LocalContext.current
    if (productos.isEmpty()) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1B3022), // Verde bosque muy oscuro
                            Color(0xFF0D1A12)  // Casi negro verdoso
                        )
                    )
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // TITULO
            Text(
                text = "Pedidos Automáticos",
                color = BrandColors.VerdeClaro,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                productos.take(8).forEach { producto ->
                    var cantidad by remember {
                        mutableStateOf((producto.stockMinimo * 2).toString())
                    }

                    val total = producto.costo * (cantidad.toIntOrNull() ?: 0)

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0x1AFFFFFF) // Glassmorphism efecto
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // PRODUCTO
                            Text(
                                text = producto.descripcion,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )

                            // DATOS
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Text(
                                    text = "Actual: ${producto.cantidad}",
                                    color = Color(0xFFB9D4BC), // Verde pálido para contraste
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Mínimo: ${producto.stockMinimo}",
                                    color = Color(0xFFB9D4BC),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            // CANTIDAD
                            OutlinedTextField(
                                value = cantidad,
                                onValueChange = { cantidad = it },
                                label = { Text("Cantidad a pedir", color = Color.LightGray) },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = BrandColors.VerdeClaro,
                                    unfocusedBorderColor = Color.Gray,
                                    focusedLabelColor = BrandColors.VerdeClaro,
                                    unfocusedLabelColor = Color.LightGray
                                )
                            )

                            // TOTAL
                            Text(
                                text = "Total estimado: Q %.2f".format(total),
                                color = Color(0xFF81C784), // Verde brillante legible
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // BOTON PDF
            Button(
                onClick = { exportarPedidoPDF(context, productos) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandColors.VerdePrincipal,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Generar Pedido PDF",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
