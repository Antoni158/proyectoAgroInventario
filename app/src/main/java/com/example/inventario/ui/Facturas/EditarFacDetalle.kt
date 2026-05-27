package com.example.inventario.ui.Facturas

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text

import androidx.compose.runtime.*

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

import com.example.inventario.data.bodega.DetalleFactura
import com.example.inventario.ui.config.notifications.AppTopBar
import com.example.inventario.viewModel.DetalleFacturaViewModel
import com.example.inventario.ui.components.navigateBackSafely

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarDetalleFacturaScreen(

    navController: NavController,

    detalleId: Int

) {

    val viewModel:
            DetalleFacturaViewModel =
        viewModel()

    var detalleOriginal by remember {

        mutableStateOf<DetalleFactura?>(
            null
        )
    }

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

    var precio by remember {
        mutableStateOf("")
    }

    var subtotal by remember {
        mutableStateOf("")
    }

    LaunchedEffect(
        detalleId
    ) {

        val detalle =

            viewModel
                .obtenerDetallePorId(
                    detalleId
                )

        detalle?.let {

            detalleOriginal =
                it

            codigo =
                it.codigoProducto

            descripcion =
                it.descripcion

            categoria =
                it.categoria

            cantidad =
                it.cantidad
                    .toString()

            precio =
                it.precioUnitario
                    .toString()

            subtotal =
                it.subtotal
                    .toString()
        }
    }

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

            OutlinedTextField(

                value = codigo,

                onValueChange = {
                    codigo = it
                },

                label = {
                    Text("Código")
                },

                modifier =
                    Modifier.fillMaxWidth()
            )

            OutlinedTextField(

                value = descripcion,

                onValueChange = {
                    descripcion = it
                },

                label = {
                    Text("Descripción")
                },

                modifier =
                    Modifier.fillMaxWidth()
            )

            OutlinedTextField(

                value = categoria,

                onValueChange = {
                    categoria = it
                },

                label = {
                    Text("Categoría")
                },

                modifier =
                    Modifier.fillMaxWidth()
            )

            OutlinedTextField(

                value = cantidad,

                onValueChange = {

                    cantidad = it

                    val cantidadDouble =
                        it.toDoubleOrNull()
                            ?: 0.0

                    val precioDouble =
                        precio.toDoubleOrNull()
                            ?: 0.0

                    subtotal =

                        String.format(
                            "%.2f",
                            cantidadDouble * precioDouble
                        )
                },

                label = {
                    Text("Cantidad")
                },

                modifier =
                    Modifier.fillMaxWidth()
            )

            OutlinedTextField(

                value = precio,

                onValueChange = {

                    precio = it

                    val cantidadDouble =
                        cantidad.toDoubleOrNull()
                            ?: 0.0

                    val precioDouble =
                        it.toDoubleOrNull()
                            ?: 0.0

                    subtotal =

                        String.format(
                            "%.2f",
                            cantidadDouble * precioDouble
                        )
                },

                label = {
                    Text("Precio")
                },

                modifier =
                    Modifier.fillMaxWidth()
            )

            OutlinedTextField(

                value = subtotal,

                onValueChange = {},

                readOnly = true,

                label = {
                    Text("Subtotal")
                },

                modifier =
                    Modifier.fillMaxWidth()
            )

            Spacer(

                modifier =
                    Modifier.height(12.dp)
            )

            Button(

                onClick = {

                    detalleOriginal?.let {

                        val nuevoDetalle =

                            it.copy(

                                codigoProducto =
                                    codigo,

                                descripcion =
                                    descripcion,

                                categoria =
                                    categoria,

                                cantidad =

                                    cantidad.toIntOrNull()
                                        ?: 0,

                                precioUnitario =

                                    precio.toDoubleOrNull()
                                        ?: 0.0,

                                subtotal =

                                    subtotal.toDoubleOrNull()
                                        ?: 0.0
                            )

                        viewModel
                            .actualizarDetalle(
                                nuevoDetalle
                            )

                        navController.navigateBackSafely()
                    }
                },

                modifier =
                    Modifier.fillMaxWidth()

            ) {

                Text(
                    "Actualizar Producto"
                )
            }
        }
    }
}