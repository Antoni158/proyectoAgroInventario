package com.example.inventario.ui.Movimientos




import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import com.example.inventario.ui.components.BodegaScrollableListScaffold
import androidx.compose.foundation.lazy.items

import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Warning

import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.navigation.NavController

import com.example.inventario.data.bodega.Salida
import com.example.inventario.ui.config.notifications.AppTopBar


import com.example.inventario.viewModel.SalidaViewModel
import kotlin.collections.emptyList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovimientoSalidasScreen(

    navController: NavController,

    bodegaId: String

) {

    val context = LocalContext.current

    val salidaViewModel:
            SalidaViewModel =
        viewModel()

    val salidas by salidaViewModel
        .obtenerSalidas(bodegaId)
        .collectAsState(
            initial = emptyList()
        )

    val totalSalidas =
        salidas.size

    val totalProductos =
        salidas.sumOf {
            it.cantidad
        }

    BodegaScrollableListScaffold(
        titulo = "Movimiento Salidas",
        bodegaId = bodegaId,
        navController = navController,
        containerColor = Color(0xFFF1F5F9)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MovimientoCard(
                    titulo = "Movimientos",
                    valor = totalSalidas.toString(),
                    color = Color(0xFF7B2FF7),
                    modifier = Modifier.weight(1f)
                )
                MovimientoCard(
                    titulo = "Productos",
                    valor = totalProductos.toString(),
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Text(
                text = "Historial de movimientos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (salidas.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No hay movimientos", color = Color.Gray)
                }
            }
        } else {
            items(salidas, key = { it.id }) { salida ->
                MovimientoItem(
                    salida = salida,
                    onGenerarVale = { exportarValePDF(context, salida) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun MovimientoCard(

    titulo: String,

    valor: String,

    color: Color,

    modifier: Modifier =
        Modifier

) {

    Card(

        modifier =
            modifier,

        shape =
            RoundedCornerShape(20.dp),

        colors =
            CardDefaults.cardColors(

                containerColor =
                    color
            )

    ) {

        Column(

            modifier = Modifier
                .padding(18.dp)

        ) {

            Text(

                text = valor,

                color =
                    Color.White,

                fontWeight =
                    FontWeight.Bold,

                fontSize =
                    28.sp
            )

            Spacer(
                modifier =
                    Modifier.height(6.dp)
            )

            Text(

                text = titulo,

                color =
                    Color.White
            )
        }
    }
}

@Composable
fun MovimientoItem(

    salida: Salida,

    onGenerarVale: () -> Unit

) {

    Card(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(18.dp),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )

    ) {

        Column(

            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)

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
                            salida.descripcion,

                        fontWeight =
                            FontWeight.Bold,

                        fontSize =
                            18.sp
                    )

                    Spacer(
                        modifier =
                            Modifier.height(4.dp)
                    )

                    Text(

                        text =
                            "Código: ${salida.codigoProducto}",

                        color =
                            Color.Gray
                    )
                }

                Text(

                    text =
                        "-${salida.cantidad}",

                    color =
                        Color.Red,

                    fontWeight =
                        FontWeight.Bold,

                    fontSize =
                        22.sp
                )
            }

            HorizontalDivider(

                modifier =
                    Modifier.padding(
                        vertical = 12.dp
                    )
            )

            Text(
                "Destino: ${salida.destino}"
            )

            Text(
                "Fecha: ${salida.fechaSalida}"
            )

            Spacer(
                modifier =
                    Modifier.height(14.dp)
            )

            Button(

                onClick =
                    onGenerarVale,

                modifier =
                    Modifier.fillMaxWidth()

            ) {

                Icon(

                    Icons.Default.PictureAsPdf,

                    contentDescription =
                        null
                )

                Spacer(
                    modifier =
                        Modifier.height(4.dp)
                )

                Text(
                    "Generar Vale"
                )
            }
        }
    }
}