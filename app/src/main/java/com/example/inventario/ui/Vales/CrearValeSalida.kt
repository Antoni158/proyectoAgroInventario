package com.example.inventario.ui.Vales

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.inventario.data.bodega.DetalleVale
import com.example.inventario.data.bodega.Vale
import com.example.inventario.navigation.NavRoutes
import com.example.inventario.ui.components.navigateBackSafely
import com.example.inventario.ui.config.notifications.AppTopBar
import com.example.inventario.viewModel.ProductoViewModel
import com.example.inventario.viewModel.SessionManager
import com.example.inventario.viewModel.ValeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class LineaValeDraft(
    val codigo: String = "",
    val descripcion: String = "",
    val categoria: String = "",
    val cantidad: String = "1"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearValeScreen(
    navController: NavController,
    bodegaId: String
) {
    val valeViewModel: ValeViewModel = viewModel()
    val productoViewModel: ProductoViewModel = viewModel()

    val productos by remember(productoViewModel, bodegaId) {
        productoViewModel.obtenerProductos(bodegaId)
    }.collectAsState(initial = emptyList())

    val codigoVale = remember { valeViewModel.generarCodigoVale(bodegaId) }
    val fecha = remember {
        SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(Date())
    }

    var responsable by remember { mutableStateOf(SessionManager.nombreUsuario()) }
    var destino by remember { mutableStateOf("") }
    var observacion by remember { mutableStateOf("") }
    var buscarProducto by remember { mutableStateOf("") }

    val lineas = remember { mutableStateListOf(LineaValeDraft()) }

    val productosFiltrados = productos.filter {
        buscarProducto.isBlank() ||
            it.codigo.contains(buscarProducto, ignoreCase = true) ||
            it.descripcion.contains(buscarProducto, ignoreCase = true)
    }.take(8)

    Scaffold(
        topBar = {
            AppTopBar(
                titulo = "Nuevo vale multi-producto",
                subtitulo = codigoVale,
                navController = navController,
                bodegaId = bodegaId
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = responsable,
                onValueChange = { responsable = it },
                label = { Text("Responsable") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = destino,
                onValueChange = { destino = it },
                label = { Text("Destino") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = observacion,
                onValueChange = { observacion = it },
                label = { Text("Observación") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Agregar productos", fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = buscarProducto,
                onValueChange = { buscarProducto = it },
                label = { Text("Buscar en inventario") },
                modifier = Modifier.fillMaxWidth()
            )

            productosFiltrados.forEach { p ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .clickable {
                            lineas.add(
                                LineaValeDraft(
                                    codigo = p.codigo,
                                    descripcion = p.descripcion,
                                    categoria = p.categoria,
                                    cantidad = "1"
                                )
                            )
                            buscarProducto = ""
                        }
                ) {
                    Text(
                        "${p.codigo} — ${p.descripcion} (stock: ${p.cantidad})",
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Text("Líneas del vale (${lineas.size})", fontWeight = FontWeight.Bold)

            lineas.forEachIndexed { index, linea ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Producto ${index + 1}", fontWeight = FontWeight.SemiBold)
                            if (lineas.size > 1) {
                                IconButton(onClick = { lineas.removeAt(index) }) {
                                    Text("✕")
                                }
                            }
                        }
                        OutlinedTextField(
                            value = linea.codigo,
                            onValueChange = { lineas[index] = linea.copy(codigo = it) },
                            label = { Text("Código") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = linea.descripcion,
                            onValueChange = { lineas[index] = linea.copy(descripcion = it) },
                            label = { Text("Descripción") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = linea.categoria,
                            onValueChange = { lineas[index] = linea.copy(categoria = it) },
                            label = { Text("Categoría") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = linea.cantidad,
                            onValueChange = { lineas[index] = linea.copy(cantidad = it) },
                            label = { Text("Cantidad") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Button(
                onClick = { lineas.add(LineaValeDraft()) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Añadir línea manual")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val detalles = lineas.mapNotNull { l ->
                        val cant = l.cantidad.toIntOrNull() ?: 0
                        if (l.codigo.isBlank() || cant <= 0) null
                        else DetalleVale(
                            productoCodigo = l.codigo,
                            productoDescripcion = l.descripcion,
                            categoria = l.categoria,
                            cantidad = cant,
                            bodegaId = bodegaId
                        )
                    }
                    if (detalles.isEmpty()) return@Button

                    val vale = Vale(
                        codigoVale = codigoVale,
                        responsable = responsable,
                        destino = destino,
                        fecha = fecha,
                        observacion = observacion,
                        bodegaId = bodegaId,
                        estado = "CONFIRMADO",
                        usuario = SessionManager.usernameUsuario()
                    )

                    valeViewModel.crearValeConDetalles(vale, detalles) { valeId ->
                        navController.navigate(NavRoutes.detalleVale(valeId, bodegaId)) {
                            popUpTo(NavRoutes.crearVale(bodegaId)) { inclusive = true }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar vale")
            }

            Button(
                onClick = { navController.navigateBackSafely(bodegaId = bodegaId) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar")
            }
        }
    }
}
