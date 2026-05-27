package com.example.inventario.ui.StockBajo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.inventario.ui.components.BodegaScrollableListScaffold
import com.example.inventario.ui.dashboard.StockPedidoUtil
import com.example.inventario.viewModel.ProductoViewModel
import com.example.inventario.viewModel.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockBajoScreen(
    navController: NavController,
    bodegaId: String
) {
    val productoViewModel: ProductoViewModel = viewModel()
    val productos by productoViewModel
        .obtenerProductos(bodegaId)
        .collectAsState(initial = emptyList())

    val productosStockBajo = productos.filter { StockPedidoUtil.esStockBajo(it) }
    val productosCriticos = productos.filter { it.cantidad <= 5 }

    val context = LocalContext.current
    val header = com.example.inventario.ui.components.rememberBodegaHeader(bodegaId)

    BodegaScrollableListScaffold(
        titulo = "Stock Inteligente",
        bodegaId = bodegaId,
        navController = navController
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StockCardKPI(
                    titulo = "Stock Bajo",
                    valor = productosStockBajo.size.toString(),
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                )
                StockCardKPI(
                    titulo = "Críticos",
                    valor = productosCriticos.size.toString(),
                    color = Color.Red,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Text(
                text = "Productos en Riesgo",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        exportarStockBajoPDF(
                            context,
                            productosStockBajo,
                            com.example.inventario.ui.components.etiquetaBodegaExport(header, bodegaId),
                            SessionManager.nombreUsuario()
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                    Text("PDF")
                }
                Button(
                    onClick = {
                        exportarStockBajoExcel(
                            context,
                            productosStockBajo,
                            com.example.inventario.ui.components.etiquetaBodegaExport(header, bodegaId)
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null)
                    Text("Excel")
                }
            }
        }

        if (productosStockBajo.isEmpty()) {
            item {
                Text(
                    "No hay productos con stock bajo",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(productosStockBajo, key = { it.id }) { producto ->
                val porcentaje = if (producto.stockMinimo > 0) {
                    producto.cantidad.toFloat() / producto.stockMinimo.toFloat()
                } else 0f
                val colorEstado = when {
                    producto.cantidad <= 5 -> Color.Red
                    producto.cantidad <= producto.stockMinimo -> Color(0xFFFF9800)
                    else -> Color.Green
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(producto.descripcion, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text("Código: ${producto.codigo}", color = Color.Gray)
                            }
                            Box(
                                modifier = Modifier
                                    .background(colorEstado.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (producto.cantidad <= 5) "CRÍTICO" else "BAJO",
                                    color = colorEstado,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        LinearProgressIndicator(
                            progress = { porcentaje.coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(10.dp),
                            color = colorEstado
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Actual: ${producto.cantidad}")
                            Text("Mínimo: ${producto.stockMinimo}")
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun StockCardKPI(
    titulo: String,
    valor: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = valor, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = titulo)
        }
    }
}
