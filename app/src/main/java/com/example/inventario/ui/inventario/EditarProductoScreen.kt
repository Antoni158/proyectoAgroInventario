package com.example.inventario.ui.inventario

import android.app.Application

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.setValue

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.navigation.NavController

import com.example.inventario.data.bodega.Producto

import com.example.inventario.ui.config.notifications.AppTopBar
import com.example.inventario.ui.config.notifications.FechaIngresar

import com.example.inventario.viewModel.ProductoViewModel
import com.example.inventario.viewModel.SessionManager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.inventario.ui.components.navigateBackSafely

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarProductoScreen(

    navController: NavController,

    productoId: Int,
    bodegaId: String

) {

    val context =
        androidx.compose.ui.platform.LocalContext.current

    val productoViewModel:
            ProductoViewModel =

        viewModel(

            factory =

                ViewModelProvider
                    .AndroidViewModelFactory
                    .getInstance(

                        context.applicationContext
                                as Application
                    )
        )

    // PRODUCTO

    var productoActual by remember {

        mutableStateOf<Producto?>(
            null
        )
    }

    // CAMPOS

    var codigo by remember {

        mutableStateOf("")
    }

    var descripcion by remember {

        mutableStateOf("")
    }

    var categoria by remember {

        mutableStateOf("")
    }

    var cantidad by remember {

        mutableStateOf("")
    }

    var unidad by remember {

        mutableStateOf("")
    }

    var ubicacion by remember {

        mutableStateOf("")
    }

    var proveedor by remember {

        mutableStateOf("")
    }

    var costo by remember {

        mutableStateOf("")
    }

    var precioVenta by remember {

        mutableStateOf("")
    }

    var centroCosto by remember { mutableStateOf("") }
    var areaOperativa by remember { mutableStateOf("") }
    var usoOperativo by remember { mutableStateOf("") }

    var stockMinimo by remember {

        mutableStateOf("")
    }

    var lote by remember {

        mutableStateOf("")
    }

    var fechaIngreso by remember {

        mutableStateOf("")
    }

    var notas by remember {

        mutableStateOf("")
    }

    // CARGAR PRODUCTO

    LaunchedEffect(

        productoId

    ) {

        val p =

            productoViewModel
                .obtenerProductoPorId(

                    productoId
                )

        productoActual = p

        p?.let {

            codigo =
                it.codigo

            descripcion =
                it.descripcion

            categoria =
                it.categoria

            cantidad =
                it.cantidad.toString()

            unidad =
                it.unidad

            ubicacion =
                it.ubicacion

            proveedor =
                it.proveedor

            costo =
                it.costo.toString()

            precioVenta =
                it.precioVenta.toString()

            centroCosto = it.centroCosto
            areaOperativa = it.areaOperativa
            usoOperativo = it.usoOperativo

            stockMinimo =
                it.stockMinimo.toString()

            lote =
                it.lote

            fechaIngreso =
                it.fechaIngreso

            notas =
                it.notas
        }
    }

    // SESION

    val usuarioActual by

    SessionManager
        .usuarioActual
        .collectAsState()

    val esAdmin =

        usuarioActual
            ?.rol == "ADMIN"

    Scaffold(

        topBar = {

            AppTopBar(

                titulo =
                    "Editar Producto",

                navController =
                    navController
            )
        }

    ) { padding ->

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(
                    rememberScrollState()
                ),

            verticalArrangement =
                Arrangement.spacedBy(12.dp)

        ) {

            Card(

                modifier =
                    Modifier.fillMaxWidth(),

                colors =

                    CardDefaults.cardColors(

                        containerColor =

                            MaterialTheme
                                .colorScheme
                                .surface
                    )
            ) {

                Column(

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),

                    verticalArrangement =
                        Arrangement.spacedBy(12.dp)

                ) {

                    // CODIGO

                    OutlinedTextField(

                        value =
                            codigo,

                        onValueChange = {},

                        enabled = false,

                        label = {

                            Text("Código")
                        },

                        modifier =
                            Modifier.fillMaxWidth()
                    )

                    // DESCRIPCION

                    OutlinedTextField(

                        value =
                            descripcion,

                        onValueChange = {

                            if (esAdmin) {

                                descripcion = it
                            }
                        },

                        readOnly =
                            !esAdmin,

                        label = {

                            Text("Descripción")
                        },

                        modifier =
                            Modifier.fillMaxWidth()
                    )

                    // CATEGORIA

                    OutlinedTextField(

                        value =
                            categoria,

                        onValueChange = {

                            if (esAdmin) {

                                categoria = it
                            }
                        },

                        readOnly =
                            !esAdmin,

                        label = {

                            Text("Categoría")
                        },

                        modifier =
                            Modifier.fillMaxWidth()
                    )

                    // CANTIDAD

                    OutlinedTextField(

                        value =
                            cantidad,

                        onValueChange = {

                            if (esAdmin) {

                                cantidad = it
                            }
                        },

                        readOnly =
                            !esAdmin,

                        label = {

                            Text("Cantidad")
                        },

                        modifier =
                            Modifier.fillMaxWidth()
                    )

                    // UNIDAD

                    OutlinedTextField(

                        value =
                            unidad,

                        onValueChange = {

                            if (esAdmin) {

                                unidad = it
                            }
                        },

                        readOnly =
                            !esAdmin,

                        label = {

                            Text("Unidad")
                        },

                        modifier =
                            Modifier.fillMaxWidth()
                    )

                    // UBICACION

                    OutlinedTextField(

                        value =
                            ubicacion,

                        onValueChange = {

                            if (esAdmin) {

                                ubicacion = it
                            }
                        },

                        readOnly =
                            !esAdmin,

                        label = {

                            Text("Ubicación")
                        },

                        modifier =
                            Modifier.fillMaxWidth()
                    )

                    // PROVEEDOR

                    OutlinedTextField(

                        value =
                            proveedor,

                        onValueChange = {

                            if (esAdmin) {

                                proveedor = it
                            }
                        },

                        readOnly =
                            !esAdmin,

                        label = {

                            Text("Proveedor")
                        },

                        modifier =
                            Modifier.fillMaxWidth()
                    )

                    // COSTO

                    OutlinedTextField(

                        value =
                            costo,

                        onValueChange = {

                            if (esAdmin) {

                                costo = it
                            }
                        },

                        readOnly =
                            !esAdmin,

                        label = {

                            Text("Costo Compra")
                        },

                        modifier =
                            Modifier.fillMaxWidth()
                    )

                    // PRECIO VENTA

                    OutlinedTextField(

                        value =
                            precioVenta,

                        onValueChange = {

                            if (esAdmin) {

                                precioVenta = it
                            }
                        },

                        readOnly =
                            !esAdmin,

                        label = {

                            Text("Precio Venta")
                        },

                        modifier =
                            Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = centroCosto,
                        onValueChange = { if (esAdmin) centroCosto = it },
                        readOnly = !esAdmin,
                        label = { Text("Centro de costo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = areaOperativa,
                        onValueChange = { if (esAdmin) areaOperativa = it },
                        readOnly = !esAdmin,
                        label = { Text("Área operativa") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = usoOperativo,
                        onValueChange = { if (esAdmin) usoOperativo = it },
                        readOnly = !esAdmin,
                        label = { Text("Uso operativo") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // STOCK MINIMO

                    OutlinedTextField(

                        value =
                            stockMinimo,

                        onValueChange = {

                            if (esAdmin) {

                                stockMinimo = it
                            }
                        },

                        readOnly =
                            !esAdmin,

                        label = {

                            Text("Stock Mínimo")
                        },

                        modifier =
                            Modifier.fillMaxWidth()
                    )

                    // LOTE

                    OutlinedTextField(

                        value =
                            lote,

                        onValueChange = {

                            if (esAdmin) {

                                lote = it
                            }
                        },

                        readOnly =
                            !esAdmin,

                        label = {

                            Text("Lote")
                        },

                        modifier =
                            Modifier.fillMaxWidth()
                    )

                    // FECHA INGRESO

                    FechaIngresar(

                        fecha =
                            fechaIngreso,

                        onFechaChange = {

                            if (esAdmin) {

                                fechaIngreso = it
                            }
                        },

                        label =
                            "Fecha Ingreso"
                    )

                    // NOTAS

                    OutlinedTextField(

                        value =
                            notas,

                        onValueChange = {

                            if (esAdmin) {

                                notas = it
                            }
                        },

                        readOnly =
                            !esAdmin,

                        label = {

                            Text("Observaciones")
                        },

                        modifier =
                            Modifier.fillMaxWidth(),

                        minLines = 4
                    )

                    Spacer(

                        modifier =
                            Modifier.height(10.dp)
                    )

                    // BOTON

                    if (esAdmin) {

                        Button(

                            modifier =
                                Modifier.fillMaxWidth(),

                            onClick = {

                                productoActual?.let {

                                    val actualizado =

                                        it.copy(

                                            descripcion =
                                                descripcion,

                                            categoria =
                                                categoria,

                                            cantidad =

                                                cantidad
                                                    .toIntOrNull()
                                                    ?: 0,

                                            unidad =
                                                unidad,

                                            ubicacion =
                                                ubicacion,

                                            proveedor =
                                                proveedor,

                                            costo =

                                                costo
                                                    .toDoubleOrNull()
                                                    ?: 0.0,

                                            precioVenta =

                                                precioVenta
                                                    .toDoubleOrNull()
                                                    ?: 0.0,

                                            centroCosto = centroCosto,
                                            areaOperativa = areaOperativa,
                                            usoOperativo = usoOperativo,

                                            stockMinimo =

                                                stockMinimo
                                                    .toIntOrNull()
                                                    ?: 0,

                                            lote =
                                                lote,

                                            fechaIngreso = fechaIngreso,

                                            notas =
                                                notas
                                        )

                                    CoroutineScope(

                                        Dispatchers.IO

                                    ).launch {

                                        productoViewModel
                                            .actualizarProducto(

                                                actualizado
                                            )
                                    }

                                    navController.navigateBackSafely()
                                }
                            }

                        ) {

                            Text(
                                "Actualizar Producto"
                            )
                        }
                    }
                }
            }
        }
    }
}