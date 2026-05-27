package com.example.inventario.ui.salidas

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

import com.example.inventario.ui.components.BodegaScrollableListScaffold
import androidx.compose.foundation.lazy.items

import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
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

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.navigation.NavController

import com.example.inventario.data.bodega.Salida

import com.example.inventario.ui.config.notifications.AppTopBar

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import com.example.inventario.viewModel.SalidaViewModel
import com.example.inventario.viewModel.SessionManager
import com.example.inventario.viewModel.TipoBusquedaSalida

import androidx.compose.ui.platform.LocalContext
import com.example.inventario.ui.export.PdfExportProgressDialog
import com.example.inventario.ui.export.launchPdfExport
import com.example.inventario.ui.export.rememberPdfExportState
import com.example.inventario.ui.salidas.exportarSalidasExcel
import com.example.inventario.ui.salidas.generarSalidasPdfFile
import java.util.Calendar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SalidasScreen(

    navController: NavController,

    bodegaId: String

) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pdfState = rememberPdfExportState()

    val salidaViewModel:
            SalidaViewModel =
        viewModel()

    val searchQuery by salidaViewModel
        .searchQuery
        .collectAsState()

    val tipoBusqueda by salidaViewModel
        .tipoBusqueda
        .collectAsState()

    val filtroPeriodo by salidaViewModel
        .filtroPeriodo
        .collectAsState()

    val periodoTexto by salidaViewModel
        .periodoTexto
        .collectAsState()

    val fechaReferencia by salidaViewModel
        .fechaReferencia
        .collectAsState()

    val salidas by salidaViewModel
        .obtenerSalidasFiltradas(
            bodegaId
        )
        .collectAsState(
            initial = emptyList()
        )

    var showDatePicker by remember {

        mutableStateOf(false)
    }

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
                                        .apply {

                                            timeInMillis =
                                                millis +
                                                        (
                                                                1000 *
                                                                        60 *
                                                                        60 *
                                                                        24
                                                                )
                                        }

                                salidaViewModel
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

    val header = com.example.inventario.ui.components.rememberBodegaHeader(bodegaId)
    val bgColor = MaterialTheme.colorScheme.background
    val accentColor = MaterialTheme.colorScheme.primary

    PdfExportProgressDialog(pdfState)

    BodegaScrollableListScaffold(
        titulo = "Historial Salidas",
        bodegaId = bodegaId,
        navController = navController,
        containerColor = bgColor,
        floatingActionButton = {
            com.example.inventario.ui.components.ReadOnlyGate {
                if (SessionManager.tienePermiso(com.example.inventario.security.AppPermission.CREAR_SALIDA)) {
                    FloatingActionButton(
                        containerColor = accentColor,
                        onClick = { navController.navigate("crearSalida/$bodegaId") }
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Agregar",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
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
                            generarSalidasPdfFile(
                                context = context,
                                salidas = salidas,
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
                        exportarSalidasExcel(
                            context = context,
                            salidas = salidas,
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

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { salidaViewModel.setSearchQuery(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        when (tipoBusqueda) {
                            TipoBusquedaSalida.DESCRIPCION -> "Buscar por descripción..."
                            TipoBusquedaSalida.DESTINO -> "Buscar por destino..."
                            TipoBusquedaSalida.AREA -> "Buscar por área..."
                            TipoBusquedaSalida.VEHICULO -> "Buscar vehículo o placa..."
                            TipoBusquedaSalida.TODO -> "Buscar descripción, destino, área, vehículo..."
                        }
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )
        }

        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TipoBusquedaSalida.entries.forEach { tipo ->
                    FilterChip(
                        selected = tipoBusqueda == tipo,
                        onClick = { salidaViewModel.setTipoBusqueda(tipo) },
                        label = { Text(tipo.etiqueta) }
                    )
                }
            }
        }

        item {
            PeriodoTabsSalida(periodoSeleccionado = filtroPeriodo) {
                salidaViewModel.setFiltroPeriodo(it)
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
                color = accentColor
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EstadisticaCard(
                    titulo = "Salidas",
                    valor = salidas.size.toString(),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
                EstadisticaCard(
                    titulo = "Productos",
                    valor = salidas.sumOf { it.cantidad }.toString(),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (salidas.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay salidas",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(salidas, key = { it.id }) { salida ->
                SalidaCardItem(
                    salida = salida,
                    viewModel = salidaViewModel,
                    navController = navController,
                    bodegaId = bodegaId
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun EstadisticaCard(
    titulo: String,
    valor: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.14f)
        )
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                text = valor,
                color = color,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = titulo,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PeriodoTabsSalida(

    periodoSeleccionado: String,

    onPeriodoSelected:
        (String) -> Unit

) {

    val opciones = listOf(

        "Dia",
        "Semana",
        "Mes",
        "Año",
        "Todo"
    )

    val tabBg = MaterialTheme.colorScheme.surfaceVariant
    val tabSelected = MaterialTheme.colorScheme.primary
    val onTabSelected = MaterialTheme.colorScheme.onPrimary
    val onTab = MaterialTheme.colorScheme.onSurfaceVariant

    Row(

        modifier = Modifier
            .fillMaxWidth()
            .background(tabBg, RoundedCornerShape(14.dp))
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
                        if (seleccionado) tabSelected else Color.Transparent,
                        RoundedCornerShape(10.dp)
                    )
                    .clickable {

                        onPeriodoSelected(
                            opcion
                        )
                    }
                    .padding(
                        vertical = 10.dp
                    ),

                contentAlignment =
                    Alignment.Center

            ) {

                Text(

                    text = opcion,
                    color = if (seleccionado) onTabSelected else onTab
                )
            }
        }
    }
}

@Composable
fun SalidaCardItem(

    salida: Salida,

    viewModel: SalidaViewModel,

    navController: NavController,

    bodegaId: String

) {

    Card(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(18.dp),

        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)

    ) {

        Column(

            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)

        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = salida.descripcion,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Código: ${salida.codigoProducto}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (salida.numeroVale.isNotBlank()) {
                        Text(
                            text = "Vale: ${salida.numeroVale}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "-${salida.cantidad}",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                    if (SessionManager.esAdmin()) {
                        IconButton(
                            onClick = {
                                navController.navigate("editarSalida/${salida.id}/$bodegaId")
                            }
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Editar",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = { viewModel.eliminarSalida(salida) }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = Color.Red
                            )
                        }
                    }
                }
            }
        }
    }
}