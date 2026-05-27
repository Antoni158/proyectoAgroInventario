package com.example.inventario.ui.inventario

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.inventario.data.bodega.Categoria
import com.example.inventario.data.bodega.Producto
import com.example.inventario.ui.components.CategoriaSelector
import com.example.inventario.ui.components.navigateBackSafely
import com.example.inventario.ui.config.notifications.AppTopBar
import com.example.inventario.ui.config.notifications.FechaIngresar
import com.example.inventario.util.CodigoGenerator
import com.example.inventario.viewModel.CategoriaViewModel
import com.example.inventario.viewModel.ProductoViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearProductoScreen(
    navController: NavController,
    bodegaId: String,
    prefijoBodega: String
) {
    val viewModel: ProductoViewModel = viewModel()
    val categoriaViewModel: CategoriaViewModel = viewModel()
    val scope = rememberCoroutineScope()
    val categorias by categoriaViewModel.categorias.collectAsState()

    var categoriaSeleccionada by remember { mutableStateOf<Categoria?>(null) }
    var codigoGenerado by remember { mutableStateOf("") }
    var previewCodigo by remember { mutableStateOf("Seleccione categoría…") }
    var generandoCodigo by remember { mutableStateOf(false) }

    var descripcion by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var unidad by remember { mutableStateOf("Unidad") }
    var ubicacion by remember { mutableStateOf("") }
    var proveedor by remember { mutableStateOf("") }
    var costo by remember { mutableStateOf("") }
    var precioVenta by remember { mutableStateOf("") }
    var centroCosto by remember { mutableStateOf("") }
    var areaOperativa by remember { mutableStateOf("") }
    var usoOperativo by remember { mutableStateOf("") }
    var stockMinimo by remember { mutableStateOf("") }
    var lote by remember { mutableStateOf("") }
    var notas by remember { mutableStateOf("") }
    var fechaIngreso by remember {
        val c = Calendar.getInstance()
        mutableStateOf("${c.get(Calendar.DAY_OF_MONTH)}/${c.get(Calendar.MONTH) + 1}/${c.get(Calendar.YEAR)}")
    }

    LaunchedEffect(categoriaSeleccionada, bodegaId) {
        categoriaSeleccionada?.let { cat ->
            previewCodigo = categoriaViewModel.previewSiguienteCodigo(cat.id, bodegaId)
        } ?: run {
            previewCodigo = "Seleccione categoría…"
            codigoGenerado = ""
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(titulo = "Nuevo Producto", navController = navController)
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
                "Datos del producto",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Ej. tornillos de 1/2\" x 1\"") }
            )

            CategoriaSelector(
                categorias = categorias,
                categoriaSeleccionada = categoriaSeleccionada,
                onCategoriaSelected = { categoriaSeleccionada = it },
                categoriaViewModel = categoriaViewModel,
                bodegaId = bodegaId,
                modifier = Modifier.fillMaxWidth()
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (codigoGenerado.isNotBlank())
                        Color(0xFFE8F5E9)
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Código automático",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (codigoGenerado.isNotBlank()) codigoGenerado else previewCodigo,
                            style = MaterialTheme.typography.headlineMedium,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (codigoGenerado.isNotBlank()) Color(0xFF1B5E20)
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            if (categoriaSeleccionada != null)
                                "Según categoría · T0001, T0002…"
                            else
                                "Seleccione categoría para ver el código",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (generandoCodigo) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            OutlinedTextField(
                value = cantidad,
                onValueChange = { cantidad = it },
                label = { Text("Cantidad inicial") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = unidad,
                onValueChange = { unidad = it },
                label = { Text("Unidad (Litro, kg, Unidad...)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = ubicacion,
                onValueChange = { ubicacion = it },
                label = { Text("Ubicación en bodega") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = proveedor,
                onValueChange = { proveedor = it },
                label = { Text("Proveedor") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = costo,
                onValueChange = { costo = it },
                label = { Text("Costo unitario") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = precioVenta,
                onValueChange = { precioVenta = it },
                label = { Text("Precio de venta") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = centroCosto,
                onValueChange = { centroCosto = it },
                label = { Text("Centro de costo") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = areaOperativa,
                onValueChange = { areaOperativa = it },
                label = { Text("Área operativa") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = usoOperativo,
                onValueChange = { usoOperativo = it },
                label = { Text("Uso operativo") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = stockMinimo,
                onValueChange = { stockMinimo = it },
                label = { Text("Stock mínimo") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = lote,
                onValueChange = { lote = it },
                label = { Text("Lote") },
                modifier = Modifier.fillMaxWidth()
            )

            FechaIngresar(
                fecha = fechaIngreso,
                onFechaChange = { fechaIngreso = it },
                label = "Fecha Ingreso"
            )

            OutlinedTextField(
                value = notas,
                onValueChange = { notas = it },
                label = { Text("Observaciones") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val cat = categoriaSeleccionada ?: return@Button
                    if (descripcion.isBlank()) return@Button
                    val qty = cantidad.toIntOrNull() ?: 0
                    if (qty < 0 || (costo.toDoubleOrNull() ?: 0.0) < 0) return@Button
                    generandoCodigo = true
                    scope.launch {
                        try {
                            val codGenerado = categoriaViewModel.generarCodigoProducto(cat.id, bodegaId)
                            if (codGenerado != null) {
                                val existente = viewModel.obtenerProductoPorCodigo(codGenerado, bodegaId)
                                if (existente == null) {
                                    codigoGenerado = codGenerado
                                    val prefijo = CodigoGenerator.extraerPrefijo(codGenerado).orEmpty()
                                    viewModel.agregarProducto(
                                        Producto(
                                            bodegaId = bodegaId,
                                            codigoBodega = prefijoBodega,
                                            codigo = codGenerado,
                                            descripcion = descripcion.trim(),
                                            categoria = cat.nombre,
                                            prefijoCategoria = prefijo,
                                            cantidad = qty,
                                            unidad = unidad.ifBlank { "Unidad" },
                                            ubicacion = ubicacion,
                                            proveedor = proveedor,
                                            costo = costo.toDoubleOrNull() ?: 0.0,
                                            precioVenta = precioVenta.toDoubleOrNull() ?: 0.0,
                                            centroCosto = centroCosto,
                                            areaOperativa = areaOperativa,
                                            usoOperativo = usoOperativo,
                                            stockMinimo = stockMinimo.toIntOrNull()?.coerceAtLeast(0) ?: 0,
                                            lote = lote,
                                            fechaIngreso = fechaIngreso,
                                            notas = notas
                                        )
                                    )
                                    navController.navigateBackSafely(bodegaId = bodegaId)
                                }
                            }
                        } finally {
                            generandoCodigo = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = categoriaSeleccionada != null && descripcion.isNotBlank() && !generandoCodigo,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (generandoCodigo) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Text(
                        if (categoriaSeleccionada != null)
                            "Guardar producto · $previewCodigo"
                        else
                            "Seleccione categoría"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
