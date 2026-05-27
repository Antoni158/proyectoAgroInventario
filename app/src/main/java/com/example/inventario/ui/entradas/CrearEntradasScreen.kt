package com.example.inventario.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.inventario.data.bodega.Categoria
import com.example.inventario.data.bodega.Entrada
import com.example.inventario.data.bodega.Producto
import com.example.inventario.data.repos.MovimientoInventarioService
import com.example.inventario.ui.components.CategoriaSelector
import com.example.inventario.ui.components.navigateBackSafely
import com.example.inventario.ui.components.rememberBodegaHeader
import com.example.inventario.ui.config.notifications.AppTopBar
import com.example.inventario.ui.config.notifications.FechaIngresar
import com.example.inventario.util.CodigoGenerator
import com.example.inventario.viewModel.CategoriaViewModel
import com.example.inventario.viewModel.EntradaViewModel
import com.example.inventario.viewModel.ProductoViewModel
import com.example.inventario.viewModel.SessionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

private enum class ModoEntrada { EXISTENTE, NUEVO }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearEntradasScreen(
    navController: NavController,
    bodegaId: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val header = rememberBodegaHeader(bodegaId)
    val prefijoBodega = header.codigo

    val entradaViewModel: EntradaViewModel = viewModel()
    val productoViewModel: ProductoViewModel = viewModel()
    val categoriaViewModel: CategoriaViewModel = viewModel()
    val categorias by categoriaViewModel.categorias.collectAsState()

    val codigoEntrada = remember(bodegaId) { entradaViewModel.generarCodigoEntrada(bodegaId) }

    var modo by remember { mutableStateOf(ModoEntrada.EXISTENTE) }
    var guardando by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }

    // Producto existente
    var busquedaCodigo by remember { mutableStateOf("") }
    var productoEncontrado by remember { mutableStateOf<Producto?>(null) }
    var buscando by remember { mutableStateOf(false) }
    var sugerenciasProductos by remember { mutableStateOf<List<Producto>>(emptyList()) }
    var mostrarSugerenciasProd by remember { mutableStateOf(false) }
    var searchJob by remember { mutableStateOf<Job?>(null) }

    // Producto nuevo
    var categoriaSeleccionada by remember { mutableStateOf<Categoria?>(null) }
    var previewCodigo by remember { mutableStateOf("Seleccione categoría…") }

    // Campos comunes
    var descripcion by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var unidad by remember { mutableStateOf("Unidad") }
    var proveedor by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var costoEntrada by remember { mutableStateOf("") }
    var stockMinimo by remember { mutableStateOf("0") }
    var lote by remember { mutableStateOf("") }
    var notas by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var numeroFactura by remember { mutableStateOf("") }
    var tipoEntrada by remember { mutableStateOf("COMPRA") }

    var fechaIngreso by remember {
        val c = Calendar.getInstance()
        mutableStateOf("${c.get(Calendar.DAY_OF_MONTH)}/${c.get(Calendar.MONTH) + 1}/${c.get(Calendar.YEAR)}")
    }

    fun rellenarDesdeProducto(producto: Producto) {
        productoEncontrado = producto
        busquedaCodigo = producto.codigo
        descripcion = producto.descripcion
        categoria = producto.categoria
        proveedor = producto.proveedor
        unidad = producto.unidad.ifBlank { "Unidad" }
        ubicacion = producto.ubicacion
        costoEntrada = producto.costo.toString()
        stockMinimo = producto.stockMinimo.toString()
        lote = producto.lote
        notas = producto.notas
        mostrarSugerenciasProd = false
        sugerenciasProductos = emptyList()
        errorMsg = ""
    }

    LaunchedEffect(busquedaCodigo, modo) {
        if (modo != ModoEntrada.EXISTENTE) return@LaunchedEffect
        if (productoEncontrado?.codigo == busquedaCodigo) return@LaunchedEffect

        searchJob?.cancel()
        errorMsg = ""
        productoEncontrado = null

        if (busquedaCodigo.isBlank()) {
            descripcion = ""
            categoria = ""
            sugerenciasProductos = emptyList()
            mostrarSugerenciasProd = false
            return@LaunchedEffect
        }

        searchJob = scope.launch {
            delay(300)
            buscando = true
            try {
                val exacto = productoViewModel.buscarProductoPorCodigoGlobal(
                    busquedaCodigo.trim().uppercase()
                )
                if (exacto != null && exacto.bodegaId == bodegaId) {
                    rellenarDesdeProducto(exacto)
                } else {
                    val sugerencias = productoViewModel.autocompletarProducto(bodegaId, busquedaCodigo.trim())
                    sugerenciasProductos = sugerencias
                    mostrarSugerenciasProd = sugerencias.isNotEmpty()
                }
            } catch (_: Exception) {
                errorMsg = "Error buscando producto"
            } finally {
                buscando = false
            }
        }
    }

    LaunchedEffect(categoriaSeleccionada, bodegaId) {
        categoriaSeleccionada?.let { cat ->
            categoria = cat.nombre
            previewCodigo = categoriaViewModel.previewSiguienteCodigo(cat.id, bodegaId)
        } ?: run {
            previewCodigo = "Seleccione categoría…"
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(titulo = "Nueva Entrada — Abastecimiento", navController = navController)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Flujo: Entrada → Factura → Producto → Stock → Kardex",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = modo == ModoEntrada.EXISTENTE,
                    onClick = { modo = ModoEntrada.EXISTENTE; errorMsg = "" },
                    label = { Text("Producto existente") }
                )
                FilterChip(
                    selected = modo == ModoEntrada.NUEVO,
                    onClick = {
                        modo = ModoEntrada.NUEVO
                        productoEncontrado = null
                        busquedaCodigo = ""
                        errorMsg = ""
                    },
                    label = { Text("Producto nuevo") }
                )
            }

            OutlinedTextField(
                value = codigoEntrada,
                onValueChange = {},
                enabled = false,
                label = { Text("Código entrada") },
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider()

            when (modo) {
                ModoEntrada.EXISTENTE -> {
                    Text("Buscar producto", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    OutlinedTextField(
                        value = busquedaCodigo,
                        onValueChange = { busquedaCodigo = it.uppercase() },
                        label = { Text("Código o descripción") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            if (buscando) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            } else {
                                Icon(Icons.Default.Search, contentDescription = null)
                            }
                        },
                        trailingIcon = {
                            if (productoEncontrado != null) {
                                Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    )

                    AnimatedVisibility(visible = mostrarSugerenciasProd && sugerenciasProductos.isNotEmpty()) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column {
                                sugerenciasProductos.take(6).forEach { prod ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { rellenarDesdeProducto(prod) }
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

                    AnimatedVisibility(visible = productoEncontrado != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(descripcion, fontWeight = FontWeight.Bold)
                                Text("$categoria · Stock actual: ${productoEncontrado?.cantidad ?: 0}")
                            }
                        }
                    }
                }

                ModoEntrada.NUEVO -> {
                    Text("Nuevo producto", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text("Descripción del producto *") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Ej. Tornillo 1/2 x 2\"") }
                    )

                    CategoriaSelector(
                        categorias = categorias,
                        categoriaSeleccionada = categoriaSeleccionada,
                        onCategoriaSelected = { categoriaSeleccionada = it },
                        categoriaViewModel = categoriaViewModel,
                        bodegaId = bodegaId,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (categoriaSeleccionada != null) {
                        Text(
                            "Código automático: $previewCodigo",
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            HorizontalDivider()
            Text("Datos de entrada", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

            OutlinedTextField(value = cantidad, onValueChange = { cantidad = it }, label = { Text("Cantidad *") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = costoEntrada, onValueChange = { costoEntrada = it }, label = { Text("Costo unitario de esta entrada (Q) *") }, modifier = Modifier.fillMaxWidth())
            if (productoEncontrado != null) {
                val cantPreview = cantidad.toIntOrNull() ?: 0
                val costoLote = costoEntrada.toDoubleOrNull() ?: 0.0
                val stockAct = productoEncontrado?.cantidad ?: 0
                val costoAct = productoEncontrado?.costo ?: 0.0
                val promedio = if (cantPreview > 0 && stockAct + cantPreview > 0) {
                    ((stockAct * costoAct) + (cantPreview * costoLote)) / (stockAct + cantPreview)
                } else costoAct
                Text(
                    "Costo estándar resultante: Q ${"%.2f".format(promedio)} (promedio ponderado)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            } else if (modo == ModoEntrada.NUEVO) {
                Text(
                    "El costo ingresado será el costo estándar inicial del producto.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedTextField(value = unidad, onValueChange = { unidad = it }, label = { Text("Unidad") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = proveedor, onValueChange = { proveedor = it }, label = { Text("Proveedor *") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = ubicacion, onValueChange = { ubicacion = it }, label = { Text("Ubicación") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = numeroFactura, onValueChange = { numeroFactura = it }, label = { Text("N° Factura") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = lote, onValueChange = { lote = it }, label = { Text("Lote") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = stockMinimo, onValueChange = { stockMinimo = it }, label = { Text("Stock mínimo") }, modifier = Modifier.fillMaxWidth())

            FechaIngresar(fecha = fechaIngreso, onFechaChange = { fechaIngreso = it }, label = "Fecha")

            OutlinedTextField(
                value = notas,
                onValueChange = { notas = it },
                label = { Text("Notas") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            if (errorMsg.isNotBlank()) {
                Text(errorMsg, color = MaterialTheme.colorScheme.error)
            }

            val puedeGuardar = when (modo) {
                ModoEntrada.EXISTENTE -> productoEncontrado != null && cantidad.isNotBlank()
                ModoEntrada.NUEVO -> categoriaSeleccionada != null && descripcion.isNotBlank() && cantidad.isNotBlank()
            }

            Button(
                onClick = {
                    scope.launch {
                        guardando = true
                        errorMsg = ""
                        try {
                            val cantidadInt = cantidad.toIntOrNull() ?: 0
                            if (cantidadInt <= 0) {
                                errorMsg = "Cantidad debe ser mayor a 0"
                                return@launch
                            }
                            val costoDouble = costoEntrada.toDoubleOrNull() ?: 0.0
                            if (costoDouble < 0) {
                                errorMsg = "Costo inválido"
                                return@launch
                            }
                            if (proveedor.isBlank()) {
                                errorMsg = "Ingrese proveedor"
                                return@launch
                            }

                            val productoNuevo: Producto? = if (modo == ModoEntrada.NUEVO) {
                                val cat = categoriaSeleccionada ?: return@launch
                                if (descripcion.isBlank()) {
                                    errorMsg = "Ingrese descripción del producto"
                                    return@launch
                                }
                                val codGenerado = categoriaViewModel.generarCodigoProducto(
                                    cat.id,
                                    bodegaId
                                ) ?: run {
                                    errorMsg = "No se pudo generar código"
                                    return@launch
                                }
                                val prefijoCodigo = CodigoGenerator.extraerPrefijo(codGenerado).orEmpty()
                                Producto(
                                    bodegaId = bodegaId,
                                    codigoBodega = prefijoBodega,
                                    codigo = codGenerado,
                                    descripcion = descripcion.trim(),
                                    categoria = cat.nombre,
                                    prefijoCategoria = prefijoCodigo,
                                    cantidad = 0,
                                    unidad = unidad.ifBlank { "Unidad" },
                                    ubicacion = ubicacion,
                                    proveedor = proveedor.trim(),
                                    costo = costoDouble,
                                    precioVenta = 0.0,
                                    stockMinimo = stockMinimo.toIntOrNull()?.coerceAtLeast(0) ?: 0,
                                    lote = lote,
                                    fechaIngreso = fechaIngreso,
                                    notas = notas
                                )
                            } else null

                            val codigoProducto = productoNuevo?.codigo
                                ?: productoEncontrado?.codigo.orEmpty()

                            if (codigoProducto.isBlank()) {
                                errorMsg = "Seleccione un producto"
                                return@launch
                            }

                            val entrada = Entrada(
                                codigoEntrada = codigoEntrada,
                                codigoProducto = codigoProducto,
                                descripcion = descripcion.ifBlank { productoEncontrado?.descripcion.orEmpty() },
                                bodegaId = bodegaId,
                                codigoBodega = productoEncontrado?.codigoBodega ?: prefijoBodega,
                                categoria = categoria.ifBlank { productoEncontrado?.categoria.orEmpty() },
                                cantidad = cantidadInt,
                                unidad = unidad.ifBlank { "Unidad" },
                                proveedor = proveedor.trim(),
                                ubicacion = ubicacion,
                                lote = lote,
                                costoEntrada = costoDouble,
                                precioVenta = 0.0,
                                presupuesto = cantidadInt * costoDouble,
                                stockMinimo = stockMinimo.toIntOrNull()?.coerceAtLeast(0)
                                    ?: productoEncontrado?.stockMinimo ?: 0,
                                numeroFactura = numeroFactura.trim(),
                                tipoEntrada = tipoEntrada,
                                fechaIngreso = fechaIngreso,
                                usuario = SessionManager.usernameUsuario(),
                                notas = notas
                            )

                            when (val r = entradaViewModel.registrarEntradaCompleta(entrada, productoNuevo)) {
                                is MovimientoInventarioService.ResultadoMovimiento.EntradaOk -> {
                                    Toast.makeText(
                                        context,
                                        "Entrada registrada · Factura ${r.factura.numeroFactura}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    navController.navigateBackSafely(bodegaId = bodegaId)
                                }
                                is MovimientoInventarioService.ResultadoMovimiento.Error -> {
                                    errorMsg = r.mensaje
                                }
                                else -> errorMsg = "No se pudo registrar la entrada"
                            }
                        } finally {
                            guardando = false
                        }
                    }
                },
                enabled = puedeGuardar && !guardando,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (guardando) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Registrar entrada y actualizar stock")
                }
            }
        }
    }
}
