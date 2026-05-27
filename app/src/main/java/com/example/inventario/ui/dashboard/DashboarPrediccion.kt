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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun DashboardPrediction(
    predicciones: List<PrediccionStock>
) {
    if (predicciones.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { predicciones.size })
    val scope = rememberCoroutineScope()

    LaunchedEffect(predicciones.size) {
        if (predicciones.size <= 1) return@LaunchedEffect
        while (true) {
            delay(4500)
            val next = (pagerState.currentPage + 1) % predicciones.size
            pagerState.animateScrollToPage(next)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            PrediccionCard(predicciones[page])
        }

        if (predicciones.size > 1) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        scope.launch {
                            val prev = (pagerState.currentPage - 1).coerceAtLeast(0)
                            pagerState.animateScrollToPage(prev)
                        }
                    },
                    enabled = pagerState.currentPage > 0
                ) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Anterior")
                }

                repeat(predicciones.size) { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (pagerState.currentPage == index) 10.dp else 7.dp)
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == index) Color(0xFF2E7D32)
                                else Color.Gray.copy(alpha = 0.4f)
                            )
                    )
                }

                IconButton(
                    onClick = {
                        scope.launch {
                            val next = (pagerState.currentPage + 1).coerceAtMost(predicciones.lastIndex)
                            pagerState.animateScrollToPage(next)
                        }
                    },
                    enabled = pagerState.currentPage < predicciones.lastIndex
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Siguiente")
                }
            }
            Text(
                text = "${pagerState.currentPage + 1} / ${predicciones.size} productos en riesgo",
                modifier = Modifier.fillMaxWidth(),
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun PrediccionCard(item: PrediccionStock) {
    val colorEstado = when {
        item.diasRestantes <= 3 -> Color.Red
        item.diasRestantes <= 7 -> Color(0xFFFF9800)
        else -> Color(0xFF2E7D32)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A1A25),
                            Color(0xFF23233A),
                            Color(0xFF101018)
                        )
                    )
                )
                .padding(22.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = colorEstado)
                Text(
                    text = "Predicción Inteligente",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = item.nombre,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 26.sp
            )
            Text(
                text = "${item.codigo} · Stock ${item.stockActual} / mín. ${item.stockMinimo}",
                color = Color.LightGray,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "El sistema detectó riesgo de agotamiento",
                color = Color.LightGray,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = colorEstado.copy(alpha = 0.12f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(text = "Tiempo estimado", color = Color.LightGray)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${item.diasRestantes} días",
                        color = colorEstado,
                        fontWeight = FontWeight.Bold,
                        fontSize = 40.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = when {
                    item.diasRestantes <= 3 -> "Acción inmediata recomendada"
                    item.diasRestantes <= 7 -> "Monitoreo recomendado"
                    else -> "Inventario en límite"
                },
                color = colorEstado,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
