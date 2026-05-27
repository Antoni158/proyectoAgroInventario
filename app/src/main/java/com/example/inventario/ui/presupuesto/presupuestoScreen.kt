package com.example.inventario.ui.presupuesto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.inventario.navigation.NavRoutes
import com.example.inventario.ui.components.BodegaScrollableListScaffold
import com.example.inventario.viewModel.PresupuestoViewModel
import com.example.inventario.viewModel.TipoPeriodoPresupuesto
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PresupuestoScreen(
    navController: NavController,
    bodegaId: String,
    viewModel: PresupuestoViewModel = viewModel()
) {
    LaunchedEffect(bodegaId) {
        viewModel.cargar(bodegaId)
        viewModel.sincronizarDesdeFirebase(bodegaId)
    }
    val resumen by viewModel.resumen.collectAsState()

    if (bodegaId.isBlank()) {
        androidx.compose.material3.Scaffold(
            topBar = {
                com.example.inventario.ui.components.BodegaAppTopBar(
                    titulo = "Panel Financiero",
                    bodegaId = bodegaId,
                    navController = navController,
                    detalleExtra = "Presupuesto"
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Seleccione una bodega", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    } else {
        BodegaScrollableListScaffold(
            titulo = "Panel Financiero",
            bodegaId = bodegaId,
            navController = navController,
            detalleExtra = "Presupuesto"
        ) {
            item {
                ConfigurarPresupuestoCard(
                    bodegaId = bodegaId,
                    resumen = resumen,
                    onPeriodoChange = { tipo, anio, indice ->
                        viewModel.setPeriodoActivo(tipo, anio, indice)
                    },
                    onGuardar = { monto, notas ->
                        viewModel.guardarPresupuesto(bodegaId, monto, notas)
                    }
                )
            }
            item { ResumenFinanciero(state = resumen) }
            item {
                Button(
                    onClick = { navController.navigate(NavRoutes.reportesOperativos(bodegaId)) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reportes operativos (costos por área, vehículo, campo)")
                }
            }
            item { GraficaPresupuesto(meses = resumen.meses) }
            item {
                MovimientosFinancierosRecientes(
                    ingresos = resumen.ingresosTotales,
                    egresos = resumen.egresosTotales,
                    utilidad = resumen.utilidad
                )
            }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ConfigurarPresupuestoCard(
    bodegaId: String,
    resumen: com.example.inventario.viewModel.ResumenFinancieroState,
    onPeriodoChange: (tipo: String, anio: Int, indice: Int) -> Unit,
    onGuardar: (monto: Double, notas: String) -> Unit
) {
    var tipo by remember { mutableStateOf(TipoPeriodoPresupuesto.MENSUAL) }
    var anio by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var indice by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.MONTH) + 1) }
    var montoTexto by remember { mutableStateOf("") }
    var notas by remember { mutableStateOf("") }

    LaunchedEffect(tipo, anio, indice) {
        onPeriodoChange(tipo.name, anio, indice)
    }

    LaunchedEffect(resumen.presupuestoMetaPeriodo, resumen.tipoPeriodoActivo, resumen.indicePeriodoActivo) {
        if (resumen.presupuestoMetaPeriodo > 0) {
            montoTexto = "%.2f".format(resumen.presupuestoMetaPeriodo)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Presupuesto por bodega", fontWeight = FontWeight.Bold)
            Text(
                "Defina el monto meta mensual, trimestral, semestral o anual para esta bodega.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text("Tipo de período", style = MaterialTheme.typography.labelMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TipoPeriodoPresupuesto.entries.forEach { t ->
                    FilterChip(
                        selected = tipo == t,
                        onClick = {
                            tipo = t
                            indice = 1
                            onPeriodoChange(t.name, anio, indice)
                        },
                        label = { Text(t.etiqueta) }
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = anio.toString(),
                    onValueChange = { v ->
                        v.toIntOrNull()?.let {
                            anio = it
                            onPeriodoChange(tipo.name, anio, indice)
                        }
                    },
                    label = { Text("Año") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = indice.toString(),
                    onValueChange = { v ->
                        v.toIntOrNull()?.let {
                            indice = it.coerceAtLeast(1)
                            onPeriodoChange(tipo.name, anio, indice)
                        }
                    },
                    label = { Text(etiquetaIndice(tipo)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = montoTexto,
                onValueChange = { montoTexto = it },
                label = { Text("Monto (Q)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
            OutlinedTextField(
                value = notas,
                onValueChange = { notas = it },
                label = { Text("Notas (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Button(
                onClick = {
                    val monto = montoTexto.replace(",", ".").toDoubleOrNull() ?: return@Button
                    onGuardar(monto, notas)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = bodegaId.isNotBlank() && montoTexto.isNotBlank()
            ) {
                Text("Guardar presupuesto")
            }

            if (resumen.presupuestosGuardados.isNotEmpty()) {
                Text("Presupuestos registrados", fontWeight = FontWeight.SemiBold)
                resumen.presupuestosGuardados.take(6).forEach { p ->
                    Text(
                        "${p.tipoPeriodo} ${p.anio} · ${etiquetaIndice(TipoPeriodoPresupuesto.valueOf(p.tipoPeriodo))} ${p.indicePeriodo}: Q ${"%.2f".format(p.monto)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

private fun etiquetaIndice(tipo: TipoPeriodoPresupuesto): String = when (tipo) {
    TipoPeriodoPresupuesto.MENSUAL -> "Mes (1-12)"
    TipoPeriodoPresupuesto.TRIMESTRAL -> "Trimestre (1-4)"
    TipoPeriodoPresupuesto.SEMESTRAL -> "Semestre (1-2)"
    TipoPeriodoPresupuesto.ANUAL -> "Período"
}

@Composable
private fun MovimientosFinancierosRecientes(
    ingresos: Double,
    egresos: Double,
    utilidad: Double
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Actividad financiera reciente", style = MaterialTheme.typography.titleMedium)
        Text("Ingresos por salidas registradas: Q %.2f".format(ingresos))
        Text("Gastos por entradas y facturas: Q %.2f".format(egresos))
        Text(
            "Utilidad neta estimada: Q %.2f".format(utilidad),
            color = if (utilidad >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
    }
}
