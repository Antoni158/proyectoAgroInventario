package com.example.inventario.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DashboardKPI(

    productoTop: String,

    productoCritico: String,

    valorInventario: Double,

    promedioSalidas: Int

) {

    Column(

        verticalArrangement =
            Arrangement.spacedBy(14.dp)

    ) {

        // FILA 1

        Row(

            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(14.dp)

        ) {

            KPIItem(

                modifier =
                    Modifier.weight(1f),

                titulo =
                    "Más Movido",

                valor =
                    productoTop,

                icono =
                    Icons.Default.TrendingUp,

                color1 =
                    Color(0xFF7B2FF7),

                color2 =
                    Color(0xFFB06CFF)
            )

            KPIItem(

                modifier =
                    Modifier.weight(1f),

                titulo =
                    "Crítico",

                valor =
                    productoCritico,

                icono =
                    Icons.Default.Warning,

                color1 =
                    Color(0xFFFF416C),

                color2 =
                    Color(0xFFFF4B2B)
            )
        }

        // FILA 2

        Row(

            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(14.dp)

        ) {

            KPIItem(

                modifier =
                    Modifier.weight(1f),

                titulo =
                    "Inventario",

                valor =
                    "Q %.2f".format(
                        valorInventario
                    ),

                icono =
                    Icons.Default.AttachMoney,

                color1 =
                    Color(0xFF11998E),

                color2 =
                    Color(0xFF38EF7D)
            )

            KPIItem(

                modifier =
                    Modifier.weight(1f),

                titulo =
                    "Movimientos",

                valor =
                    promedioSalidas
                        .toString(),

                icono =
                    Icons.Default.Inventory,

                color1 =
                    Color(0xFFFF9800),

                color2 =
                    Color(0xFFFFC107)
            )
        }
    }
}

@Composable
fun KPIItem(

    modifier: Modifier = Modifier,

    titulo: String,

    valor: String,

    icono: ImageVector,

    color1: Color,

    color2: Color

) {

    Card(

        modifier =
            modifier,

        shape =
            RoundedCornerShape(24.dp),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 8.dp
            ),

        colors =
            CardDefaults.cardColors(

                containerColor =
                    Color.Transparent
            )
    ) {

        Column(

            modifier = Modifier

                .background(

                    brush =
                        Brush.verticalGradient(

                            colors = listOf(

                                color1,

                                color2
                            )
                        )
                )

                .padding(18.dp)

        ) {

            Icon(

                imageVector =
                    icono,

                contentDescription =
                    titulo,

                tint =
                    Color.White
            )

            Spacer(

                modifier =
                    Modifier.height(14.dp)
            )

            Text(

                text =
                    titulo,

                color =
                    Color.White,

                fontSize =
                    14.sp
            )

            Spacer(

                modifier =
                    Modifier.height(8.dp)
            )

            Text(

                text =
                    valor,

                color =
                    Color.White,

                fontWeight =
                    FontWeight.Bold,

                fontSize =
                    20.sp,

                maxLines = 1
            )
        }
    }
}