package com.example.inventario.ui.salidas

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.inventario.data.bodega.Producto
import com.example.inventario.data.bodega.Salida
import com.example.inventario.data.repos.ValeSalidaIntegracion
import com.example.inventario.data.repos.appdatabase
import com.example.inventario.ui.components.navigateBackSafely
import com.example.inventario.ui.config.notifications.AppTopBar
import com.example.inventario.ui.config.notifications.FechaIngresar
import com.example.inventario.viewModel.ProductoViewModel
import com.example.inventario.viewModel.SalidaViewModel
import com.example.inventario.viewModel.SessionManager
import com.example.inventario.viewModel.ValeViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

private data class LineaSalidaDraft(
    val codigo: String,
    val descripcion: String,
    val categoria: String,
    val cantidad: String,
    val costo: Double,
    val stock: Int,
    val unidad: String = "Unidad"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearSalidasScreen(
    navController: NavController,
    bodegaId: String
) {
    val context = LocalContext.current
    val salidaViewModel: SalidaViewModel = viewModel()
    val productoViewModel: ProductoViewModel = viewModel()
    val valeViewModel: ValeViewModel = viewModel()
    val scope = rememberCoroutineScope()

    val codigoVale = remember { valeViewModel.generarCodigoVale(bodegaId) }

    var responsable by remember { mutableStateOf(SessionManager.nombreUsuario()) }
    var quienLoLleva by remember { mutableStateOf("") }
    var destino by remember { mutableStateOf("") }
    var destinoNo by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var notas by remember { mutableStateOf("") }
    var busquedaProducto by remember { mutableStateOf("") }
    var sugerencias by remember { mutableStateOf(emptyList<Producto>()) }
    var mostrarSugerencias by remember { mutableStateOf(false) }
    var searchJob by remember { mutableStateOf<Job?>(null) }
    var guardando by remember { mutableStateOf(false) }

    val lineas = remember { mutableStateListOf<LineaSalidaDraft>() }

    var fechaSalida by remember {
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
                agregarLineaSalida(lineas, exacto)
                busquedaProducto = ""
                mostrarSugerencias = false
            } else {
                sugerencias = productoViewModel.autocompletarProducto(bodegaId, busquedaProducto.trim())
                mostrarSugerencias = sugerencias.isNotEmpty()
            }
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                titulo = "Registrar Salida",
                subtitulo = "Vale $codigoVale",
                navController = navController
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Registrar salida multi-producto", fontSize = 22.sp, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = responsable,
                        onValueChange = { responsable = it },
                        label = { Text("Responsable *") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = quienLoLleva,
                        onValueChange = { quienLoLleva = it },
                        label = { Text("Quien lo lleva *") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Nombre de la persona que transporta") }
                    )

                    OutlinedTextField(
                        value = area,
                        onValueChange = { area = it },
                        label = { Text("Área (ej: mecanización, siembra)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = destino,
                            onValueChange = { destino = it },
                            label = { Text("Destino *") },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Campo, taller…") }
                        )
                        OutlinedTextField(
                            value = destinoNo,
                            onValueChange = { destinoNo = it },
                            label = { Text("No. *") },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Hilux P090PLJ, tractor 1…") }
                        )
                    }

                    FechaIngresar(
                        fecha = fechaSalida,
                        onFechaChange = { fechaSalida = it },
                        label = "Fecha"
                    )

                    OutlinedTextField(
                        value = notas,
                        onValueChange = { notas = it },
                        label = { Text("Notas") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    HorizontalDivider()
                    Text("Productos", fontWeight = FontWeight.Bold)

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
                                                agregarLineaSalida(lineas, prod)
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
                                    Text(
                                        "${linea.codigo} · ${linea.categoria} · Costo std: Q ${"%.2f".format(linea.costo)} · Stock: ${linea.stock}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    OutlinedTextField(
                                        value = linea.cantidad,
                                        onValueChange = { v ->
                                            lineas[index] = linea.copy(cantidad = v.filter { it.isDigit() })
                                        },
                                        label = { Text("Cantidad") },
                                        modifier = Modifier.fillMaxWidth(),
                                        isError = (linea.cantidad.toIntOrNull() ?: 0) > linea.stock
                                    )
                                }
                                IconButton(onClick = { lineas.removeAt(index) }) {
                                    Text("✕", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }

                    TextButton(onClick = { /* buscar arriba */ }) {
                        Text("+ Busque y seleccione productos arriba")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !guardando,
                        onClick = {
                            scope.launch {
                                if (destino.isBlank() || destinoNo.isBlank()) {
                                    Toast.makeText(context, "Complete destino y No.", Toast.LENGTH_SHORT).show()
                                    return@launch
                                }
                                if (quienLoLleva.isBlank()) {
                                    Toast.makeText(context, "Indique quien lo lleva", Toast.LENGTH_SHORT).show()
                                    return@launch
                                }
                                if (responsable.isBlank()) {
                                    Toast.makeText(context, "Ingrese responsable", Toast.LENGTH_SHORT).show()
                                    return@launch
                                }
                                if (lineas.isEmpty()) {
                                    Toast.makeText(context, "Agregue al menos un producto", Toast.LENGTH_SHORT).show()
                                    return@launch
                                }
                                guardando = true
                                try {
                                    val destinoCompleto = "$destino · $destinoNo".trim()
                                    val codigosSalida = salidaViewModel.generarCodigosSalida(bodegaId, lineas.size)
                                    val salidasPendientes = lineas.mapIndexed { i, linea ->
                                        val cant = linea.cantidad.toIntOrNull() ?: 0
                                        if (cant <= 0) {
                                            Toast.makeText(context, "Cantidad inválida en ${linea.codigo}", Toast.LENGTH_SHORT).show()
                                            return@launch
                                        }
                                        if (cant > linea.stock) {
                                            Toast.makeText(context, "Stock insuficiente: ${linea.codigo}", Toast.LENGTH_SHORT).show()
                                            return@launch
                                        }
                                        Salida(
                                            codigoSalida = codigosSalida.getOrElse(i) {
                                                salidaViewModel.generarCodigoSalida(bodegaId)
                                            },
                                            codigoProducto = linea.codigo,
                                            descripcion = linea.descripcion,
                                            categoria = linea.categoria,
                                            cantidad = cant,
                                            unidad = linea.unidad,
                                            responsable = responsable,
                                            destino = destinoCompleto,
                                            vehiculo = quienLoLleva,
                                            placa = destinoNo,
                                            area = area,
                                            costoUnitario = linea.costo,
                                            precioVenta = 0.0,
                                            total = cant * linea.costo,
                                            numeroVale = codigoVale,
                                            tipoSalida = "CONSUMO",
                                            fechaSalida = fechaSalida,
                                            usuario = SessionManager.usernameUsuario(),
                                            bodegaId = bodegaId,
                                            notas = notas
                                        )
                                    }
                                    val guardadas = salidaViewModel.registrarSalidasEnVale(salidasPendientes)
                                    val valeLineas = guardadas.map { g ->
                                        ValeSalidaIntegracion.LineaVale(
                                            productoCodigo = g.codigoProducto,
                                            productoDescripcion = g.descripcion,
                                            categoria = g.categoria,
                                            cantidad = g.cantidad,
                                            codigoSalida = g.codigoSalida,
                                            costoUnitario = g.costoUnitario
                                        )
                                    }
                                    ValeSalidaIntegracion.procesarLineasEnVale(
                                        database = appdatabase.getDatabase(context),
                                        bodegaId = bodegaId,
                                        codigoVale = codigoVale,
                                        responsable = responsable,
                                        destino = "$destino · $destinoNo".trim(),
                                        fecha = fechaSalida,
                                        observacion = notas,
                                        usuario = SessionManager.usernameUsuario(),
                                        lineas = valeLineas
                                    )
                                    Toast.makeText(
                                        context,
                                        "Salida registrada · Vale $codigoVale",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    navController.navigateBackSafely(bodegaId = bodegaId)
                                } catch (e: Exception) {
                                    Toast.makeText(context, e.message ?: "Error al registrar", Toast.LENGTH_LONG).show()
                                } finally {
                                    guardando = false
                                }
                            }
                        }
                    ) {
                        Text(if (guardando) "Guardando…" else "Registrar salida y generar vale")
                    }
                }
            }
        }
    }
}

private fun agregarLineaSalida(lineas: MutableList<LineaSalidaDraft>, prod: Producto) {
    if (lineas.any { it.codigo == prod.codigo }) return
    lineas.add(
        LineaSalidaDraft(
            codigo = prod.codigo,
            descripcion = prod.descripcion,
            categoria = prod.categoria,
            cantidad = "1",
            costo = prod.costo,
            stock = prod.cantidad,
            unidad = prod.unidad.ifBlank { "Unidad" }
        )
    )
}
