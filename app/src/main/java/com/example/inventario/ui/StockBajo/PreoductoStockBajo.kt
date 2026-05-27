package com.example.inventario.ui.StockBajo



import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

import com.example.inventario.ui.config.notifications.AppTopBar
import com.example.inventario.viewModel.ProductoViewModel

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun ProductoStockScreen(

    navController: NavController,

    bodegaId: String

) {

    val productoViewModel:
            ProductoViewModel =
        viewModel()

    val productos by

    productoViewModel
        .obtenerProductos(
            bodegaId
        )
        .collectAsState(
            initial = emptyList()
        )

    Scaffold(

        topBar = {

            AppTopBar(

                titulo =
                    "Stock",

                subtitulo =
                    "Control inventario",

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
        ) {

            // KPI

            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(12.dp)

            ) {

                ProductoStockKPI(

                    titulo =
                        "Productos",

                    valor =
                        productos.size
                            .toString(),

                    color =
                        Color(0xFF2962FF),

                    modifier =
                        Modifier.weight(1f)
                )

                ProductoStockKPI(

                    titulo =
                        "Críticos",

                    valor =

                        productos.count {

                            it.cantidad <= 5
                        }.toString(),

                    color =
                        Color.Red,

                    modifier =
                        Modifier.weight(1f)
                )
            }

            Spacer(

                modifier =
                    Modifier.height(20.dp)
            )

            Text(

                text =
                    "Estado Inventario",

                fontSize =
                    22.sp,

                fontWeight =
                    FontWeight.Bold
            )

            Spacer(

                modifier =
                    Modifier.height(14.dp)
            )

            LazyColumn(

                verticalArrangement =
                    Arrangement.spacedBy(14.dp)

            ) {

                items(productos) {

                        producto ->

                    val porcentaje =

                        if (

                            producto.stockMinimo > 0

                        ) {

                            producto.cantidad
                                .toFloat() /

                                    (
                                            producto.stockMinimo
                                                .toFloat() * 2f
                                            )

                        } else 0f

                    val colorEstado =

                        when {

                            producto.cantidad <= 5 ->
                                Color.Red

                            producto.cantidad <=
                                    producto.stockMinimo ->

                                Color(0xFFFF9800)

                            else ->
                                Color(0xFF00C853)
                        }

                    val estado =

                        when {

                            producto.cantidad <= 5 ->
                                "CRÍTICO"

                            producto.cantidad <=
                                    producto.stockMinimo ->

                                "BAJO"

                            else ->
                                "NORMAL"
                        }

                    Card(

                        modifier =
                            Modifier.fillMaxWidth(),

                        shape =
                            RoundedCornerShape(22.dp),

                        colors =
                            CardDefaults.cardColors(

                                containerColor =

                                    MaterialTheme
                                        .colorScheme
                                        .surface
                            ),

                        elevation =
                            CardDefaults.cardElevation(
                                defaultElevation = 6.dp
                            )
                    ) {

                        Column(

                            modifier =
                                Modifier.padding(18.dp)
                        ) {

                            Row(

                                modifier =
                                    Modifier.fillMaxWidth(),

                                horizontalArrangement =
                                    Arrangement.SpaceBetween
                            ) {

                                Column {

                                    Text(

                                        text =
                                            producto.descripcion,

                                        fontSize =
                                            18.sp,

                                        fontWeight =
                                            FontWeight.Bold
                                    )

                                    Spacer(

                                        modifier =
                                            Modifier.height(4.dp)
                                    )

                                    Text(

                                        text =
                                            "Código: ${producto.codigo}",

                                        color =
                                            Color.Gray
                                    )
                                }

                                Box(

                                    modifier = Modifier

                                        .background(

                                            colorEstado.copy(
                                                alpha = 0.15f
                                            ),

                                            RoundedCornerShape(
                                                12.dp
                                            )
                                        )

                                        .padding(

                                            horizontal = 12.dp,

                                            vertical = 6.dp
                                        )
                                ) {

                                    Text(

                                        text = estado,

                                        color =
                                            colorEstado,

                                        fontWeight =
                                            FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(

                                modifier =
                                    Modifier.height(16.dp)
                            )

                            LinearProgressIndicator(

                                progress = {

                                    porcentaje
                                        .coerceIn(
                                            0f,
                                            1f
                                        )
                                },

                                modifier = Modifier

                                    .fillMaxWidth()

                                    .height(10.dp),

                                color =
                                    colorEstado
                            )

                            Spacer(

                                modifier =
                                    Modifier.height(12.dp)
                            )

                            Row(

                                modifier =
                                    Modifier.fillMaxWidth(),

                                horizontalArrangement =
                                    Arrangement.SpaceBetween
                            ) {

                                Text(

                                    text =
                                        "Actual: ${producto.cantidad}"
                                )

                                Text(

                                    text =
                                        "Mínimo: ${producto.stockMinimo}"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductoStockKPI(

    titulo: String,

    valor: String,

    color: Color,

    modifier: Modifier = Modifier

) {

    Card(

        modifier = modifier,

        shape =
            RoundedCornerShape(18.dp),

        colors =
            CardDefaults.cardColors(

                containerColor =
                    color.copy(alpha = 0.12f)
            )
    ) {

        Column(

            modifier = Modifier
                .padding(18.dp),

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Text(

                text = valor,

                fontSize = 28.sp,

                fontWeight = FontWeight.Bold,

                color = color
            )

            Spacer(

                modifier =
                    Modifier.height(8.dp)
            )

            Text(
                text = titulo
            )
        }
    }
}
