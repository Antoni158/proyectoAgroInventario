package com.example.inventario.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items

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

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar

import androidx.compose.runtime.Composable

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

import androidx.navigation.NavController

import com.example.inventario.security.RoleManager
import com.example.inventario.ui.components.navigateBackSafely
import com.example.inventario.ui.components.rememberBodegaHeader
import com.example.inventario.ui.config.notifications.AppTopBar
import com.example.inventario.viewModel.SessionManager

data class MenuInternoItem(

    val titulo: String,

    val descripcion: String,

    val ruta: String,

    val color: Color,

    val icono: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun MenuInternoBodegaScreen(

    navController: NavController,

    bodegaId: String

) {

    val opciones = RoleManager
        .visibleBodegaMenuKeys(SessionManager.rolActual())
        .mapNotNull { key -> bodegaMenuItemFor(key, bodegaId) }

    val header = rememberBodegaHeader(bodegaId)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                titulo = header.titulo,
                subtitulo = header.subtitulo.ifBlank { null },
                navController = navController,
                bodegaId = bodegaId
            )
        }

    ) { padding ->

        Box(

            modifier = Modifier

                .fillMaxSize()

                .background(

                    MaterialTheme
                        .colorScheme
                        .background
                )

                .padding(padding)

        ) {

            LazyVerticalGrid(

                columns =

                    GridCells.Adaptive(
                        minSize = 160.dp
                    ),

                contentPadding =
                    PaddingValues(16.dp),

                horizontalArrangement =
                    Arrangement.spacedBy(16.dp),

                verticalArrangement =
                    Arrangement.spacedBy(16.dp)

            ) {

                items(opciones) {

                        opcion ->

                    CardBodega(

                        titulo =
                            opcion.titulo,

                        descripcion =
                            opcion.descripcion,

                        color =
                            opcion.color,

                        ruta =
                            opcion.ruta,

                        icono =
                            opcion.icono,

                        onClick = {

                            navController.navigate(
                                opcion.ruta
                            )
                        }
                    )
                }
            }
        }
    }
}