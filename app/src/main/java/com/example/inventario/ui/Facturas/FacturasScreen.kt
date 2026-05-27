package com.example.inventario.ui.Facturas

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

import androidx.compose.material3.*

import androidx.compose.runtime.*

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

import com.example.inventario.navigation.NavRoutes
import com.example.inventario.data.bodega.Factura
import com.example.inventario.data.repos.appdatabase
import android.widget.Toast
import com.example.inventario.data.repos.ExcelImportManager
import com.example.inventario.ui.components.BodegaScrollableListScaffold
import com.example.inventario.ui.components.etiquetaBodegaExport
import com.example.inventario.ui.components.rememberBodegaHeader
import com.example.inventario.viewModel.FacturaViewModel
import com.example.inventario.viewModel.SessionManager
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacturasScreen(

    navController: NavController,

    bodegaId: String

) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel: FacturaViewModel = viewModel()

    val launcherImportar = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            scope.launch {
                val db = appdatabase.getDatabase(context)
                val bodega = db.bodegaDao().obtenerBodegaPorId(bodegaId)
                val result = ExcelImportManager(context).importarFacturas(
                    it, bodegaId, bodega?.codigoCorto.orEmpty()
                )
                Toast.makeText(context, result.mensaje, Toast.LENGTH_LONG).show()
            }
        }
    }

    // STATES

    val searchQuery by
    viewModel.searchQuery.collectAsState()

    val filtroPeriodo by
    viewModel.filtroPeriodo.collectAsState()

    val periodoTexto by
    viewModel.periodoTexto.collectAsState()

    val fechaReferencia by
    viewModel.fechaReferencia.collectAsState()

    val facturas by

    viewModel
        .obtenerFacturasFiltradas(
            bodegaId
        )
        .collectAsState(
            initial = emptyList()
        )

    var showDatePicker by
    remember {
        mutableStateOf(false)
    }

    // DATE PICKER

    if (showDatePicker) {

        val datePickerState =
            rememberDatePickerState(

                initialSelectedDateMillis =
                    fechaReferencia.timeInMillis
            )

        DatePickerDialog(

            onDismissRequest = {

                showDatePicker = false
            },

            confirmButton = {

                TextButton(

                    onClick = {

                        datePickerState
                            .selectedDateMillis
                            ?.let { millis ->

                                val cal =
                                    Calendar.getInstance()

                                cal.timeInMillis =
                                    millis +
                                            (
                                                    1000 * 60 * 60 * 24
                                                    )

                                viewModel
                                    .setFechaReferencia(
                                        cal
                                    )
                            }

                        showDatePicker = false
                    }

                ) {

                    Text("Aceptar")
                }
            },

            dismissButton = {

                TextButton(

                    onClick = {

                        showDatePicker = false
                    }

                ) {

                    Text("Cancelar")
                }
            }

        ) {

            DatePicker(
                state = datePickerState
            )
        }
    }

    // FIREBASE

    LaunchedEffect(Unit) {
        val bId = SessionManager.obtenerBodegaActual()
        if (bId.isNotEmpty()) {
            val db = appdatabase.getDatabase(context)
            val bodega = db.bodegaDao().obtenerBodegaPorId(bId)
            bodega?.let {
                viewModel.sincronizarDesdeFirebase(it.codigoCorto, it.id)
            }
        }
    }

    // UI

    BodegaScrollableListScaffold(
        titulo = "Historial de Facturas",
        bodegaId = bodegaId,
        navController = navController,
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            com.example.inventario.ui.components.ReadOnlyGate {
                if (
                    SessionManager.tienePermiso(com.example.inventario.security.AppPermission.CREAR_FACTURA)
                    || SessionManager.rolUsuario() == "encargado"
                ) {
                    FloatingActionButton(
                        onClick = { navController.navigate(NavRoutes.crearFactura(bodegaId)) },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }
                }
            }
        }
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar Factura...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        item {
            PeriodoTabsFactura(periodoSeleccionado = filtroPeriodo) {
                viewModel.setFiltroPeriodo(it)
            }
        }

        item {
            Text(
                text = periodoTexto,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (filtroPeriodo != "Todo") showDatePicker = true
                    },
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Facturas")
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${facturas.size}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    }
                }
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Total")
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$ ${String.format("%.2f", facturas.sumOf { it.total })}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }

        item {
            val headerInfo = rememberBodegaHeader(bodegaId)
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        exportarFacturasPDF(
                            context,
                            facturas,
                            periodoTexto,
                            bodegaId = bodegaId,
                            etiquetaBodega = etiquetaBodegaExport(headerInfo, bodegaId)
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("PDF")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        exportarFacturasExcel(
                            context,
                            facturas,
                            periodoTexto,
                            etiquetaBodega = etiquetaBodegaExport(headerInfo, bodegaId)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D6F42))
                ) {
                    Icon(Icons.Default.TableChart, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Excel")
                }
            }
        }

        if (SessionManager.esAdmin()) {
            item {
                OutlinedButton(
                    onClick = { launcherImportar.launch("*/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Importar Excel")
                }
            }
        }

        if (facturas.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No hay facturas", color = Color.Gray)
                }
            }
        } else {
            items(facturas, key = { it.id }) { factura ->
                FacturaCardItem(
                    factura = factura,
                    viewModel = viewModel,
                    navController = navController
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun PeriodoTabsFactura(

    periodoSeleccionado: String,

    onPeriodoSelected:
        (String) -> Unit

) {

    val opciones =

        listOf(
            "Dia",
            "Semana",
            "Mes",
            "Año",
            "Todo"
        )

    Row(

        modifier = Modifier
            .fillMaxWidth()
            .background(

                MaterialTheme
                    .colorScheme
                    .surfaceVariant,

                RoundedCornerShape(12.dp)
            )
            .padding(4.dp),

        horizontalArrangement =
            Arrangement.SpaceEvenly

    ) {

        opciones.forEach { opcion ->

            val seleccionado =
                periodoSeleccionado ==
                        opcion

            Box(

                modifier = Modifier
                    .weight(1f)
                    .background(

                        if (seleccionado)

                            MaterialTheme
                                .colorScheme
                                .primary

                        else

                            Color.Transparent,

                        RoundedCornerShape(8.dp)
                    )
                    .clickable {

                        onPeriodoSelected(
                            opcion
                        )
                    }
                    .padding(vertical = 8.dp),

                contentAlignment =
                    Alignment.Center

            ) {

                Text(

                    text = opcion,

                    color =

                        if (seleccionado)

                            MaterialTheme
                                .colorScheme
                                .onPrimary

                        else

                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant,

                    fontWeight =

                        if (seleccionado)

                            FontWeight.Bold

                        else

                            FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun FacturaCardItem(

    factura: Factura,

    viewModel: FacturaViewModel,

    navController: NavController

) {

    Card(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(12.dp),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )

    ) {

        Column(

            modifier =
                Modifier.padding(16.dp)

        ) {

            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.CenterVertically

            ) {

                Column(

                    modifier =
                        Modifier.weight(1f)

                ) {

                    Text(

                        text =
                            "Factura N° ${factura.numeroFactura}",

                        fontWeight =
                            FontWeight.Bold,

                        fontSize = 18.sp,

                        color =
                            MaterialTheme
                                .colorScheme
                                .primary
                    )

                    Spacer(
                        modifier =
                            Modifier.height(4.dp)
                    )

                    Text(
                        "Proveedor: ${factura.proveedor}"
                    )

                    Text(
                        "Producto: ${factura.descripcion}"
                    )

                    Text(
                        "Cantidad: ${factura.cantidad}"
                    )

                    Text(
                        "Precio Unitario: $ ${factura.precioUnitario}"
                    )

                    Text(
                        "Fecha: ${factura.fecha}"
                    )
                }

                Column(

                    horizontalAlignment =
                        Alignment.End

                ) {

                    Text(

                        text =
                            "$ ${
                                String.format(
                                    "%.2f",
                                    factura.total
                                )
                            }",

                        fontWeight =
                            FontWeight.ExtraBold,

                        fontSize = 20.sp
                    )

                    Row {

                        IconButton(

                            onClick = {

                                navController.navigate(NavRoutes.editarFactura(factura.id))
                            }

                        ) {

                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null
                            )
                        }

                        IconButton(

                            onClick = {

                                viewModel
                                    .eliminarFactura(
                                        factura
                                    )
                            }

                        ) {

                            Icon(

                                Icons.Default.Delete,

                                contentDescription = null,

                                tint = Color.Red
                            )
                        }
                    }
                }
            }
        }
    }
}