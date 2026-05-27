package com.example.inventario.ui.config.notifications

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.inventario.ui.branding.BrandLogoCompact
import com.example.inventario.ui.components.ProfileTopBarAction
import com.example.inventario.ui.components.ProfileTopBarTitleBlock
import com.example.inventario.ui.components.navigateBackSafely
import com.example.inventario.viewModel.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    titulo: String,
    subtitulo: String? = null,
    navController: NavController,
    bodegaId: String? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    showBrandLogo: Boolean = true,
    showProfileAccess: Boolean = SessionManager.haySesion(),
    welcomeMode: Boolean = false
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showBrandLogo) {
                    BrandLogoCompact(height = 44.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                }
                ProfileTopBarTitleBlock(
                    titulo = titulo,
                    navController = navController,
                    subtitulo = subtitulo,
                    welcomeMode = welcomeMode
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = { navController.navigateBackSafely(bodegaId = bodegaId) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
            }
        },
        actions = {
            if (showProfileAccess) {
                ProfileTopBarAction(navController)
            }
        },
        scrollBehavior = scrollBehavior,
        windowInsets = TopAppBarDefaults.windowInsets,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            scrolledContainerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.primary,
            actionIconContentColor = MaterialTheme.colorScheme.primary
        )
    )
}
