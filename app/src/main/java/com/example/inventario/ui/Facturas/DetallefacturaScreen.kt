package com.example.inventario.ui.Facturas




import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon


import androidx.compose.material3.Scaffold

import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

import com.example.inventario.data.bodega.DetalleFactura
import com.example.inventario.data.bodega.FacturaConDetalles

import com.example.inventario.ui.config.notifications.AppTopBar
import com.example.inventario.viewModel.DetalleFacturaViewModel
import kotlin.collections.emptyList


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacturaDetallesScreen(

    navController: NavController,

    facturaId: Int

) {

    val viewModel:
            DetalleFacturaViewModel =
        viewModel()

    val detalles by

    viewModel
        .obtenerDetallesFactura(
            facturaId
        )
        .collectAsState(
            initial = emptyList()
        )

    Scaffold(

        topBar = {

            AppTopBar(

                titulo =
                    "Detalle Factura",

                navController =
                    navController
            )
        },

        floatingActionButton = {

            FloatingActionButton(

                onClick = {

                    navController.navigate(

                        "crearDetalleFactura/$facturaId"
                    )
                }

            ) {

                Icon(
                    Icons.Default.Add,
                    contentDescription = null
                )
            }
        }

    ) { padding ->

        Column(

            modifier = Modifier

                .fillMaxSize()

                .padding(padding)

                .padding(16.dp)
        ) {

            Text(

                text =
                    "Productos Facturados",

                fontSize = 22.sp,

                fontWeight =
                    FontWeight.Bold
            )

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )

            LazyColumn(

                verticalArrangement =
                    Arrangement.spacedBy(12.dp)

            ) {

                items(detalles) { detalle ->

                    DetalleFacturaCard(

                        detalle =
                            detalle,

                        navController =
                            navController
                    )
                }
            }
        }
    }
}

@Composable
fun DetalleFacturaCard(

    detalle: DetalleFactura,

    navController: NavController

) {

    Card(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(16.dp),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 4.dp
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
                    Arrangement.SpaceBetween

            ) {

                Column {

                    Text(

                        detalle.descripcion,

                        fontWeight =
                            FontWeight.Bold,

                        fontSize = 18.sp
                    )

                    Spacer(
                        modifier =
                            Modifier.height(4.dp)
                    )

                    Text(
                        "Código: ${detalle.codigoProducto}"
                    )

                    Text(
                        "Cantidad: ${detalle.cantidad}"
                    )

                    Text(
                        "Precio: Q ${detalle.precioUnitario}"
                    )

                    Text(
                        "Subtotal: Q ${detalle.subtotal}"
                    )
                }

                FloatingActionButton(

                    onClick = {

                        navController.navigate(

                            "editarDetalleFactura/${detalle.idDetalle}"
                        )
                    }

                ) {

                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null
                    )
                }
            }
        }
    }
}