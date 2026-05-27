package com.example.inventario.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width

import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Warning

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DashboardStatus(

    totalProductos: Int,

    stockBajo: Int,

    existencias: Int

) {

    val estadoSistema =

        when {

            stockBajo >= 10 ->
                "CRÍTICO"

            stockBajo >= 5 ->
                "RIESGO"

            else ->
                "ESTABLE"
        }

    val colorEstado =

        when {

            stockBajo >= 10 ->
                Color.Red

            stockBajo >= 5 ->
                Color(0xFFFF9800)

            else ->
                Color(0xFF00C853)
        }

    val porcentajeSalud =

        if (totalProductos == 0) {

            0f

        } else {

            (
                    totalProductos - stockBajo
                    ).toFloat() /

                    totalProductos.toFloat()
        }

    Card(

        modifier =
            Modifier.fillMaxWidth(),

        shape =
            RoundedCornerShape(28.dp),

        colors =
            CardDefaults.cardColors(

                containerColor =
                    Color(0xFF14141D)
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )

    ) {

        Column(

            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),

            verticalArrangement =
                Arrangement.spacedBy(18.dp)

        ) {

            // TITULO

            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.CenterVertically

            ) {

                Column {

                    Text(

                        text =
                            "Estado Sistema",

                        color =
                            Color.White,

                        fontWeight =
                            FontWeight.Bold,

                        fontSize =
                            24.sp
                    )

                    Spacer(
                        modifier =
                            Modifier.height(4.dp)
                    )

                    Text(

                        text =
                            "Monitoreo agrícola",

                        color =
                            Color.LightGray
                    )
                }

                Box(

                    modifier = Modifier

                        .background(

                            brush =
                                Brush.horizontalGradient(

                                    colors = listOf(

                                        colorEstado,

                                        colorEstado.copy(
                                            alpha = 0.5f
                                        )
                                    )
                                ),

                            shape =
                                RoundedCornerShape(16.dp)
                        )

                        .padding(

                            horizontal = 18.dp,

                            vertical = 10.dp
                        )
                ) {

                    Text(

                        text =
                            estadoSistema,

                        color =
                            Color.White,

                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }

            // BARRA

            Column {

                Text(

                    text =
                        "Salud Inventario",

                    color =
                        Color.White
                )

                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )

                LinearProgressIndicator(

                    progress = {

                        porcentajeSalud
                    },

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(12.dp),

                    color =
                        colorEstado,

                    trackColor =
                        Color.DarkGray
                )
            }

            // ESTADISTICAS

            Row(

                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween

            ) {

                StatusItem(

                    titulo =
                        "Productos",

                    valor =
                        totalProductos.toString(),

                    iconoColor =
                        Color(0xFF2979FF)
                )

                StatusItem(

                    titulo =
                        "Críticos",

                    valor =
                        stockBajo.toString(),

                    iconoColor =
                        Color.Red
                )

                StatusItem(

                    titulo =
                        "Existencias",

                    valor =
                        existencias.toString(),

                    iconoColor =
                        Color(0xFF00C853)
                )
            }

            // ALERTA

            Row(

                verticalAlignment =
                    Alignment.CenterVertically

            ) {

                Icon(

                    imageVector =

                        when {

                            stockBajo >= 10 ->
                                Icons.Default.Warning

                            stockBajo >= 5 ->
                                Icons.Default.Inventory

                            else ->
                                Icons.Default.CheckCircle
                        },

                    contentDescription = null,

                    tint =
                        colorEstado
                )

                Spacer(
                    modifier =
                        Modifier.width(8.dp)
                )

                Text(

                    text =

                        when {

                            stockBajo >= 10 ->
                                "Inventario en estado crítico"

                            stockBajo >= 5 ->
                                "Se detectó riesgo moderado"

                            else ->
                                "Sistema funcionando correctamente"
                        },

                    color =
                        Color.White
                )
            }
        }
    }
}

@Composable
fun StatusItem(

    titulo: String,

    valor: String,

    iconoColor: Color

) {

    Column(

        horizontalAlignment =
            Alignment.CenterHorizontally

    ) {

        Box(

            modifier = Modifier

                .background(

                    iconoColor.copy(
                        alpha = 0.15f
                    ),

                    RoundedCornerShape(14.dp)
                )

                .padding(14.dp)

        ) {

            Text(

                text = valor,

                color =
                    iconoColor,

                fontWeight =
                    FontWeight.Bold,

                fontSize =
                    20.sp
            )
        }

        Spacer(
            modifier =
                Modifier.height(6.dp)
        )

        Text(

            text = titulo,

            color =
                Color.LightGray
        )
    }
}