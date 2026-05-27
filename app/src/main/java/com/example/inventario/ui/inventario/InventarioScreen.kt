package com.example.inventario.ui.inventario

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.inventario.data.bodega.Producto
import com.example.inventario.navigation.NavRoutes
import com.example.inventario.ui.components.BodegaScrollableListScaffold
import com.example.inventario.viewModel.ProductoViewModel
import com.example.inventario.viewModel.SessionManager

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InventarioScreen(
    navController: NavController,
    bodegaId: String
) {
    val context = LocalContext.current
    val productoViewModel: ProductoViewModel = viewModel()

    var codigoBodega by remember { mutableStateOf("") }
    LaunchedEffect(bodegaId) {
        val db = com.example.inventario.data.repos.appdatabase.getDatabase(context)
        val bodega = db.bodegaDao().obtenerBodegaPorId(bodegaId)
        bodega?.let {
            codigoBodega = it.codigoCorto
            productoViewModel.iniciarSincronizacion(it.codigoCorto, bodegaId)
        }
    }

    DisposableEffect(Unit) {
        onDispose { productoViewModel.detenerSincronizacion() }
    }

    val productos by remember(productoViewModel, bodegaId) {
        productoViewModel.obtenerProductos(bodegaId)
    }.collectAsState(initial = emptyList())

    var busqueda by remember { mutableStateOf("") }
    var filtroStatus by remember { mutableStateOf("TODOS") }

    val productosFiltrados = productos.filter { p ->
        val matchBusqueda = busqueda.isBlank() ||
            p.descripcion.contains(busqueda, ignoreCase = true) ||
            p.codigo.contains(busqueda, ignoreCase = true) ||
            p.categoria.contains(busqueda, ignoreCase = true)
        val matchStatus = filtroStatus == "TODOS" || p.status == filtroStatus
        matchBusqueda && matchStatus
    }

    BodegaScrollableListScaffold(
        titulo = "Inventario — Control",
        bodegaId = bodegaId,
        navController = navController
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "Consulta de stock y existencias",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        "El abastecimiento (productos nuevos y entradas) se registra en Entradas.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(
                            onClick = { navController.navigate(NavRoutes.kardex(bodegaId)) },
                            label = { Text("Kardex") },
                            leadingIcon = { Icon(Icons.Default.ListAlt, null) }
                        )
                        AssistChip(
                            onClick = { navController.navigate(NavRoutes.stockBajo(bodegaId)) },
                            label = { Text("Stock bajo") },
                            leadingIcon = { Icon(Icons.Default.Warning, null) }
                        )
                        AssistChip(
                            onClick = { navController.navigate(NavRoutes.movimientos(bodegaId)) },
                            label = { Text("Movimientos") }
                        )
                        if (SessionManager.puedeEscribirInventario()) {
                            AssistChip(
                                onClick = { navController.navigate(NavRoutes.crearEntrada(bodegaId)) },
                                label = { Text("Nueva entrada") }
                            )
                        }
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                label = { Text("Buscar por código, nombre o categoría") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("TODOS", "ACTIVO", "STOCK_BAJO", "SIN_STOCK").forEach { st ->
                    FilterChip(
                        selected = filtroStatus == st,
                        onClick = { filtroStatus = st },
                        label = { Text(st.replace('_', ' ')) }
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { exportarExcel(context, productosFiltrados) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Excel")
                }
                Button(
                    onClick = { exportarInventarioPDF(context, productosFiltrados, bodegaId) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("PDF")
                }
            }
        }

        item {
            Text(
                "${productosFiltrados.size} productos · Stock total: ${productosFiltrados.sumOf { it.cantidad }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (productosFiltrados.isEmpty()) {
            item {
                Text(
                    "No hay productos registrados. Use Entradas para abastecer.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(productosFiltrados, key = { it.id }) { producto ->
                ProductoConsultaCard(producto)
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun ProductoConsultaCard(producto: Producto) {
    val colorStatus = when (producto.status) {
        "ACTIVO" -> MaterialTheme.colorScheme.primary
        "STOCK_BAJO" -> MaterialTheme.colorScheme.tertiary
        "SIN_STOCK" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(producto.descripcion, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text("Código: ${producto.codigo}", style = MaterialTheme.typography.bodySmall)
            Text("Categoría: ${producto.categoria}")
            Text("Stock: ${producto.cantidad} ${producto.unidad}", fontWeight = FontWeight.SemiBold)
            Text("Estado: ${producto.status}", color = colorStatus, fontWeight = FontWeight.Bold)
            if (producto.ubicacion.isNotBlank()) Text("Ubicación: ${producto.ubicacion}")
            if (producto.proveedor.isNotBlank()) Text("Proveedor: ${producto.proveedor}")
            Text("Fecha registro: ${producto.fechaIngreso}")
            Text("Costo Compra: Q ${String.format("%.2f", producto.costo)}")
            Text("Precio Venta: Q ${String.format("%.2f", producto.precioVenta)}")
        }
    }
}

/** @deprecated Usar ProductoConsultaCard — solo consulta */
@Composable
fun ProductoCard(
    producto: Producto,
    esAdmin: Boolean,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) = ProductoConsultaCard(producto)
