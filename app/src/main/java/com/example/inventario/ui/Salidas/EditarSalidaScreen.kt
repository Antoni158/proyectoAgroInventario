package com.example.inventario.ui.salidas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.inventario.data.bodega.Salida
import com.example.inventario.ui.components.navigateBackSafely
import com.example.inventario.ui.config.notifications.AppTopBar
import com.example.inventario.ui.config.notifications.FechaIngresar
import com.example.inventario.viewModel.SalidaViewModel
import kotlinx.coroutines.launch

private fun splitDestinoSalida(salida: Salida): Pair<String, String> {
    if (salida.placa.isNotBlank()) {
        val nombre = salida.destino.substringBefore(" · ").trim().ifBlank { salida.destino }
        return nombre to salida.placa
    }
    val parts = salida.destino.split(" · ", limit = 2)
    return if (parts.size == 2) parts[0].trim() to parts[1].trim() else salida.destino to ""
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarSalidaScreen(
    navController: NavController,
    salidaId: Int,
    bodegaId: String
) {
    val salidaViewModel: SalidaViewModel = viewModel()
    val scope = rememberCoroutineScope()

    val salidas by salidaViewModel.obtenerSalidasPorBodega(bodegaId)
        .collectAsState(initial = emptyList())

    var salidaOriginal by remember { mutableStateOf<Salida?>(null) }

    var responsable by remember { mutableStateOf("") }
    var quienLoLleva by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var destino by remember { mutableStateOf("") }
    var destinoNo by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var fechaSalida by remember { mutableStateOf("") }
    var notas by remember { mutableStateOf("") }

    LaunchedEffect(salidas, salidaId) {
        val salida = salidas.find { it.id == salidaId } ?: return@LaunchedEffect
        salidaOriginal = salida
        val (dest, no) = splitDestinoSalida(salida)
        responsable = salida.responsable
        quienLoLleva = salida.vehiculo
        area = salida.area
        destino = dest
        destinoNo = no
        cantidad = salida.cantidad.toString()
        fechaSalida = salida.fechaSalida
        notas = salida.notas
    }

    Scaffold(
        topBar = {
            AppTopBar(titulo = "Editar Salida", navController = navController)
        }
    ) { padding ->
        if (salidaOriginal == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val salida = salidaOriginal!!
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
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Modificar salida", fontSize = 22.sp, fontWeight = FontWeight.Bold)

                        OutlinedTextField(
                            value = salida.codigoSalida,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Código salida") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = salida.numeroVale,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Número vale") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = "${salida.codigoProducto} — ${salida.descripcion}",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Producto") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            "Categoría: ${salida.categoria} · Costo std: Q ${"%.2f".format(salida.costoUnitario)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = cantidad,
                            onValueChange = { cantidad = it.filter { c -> c.isDigit() } },
                            label = { Text("Cantidad *") },
                            modifier = Modifier.fillMaxWidth()
                        )

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
                            modifier = Modifier.fillMaxWidth()
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
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = destinoNo,
                                onValueChange = { destinoNo = it },
                                label = { Text("No. *") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        FechaIngresar(
                            fecha = fechaSalida,
                            onFechaChange = { fechaSalida = it },
                            label = "Fecha",
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = notas,
                            onValueChange = { notas = it },
                            label = { Text("Notas") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { navController.navigateBackSafely() }) {
                                Text("Cancelar")
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(
                                onClick = {
                                    scope.launch {
                                        val cantInt = cantidad.toIntOrNull() ?: 0
                                        if (cantInt <= 0) return@launch
                                        val destinoCompleto = "$destino · $destinoNo".trim()
                                        val total = cantInt * salida.costoUnitario
                                        salidaViewModel.actualizarSalida(
                                            salida.copy(
                                                cantidad = cantInt,
                                                responsable = responsable.trim(),
                                                vehiculo = quienLoLleva.trim(),
                                                area = area.trim(),
                                                destino = destinoCompleto,
                                                placa = destinoNo.trim(),
                                                fechaSalida = fechaSalida,
                                                notas = notas.trim(),
                                                total = total,
                                                precioVenta = 0.0
                                            )
                                        )
                                        navController.navigateBackSafely()
                                    }
                                }
                            ) {
                                Text("Actualizar")
                            }
                        }
                    }
                }
            }
        }
    }
}
