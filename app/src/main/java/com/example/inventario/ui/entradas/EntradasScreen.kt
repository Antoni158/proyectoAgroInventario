package com.example.inventario.ui.entradas

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.inventario.data.bodega.Entrada
import android.widget.Toast
import com.example.inventario.data.repos.ExcelImportManager
import com.example.inventario.data.repos.appdatabase
import com.example.inventario.ui.components.BodegaScrollableListScaffold
import com.example.inventario.ui.export.PdfExportProgressDialog
import com.example.inventario.ui.salidas.PeriodoTabsSalida
import com.example.inventario.ui.salidas.EstadisticaCard
import com.example.inventario.ui.export.launchPdfExport
import com.example.inventario.ui.export.rememberPdfExportState
import com.example.inventario.viewModel.EntradaViewModel
import com.example.inventario.viewModel.SessionManager
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntradasScreen(
    navController: NavController,
    bodegaId: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pdfState = rememberPdfExportState()
    val entradaViewModel: EntradaViewModel = viewModel()

    val searchQuery by entradaViewModel.searchQuery.collectAsState()
    val filtroPeriodo by entradaViewModel.filtroPeriodo.collectAsState()
    val periodoTexto by entradaViewModel.periodoTexto.collectAsState()
    val fechaReferencia by entradaViewModel.fechaReferencia.collectAsState()

    val entradas by entradaViewModel
        .obtenerEntradasFiltradas(bodegaId)
        .collectAsState(initial = emptyList())

    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = fechaReferencia.timeInMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val cal = Calendar.getInstance().apply {
                                timeInMillis = millis + (1000 * 60 * 60 * 24)
                            }
                            entradaViewModel.setFechaReferencia(cal)
                        }
                        showDatePicker = false
                    }
                ) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    val header = com.example.inventario.ui.components.rememberBodegaHeader(bodegaId)

    val launcherImportar = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            scope.launch {
                val db = appdatabase.getDatabase(context)
                val bodega = db.bodegaDao().obtenerBodegaPorId(bodegaId)
                val result = ExcelImportManager(context).importarEntradas(
                    it, bodegaId, bodega?.codigoCorto.orEmpty()
                )
                Toast.makeText(context, result.mensaje, Toast.LENGTH_LONG).show()
            }
        }
    }

    val accentColor = MaterialTheme.colorScheme.primary

    PdfExportProgressDialog(pdfState)

    BodegaScrollableListScaffold(
        titulo = "Historial Entradas",
        bodegaId = bodegaId,
        navController = navController,
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            com.example.inventario.ui.components.ReadOnlyGate {
                if (SessionManager.tienePermiso(com.example.inventario.security.AppPermission.CREAR_ENTRADA)) {
                    FloatingActionButton(
                        containerColor = accentColor,
                        onClick = { navController.navigate("crearEntrada/$bodegaId") }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            launchPdfExport(context, pdfState, scope) { onProgress ->
                                generarEntradasPdfFile(
                                    context = context,
                                    entradas = entradas,
                                    periodo = periodoTexto,
                                    etiquetaBodega = com.example.inventario.ui.components.etiquetaBodegaExport(header, bodegaId),
                                    onProgress = onProgress
                                )
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("PDF")
                    }
                    Button(
                        onClick = {
                            exportarEntradasExcel(
                                context = context,
                                entradas = entradas,
                                periodo = periodoTexto,
                                etiquetaBodega = com.example.inventario.ui.components.etiquetaBodegaExport(header, bodegaId)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Excel")
                    }
                }
            }

            if (SessionManager.esAdmin()) {
                item {
                    OutlinedButton(
                        onClick = {
                            launcherImportar.launch(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Importar Excel")
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { entradaViewModel.setSearchQuery(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar producto...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )
            }

            item {
                PeriodoTabsSalida(
                    periodoSeleccionado = filtroPeriodo,
                    onPeriodoSelected = { entradaViewModel.setFiltroPeriodo(it) }
                )
            }

            item {
                Text(
                    text = periodoTexto,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { if (filtroPeriodo != "Todo") showDatePicker = true },
                    textAlign = TextAlign.Center,
                    color = accentColor
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EstadisticaCard(
                        titulo = "Entradas",
                        valor = entradas.size.toString(),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    EstadisticaCard(
                        titulo = "Productos",
                        valor = entradas.sumOf { it.cantidad }.toString(),
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EstadisticaCard(
                        titulo = "Costo total",
                        valor = String.format("%.0f", entradas.sumOf { it.presupuesto }),
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f)
                    )
                    EstadisticaCard(
                        titulo = "Movimientos",
                        valor = entradas.map { it.codigoProducto }.distinct().size.toString(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (entradas.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay entradas",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(items = entradas, key = { it.id }) { entrada ->
                    EntradaCard(
                        entrada = entrada,
                        navController = navController,
                        entradaViewModel = entradaViewModel,
                        bodegaId = bodegaId
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun EntradaCard(
    entrada: Entrada,
    navController: NavController,
    entradaViewModel: EntradaViewModel,
    bodegaId: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entrada.descripcion,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Código: ${entrada.codigoProducto}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "+${entrada.cantidad}",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            Text(
                text = "Proveedor: ${entrada.proveedor}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Fecha: ${entrada.fechaIngreso}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Costo: Q${String.format("%.2f", entrada.presupuesto)}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (SessionManager.esAdmin()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = {
                            navController.navigate("editarEntrada/${entrada.id}/$bodegaId")
                        }
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                    }
                    IconButton(
                        onClick = { entradaViewModel.eliminarEntrada(entrada) }
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
