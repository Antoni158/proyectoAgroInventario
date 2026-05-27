package com.example.inventario.ui.Vales

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.inventario.data.bodega.Bodega
import com.example.inventario.data.bodega.Traslado
import com.example.inventario.data.repos.MovimientoInventarioService
import com.example.inventario.data.repos.ValeSalidaIntegracion
import com.example.inventario.data.repos.appdatabase
import com.example.inventario.ui.components.navigateBackSafely
import com.example.inventario.ui.components.rememberBodegaHeader
import com.example.inventario.ui.config.notifications.AppTopBar
import com.example.inventario.ui.config.notifications.FechaIngresar
import com.example.inventario.viewModel.BodegaViewModel
import com.example.inventario.viewModel.ProductoViewModel
import com.example.inventario.viewModel.SessionManager
import com.example.inventario.viewModel.TrasladoViewModel
import com.example.inventario.viewModel.ValeViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

private data class LineaTrasladoDraft(
    val codigo: String,
    val descripcion: String,
    val categoria: String,
    val cantidad: String,
    val stock: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearTrasladoScreen(
    navController: NavController,
    bodegaId: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val header = rememberBodegaHeader(bodegaId)

    val viewModel: TrasladoViewModel = viewModel()
    val productoViewModel: ProductoViewModel = viewModel()
    val bodegaViewModel: BodegaViewModel = viewModel()
    val valeViewModel: ValeViewModel = viewModel()

    val bodegas by bodegaViewModel.bodegas.collectAsState(initial = emptyList())
    val otrasBodegas = bodegas.filter { it.id != bodegaId }

    val codigoTraslado = remember { "TRA-${System.currentTimeMillis()}" }
    val codigoVale = remember { valeViewModel.generarCodigoVale(bodegaId) }

    var bodegaDestino by remember { mutableStateOf<Bodega?>(null) }
    var destinoUso by remember { mutableStateOf("") }
    var responsable by remember { mutableStateOf(SessionManager.nombreUsuario()) }
    var observacion by remember { mutableStateOf("") }
    var busquedaProducto by remember { mutableStateOf("") }
    var sugerencias by remember { mutableStateOf(emptyList<com.example.inventario.data.bodega.Producto>()) }
    var mostrarSugerencias by remember { mutableStateOf(false) }
    var searchJob by remember { mutableStateOf<Job?>(null) }
    var bodegaDestinoExpanded by remember { mutableStateOf(false) }

    val lineas = remember { mutableStateListOf<LineaTrasladoDraft>() }

    var fecha by remember {
        val c = Calendar.getInstance()
        mutableStateOf("${c.get(Calendar.DAY_OF_MONTH)}/${c.get(Calendar.MONTH) + 1}/${c.get(Calendar.YEAR)}")
    }

    LaunchedEffect(busquedaProducto) {
        searchJob?.cancel()
        if (busquedaProducto.length < 2) {
            sugerencias = emptyList()
            mostrarSugerencias = false
            return@LaunchedEffect
        }
        searchJob = scope.launch {
            delay(300)
            val exacto = productoViewModel.buscarProductoPorCodigoGlobal(busquedaProducto.trim().uppercase())
            if (exacto != null && exacto.bodegaId == bodegaId) {
                lineas.add(
                    LineaTrasladoDraft(
                        codigo = exacto.codigo,
                        descripcion = exacto.descripcion,
                        categoria = exacto.categoria,
                        cantidad = "1",
                        stock = exacto.cantidad
                    )
                )
                busquedaProducto = ""
                mostrarSugerencias = false
            } else {
                sugerencias = productoViewModel.autocompletarProducto(bodegaId, busquedaProducto.trim())
                mostrarSugerencias = sugerencias.isNotEmpty()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            AppTopBar(
                titulo = "Nuevo Traslado",
                subtitulo = "Movimiento entre bodegas",
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = codigoTraslado,
                onValueChange = {},
                readOnly = true,
                label = { Text("Código traslado") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = "${header.nombre} (${header.codigo})",
                onValueChange = {},
                readOnly = true,
                label = { Text("Bodega origen") },
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = bodegaDestinoExpanded,
                onExpandedChange = { bodegaDestinoExpanded = it }
            ) {
                OutlinedTextField(
                    value = bodegaDestino?.let { "${it.nombre} (${it.codigoCorto})" } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Bodega destino *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bodegaDestinoExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = bodegaDestinoExpanded,
                    onDismissRequest = { bodegaDestinoExpanded = false }
                ) {
                    otrasBodegas.forEach { b ->
                        DropdownMenuItem(
                            text = { Text("${b.nombre} (${b.codigoCorto})") },
                            onClick = {
                                bodegaDestino = b
                                bodegaDestinoExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = destinoUso,
                onValueChange = { destinoUso = it },
                label = { Text("Destino / uso (ej. tractor, campo, taller)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = responsable,
                onValueChange = { responsable = it },
                label = { Text("Responsable") },
                modifier = Modifier.fillMaxWidth()
            )

            FechaIngresar(fecha = fecha, onFechaChange = { fecha = it }, label = "Fecha")

            OutlinedTextField(
                value = observacion,
                onValueChange = { observacion = it },
                label = { Text("Observación") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            HorizontalDivider()
            Text("Productos a trasladar", fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = busquedaProducto,
                onValueChange = { busquedaProducto = it },
                label = { Text("Buscar producto (código o descripción)") },
                modifier = Modifier.fillMaxWidth()
            )

            if (mostrarSugerencias) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        sugerencias.take(6).forEach { prod ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        lineas.add(
                                            LineaTrasladoDraft(
                                                codigo = prod.codigo,
                                                descripcion = prod.descripcion,
                                                categoria = prod.categoria,
                                                cantidad = "1",
                                                stock = prod.cantidad
                                            )
                                        )
                                        busquedaProducto = ""
                                        mostrarSugerencias = false
                                    }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(prod.descripcion, fontWeight = FontWeight.Bold)
                                    Text(prod.codigo, style = MaterialTheme.typography.bodySmall)
                                }
                                Text("Stock: ${prod.cantidad}", style = MaterialTheme.typography.bodySmall)
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }

            lineas.forEachIndexed { index, linea ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(linea.descripcion, fontWeight = FontWeight.Bold)
                            Text("${linea.codigo} · ${linea.categoria} · Stock: ${linea.stock}")
                            OutlinedTextField(
                                value = linea.cantidad,
                                onValueChange = { v ->
                                    lineas[index] = linea.copy(cantidad = v.filter { c -> c.isDigit() })
                                },
                                label = { Text("Cantidad") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        IconButton(onClick = { lineas.removeAt(index) }) {
                            Text("✕", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            TextButton(onClick = { lineas.add(LineaTrasladoDraft("", "", "", "1")) }) {
                Text("+ Agregar línea manual")
            }

            Button(
                onClick = {
                    scope.launch {
                        val dest = bodegaDestino
                        if (dest == null) {
                            snackbar.showSnackbar("Seleccione bodega destino")
                            return@launch
                        }
                        if (lineas.isEmpty()) {
                            snackbar.showSnackbar("Agregue al menos un producto")
                            return@launch
                        }
                        val obsFinal = buildString {
                            if (destinoUso.isNotBlank()) append("Destino uso: $destinoUso. ")
                            append(observacion.trim())
                        }.trim()
                        val valeLineas = mutableListOf<ValeSalidaIntegracion.LineaVale>()
                        for (linea in lineas) {
                            val cant = linea.cantidad.toIntOrNull() ?: 0
                            if (linea.codigo.isBlank() || cant <= 0) {
                                snackbar.showSnackbar("Revise código y cantidad de cada producto")
                                return@launch
                            }
                            if (cant > linea.stock) {
                                snackbar.showSnackbar("Stock insuficiente para ${linea.codigo}")
                                return@launch
                            }
                            val traslado = Traslado(
                                codigoTraslado = codigoTraslado,
                                productoCodigo = linea.codigo,
                                productoDescripcion = linea.descripcion,
                                categoria = linea.categoria,
                                cantidad = cant,
                                bodegaOrigen = bodegaId,
                                bodegaDestino = dest.id,
                                responsable = responsable,
                                fecha = fecha,
                                observacion = obsFinal,
                                bodegaId = bodegaId
                            )
                            when (val r = viewModel.registrarTrasladoCompleto(
                                traslado,
                                SessionManager.usernameUsuario()
                            )) {
                                is MovimientoInventarioService.ResultadoMovimiento.TrasladoOk -> {
                                    valeLineas.add(
                                        ValeSalidaIntegracion.LineaVale(
                                            productoCodigo = linea.codigo,
                                            productoDescripcion = linea.descripcion,
                                            categoria = linea.categoria,
                                            cantidad = cant,
                                            codigoSalida = codigoTraslado
                                        )
                                    )
                                }
                                is MovimientoInventarioService.ResultadoMovimiento.Error -> {
                                    snackbar.showSnackbar(r.mensaje)
                                    return@launch
                                }
                                else -> {
                                    snackbar.showSnackbar("Error en traslado")
                                    return@launch
                                }
                            }
                        }
                        val destinoVale = destinoUso.ifBlank { dest.nombre }
                        ValeSalidaIntegracion.procesarLineasEnVale(
                            database = appdatabase.getDatabase(context),
                            bodegaId = bodegaId,
                            codigoVale = codigoVale,
                            responsable = responsable,
                            destino = destinoVale,
                            fecha = fecha,
                            observacion = obsFinal,
                            usuario = SessionManager.usernameUsuario(),
                            lineas = valeLineas
                        )
                        Toast.makeText(
                            context,
                            "Traslado $codigoTraslado · Vale $codigoVale",
                            Toast.LENGTH_LONG
                        ).show()
                        navController.navigateBackSafely(bodegaId = bodegaId)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar traslado")
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
