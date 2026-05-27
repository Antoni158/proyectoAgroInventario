package com.example.inventario.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inventario.data.bodega.Bodega
import com.example.inventario.data.bodega.BodegaCodigoUtil
import com.example.inventario.viewModel.BodegaViewModel

data class BodegaHeaderInfo(
    val titulo: String,
    val subtitulo: String,
    val codigo: String,
    val nombre: String
)

@Composable
fun rememberBodegaHeader(
    bodegaId: String,
    tituloPorDefecto: String? = null,
    viewModel: BodegaViewModel = viewModel()
): BodegaHeaderInfo {
    var bodega by remember(bodegaId) { mutableStateOf<Bodega?>(null) }
    LaunchedEffect(bodegaId) {
        if (bodegaId.isNotBlank()) {
            bodega = viewModel.obtenerBodega(bodegaId)
        }
    }
    val titulo = tituloPorDefecto ?: if (bodega != null) "Panel de ${bodega?.nombre}" else "Panel Agrícola"
    val sub = if (bodega != null) "${bodega?.nombre} (${bodega?.codigoCorto})" else ""
    return BodegaHeaderInfo(
        titulo = titulo,
        subtitulo = sub,
        codigo = bodega?.codigoCorto.orEmpty(),
        nombre = bodega?.nombre.orEmpty()
    )
}

/** Evita mostrar UUID en subtítulos de AppTopBar. */
fun bodegaSubtituloSeguro(bodegaId: String, header: BodegaHeaderInfo): String {
    if (header.subtitulo.isNotBlank()) return header.subtitulo
    if (header.codigo.isNotBlank()) return header.codigo
    if (bodegaId.length > 12) return ""
    return bodegaId
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodegaAppTopBar(
    titulo: String,
    bodegaId: String,
    navController: androidx.navigation.NavController,
    detalleExtra: String? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    viewModel: BodegaViewModel = viewModel(),
    showProfileAccess: Boolean = true
) {
    val header = rememberBodegaHeader(bodegaId, viewModel = viewModel)
    val base = bodegaSubtituloSeguro(bodegaId, header)
    val subtitulo = when {
        detalleExtra != null && base.isNotBlank() -> "$detalleExtra · $base"
        detalleExtra != null -> detalleExtra
        base.isNotBlank() -> base
        else -> null
    }
    com.example.inventario.ui.config.notifications.AppTopBar(
        titulo = titulo,
        subtitulo = subtitulo,
        navController = navController,
        bodegaId = bodegaId,
        scrollBehavior = scrollBehavior,
        showProfileAccess = showProfileAccess
    )
}

/** Etiqueta para PDF/export sin UUID. */
fun etiquetaBodegaExport(header: BodegaHeaderInfo, bodegaId: String): String {
    val nombre = header.nombre.ifBlank { header.codigo }
    return when {
        nombre.isNotBlank() && header.codigo.isNotBlank() -> "$nombre (${header.codigo})"
        nombre.isNotBlank() -> nombre
        header.codigo.isNotBlank() -> header.codigo
        bodegaId.length <= 12 -> bodegaId
        else -> "Bodega"
    }
}
