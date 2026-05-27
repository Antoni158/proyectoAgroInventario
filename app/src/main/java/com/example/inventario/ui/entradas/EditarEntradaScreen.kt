package com.example.inventario.ui

import android.widget.Toast
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.inventario.data.bodega.Entrada
import com.example.inventario.ui.components.navigateBackSafely
import com.example.inventario.ui.config.notifications.AppTopBar
import com.example.inventario.ui.config.notifications.FechaIngresar
import com.example.inventario.viewModel.EntradaViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarEntradaScreen(
    navController: NavController,
    entradaId: Int,
    bodegaId: String
) {
    val context = LocalContext.current
    val entradaViewModel: EntradaViewModel = viewModel()
    val scope = rememberCoroutineScope()

    var entradaOriginal by remember { mutableStateOf<Entrada?>(null) }

    var cantidad by remember { mutableStateOf("") }
    var costoEntrada by remember { mutableStateOf("") }
    var unidad by remember { mutableStateOf("") }
    var proveedor by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var numeroFactura by remember { mutableStateOf("") }
    var lote by remember { mutableStateOf("") }
    var stockMinimo by remember { mutableStateOf("") }
    var fechaIngreso by remember { mutableStateOf("") }
    var notas by remember { mutableStateOf("") }

    LaunchedEffect(entradaId) {
        val entrada = entradaViewModel.obtenerEntradaPorId(entradaId) ?: return@LaunchedEffect
        entradaOriginal = entrada
        cantidad = entrada.cantidad.toString()
        costoEntrada = entrada.costoEntrada.toString()
        unidad = entrada.unidad
        proveedor = entrada.proveedor
        ubicacion = entrada.ubicacion
        numeroFactura = entrada.numeroFactura
        lote = entrada.lote
        stockMinimo = entrada.stockMinimo.toString()
        fechaIngreso = entrada.fechaIngreso
        notas = entrada.notas
    }

    Scaffold(
        topBar = {
            AppTopBar(titulo = "Editar Entrada", navController = navController)
        }
    ) { padding ->
        if (entradaOriginal == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val entrada = entradaOriginal!!
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
                        Text("Modificar entrada", fontSize = 22.sp, fontWeight = FontWeight.Bold)

                        OutlinedTextField(
                            value = entrada.codigoEntrada,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Código entrada") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = "${entrada.codigoProducto} — ${entrada.descripcion}",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Producto") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            "Categoría: ${entrada.categoria}",
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
                            value = costoEntrada,
                            onValueChange = { costoEntrada = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text("Costo unitario de esta entrada (Q) *") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = unidad,
                            onValueChange = { unidad = it },
                            label = { Text("Unidad") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = proveedor,
                            onValueChange = { proveedor = it },
                            label = { Text("Proveedor *") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = ubicacion,
                            onValueChange = { ubicacion = it },
                            label = { Text("Ubicación") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = numeroFactura,
                            onValueChange = { numeroFactura = it },
                            label = { Text("N° Factura") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = lote,
                            onValueChange = { lote = it },
                            label = { Text("Lote") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = stockMinimo,
                            onValueChange = { stockMinimo = it.filter { c -> c.isDigit() } },
                            label = { Text("Stock mínimo") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        FechaIngresar(
                            fecha = fechaIngreso,
                            onFechaChange = { fechaIngreso = it },
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
                                        val costoDouble = costoEntrada.toDoubleOrNull() ?: 0.0
                                        if (cantInt <= 0) {
                                            Toast.makeText(context, "Cantidad inválida", Toast.LENGTH_SHORT).show()
                                            return@launch
                                        }
                                        if (proveedor.isBlank()) {
                                            Toast.makeText(context, "Ingrese proveedor", Toast.LENGTH_SHORT).show()
                                            return@launch
                                        }
                                        entradaViewModel.actualizarEntrada(
                                            entrada.copy(
                                                cantidad = cantInt,
                                                costoEntrada = costoDouble,
                                                precioVenta = 0.0,
                                                presupuesto = cantInt * costoDouble,
                                                unidad = unidad.ifBlank { "Unidad" },
                                                proveedor = proveedor.trim(),
                                                ubicacion = ubicacion,
                                                numeroFactura = numeroFactura.trim(),
                                                lote = lote,
                                                stockMinimo = stockMinimo.toIntOrNull()?.coerceAtLeast(0) ?: 0,
                                                fechaIngreso = fechaIngreso,
                                                notas = notas.trim()
                                            )
                                        )
                                        Toast.makeText(context, "Entrada actualizada", Toast.LENGTH_SHORT).show()
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
