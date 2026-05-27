package com.example.inventario.ui.menu

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MoveToInbox
import androidx.compose.material.icons.filled.Outbox
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.ui.graphics.Color
import com.example.inventario.navigation.NavRoutes
import com.example.inventario.security.BodegaMenuKey

fun bodegaMenuItemFor(key: BodegaMenuKey, bodegaId: String): MenuInternoItem? =
    when (key) {
        BodegaMenuKey.INVENTARIO -> MenuInternoItem(
            "Inventario", "Productos y lotes", NavRoutes.inventario(bodegaId),
            Color(0xFF2962FF), Icons.Default.Inventory
        )
        BodegaMenuKey.ENTRADAS -> MenuInternoItem(
            "Entradas", "Ingresos y compras", NavRoutes.entradas(bodegaId),
            Color(0xFF00C853), Icons.Default.MoveToInbox
        )
        BodegaMenuKey.SALIDAS -> MenuInternoItem(
            "Salidas", "Despachos y consumo", NavRoutes.salidas(bodegaId),
            Color(0xFFD50000), Icons.Default.Outbox
        )
        BodegaMenuKey.VALES -> MenuInternoItem(
            "Vales", "Traslados y salidas", NavRoutes.vales(bodegaId),
            Color(0xFFFF6D00), Icons.Default.LocalShipping
        )
        BodegaMenuKey.EXISTENCIAS -> MenuInternoItem(
            "Existencias", "Control de stock", NavRoutes.existencias(bodegaId),
            Color(0xFF0091EA), Icons.Default.Storage
        )
        BodegaMenuKey.KARDEX -> MenuInternoItem(
            "Kardex", "Historial movimientos", NavRoutes.kardex(bodegaId),
            Color(0xFF6200EA), Icons.Default.ListAlt
        )
        BodegaMenuKey.CATEGORIAS -> MenuInternoItem(
            "Categorías", "Organización productos", NavRoutes.categorias(bodegaId),
            Color(0xFFAA00FF), Icons.Default.Category
        )
        BodegaMenuKey.FACTURAS -> MenuInternoItem(
            "Facturas", "Compras y registros", NavRoutes.facturas(bodegaId),
            Color(0xFFFFAB00), Icons.Default.ReceiptLong
        )
        BodegaMenuKey.MOVIMIENTOS -> MenuInternoItem(
            "Movimientos", "Historial de salidas", NavRoutes.movimientos(bodegaId),
            Color(0xFF5C6BC0), Icons.Default.SwapHoriz
        )
        BodegaMenuKey.ANALISIS -> MenuInternoItem(
            "Análisis", "Gráficas y estadísticas", NavRoutes.panelBodega(bodegaId),
            Color(0xFF00B8D4), Icons.Default.Assessment
        )
        BodegaMenuKey.AUDITORIA_BODEGA -> MenuInternoItem(
            "Auditoría", "Conteos y ajustes", NavRoutes.auditoria(bodegaId),
            Color(0xFF455A64), Icons.Default.Security
        )
    }
