package com.example.inventario.ui.papelera

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Input
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Warehouse
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.inventario.ui.config.notifications.AppTopBar
import com.example.inventario.viewModel.BodegaViewModel
import com.example.inventario.viewModel.CategoriaViewModel
import com.example.inventario.viewModel.EntradaViewModel
import com.example.inventario.viewModel.FacturaViewModel
import com.example.inventario.viewModel.ProductoViewModel
import com.example.inventario.viewModel.SalidaViewModel
import com.example.inventario.viewModel.UsuarioViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PapeleraScreen(

    navController: NavController,

    bodegaId: String

) {

    val productoViewModel: ProductoViewModel =
        viewModel()

    val entradaViewModel: EntradaViewModel =
        viewModel()

    val salidaViewModel: SalidaViewModel =
        viewModel()

    val facturaViewModel: FacturaViewModel =
        viewModel()

    val bodegaViewModel: BodegaViewModel =
        viewModel()

    val usuarioViewModel: UsuarioViewModel =
        viewModel()

    val categoriaViewModel: CategoriaViewModel =
        viewModel()

    var viewState by remember {

        mutableStateOf(
            PapeleraView.Main
        )
    }

    LaunchedEffect(Unit) {

        productoViewModel.purgarAntiguos()

        entradaViewModel.purgarAntiguos()

        salidaViewModel.purgarAntiguos()

        facturaViewModel.purgarAntiguos()

        bodegaViewModel.purgarAntiguos()

        usuarioViewModel.purgarAntiguos()

        categoriaViewModel.purgarAntiguos()
    }

    Scaffold(

        containerColor =
            MaterialTheme.colorScheme.background,

        topBar = {

            AppTopBar(

                titulo = "Papelera",

                navController =
                    navController
            )
        }

    ) { padding ->

        Box(

            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)

        ) {

            when (viewState) {

                PapeleraView.Main -> {

                    PapeleraMenu {
                        viewState = it
                    }
                }

                PapeleraView.Productos -> {

                    ProductosPapelera(
                        productoViewModel
                    )
                }

                PapeleraView.Entradas -> {

                    EntradasPapelera(
                        entradaViewModel
                    )
                }

                PapeleraView.Salidas -> {

                    SalidasPapelera(
                        salidaViewModel
                    )
                }

                PapeleraView.Facturas -> {

                    FacturasPapelera(
                        facturaViewModel
                    )
                }

                PapeleraView.Bodegas -> {

                    BodegasPapelera(
                        bodegaViewModel
                    )
                }

                PapeleraView.Usuarios -> {

                    UsuariosPapelera(
                        usuarioViewModel
                    )
                }

                PapeleraView.Categorias -> {

                    CategoriasPapelera(
                        categoriaViewModel
                    )
                }
            }
        }
    }
}

enum class PapeleraView {

    Main,
    Productos,
    Entradas,
    Salidas,
    Facturas,
    Bodegas,
    Usuarios,
    Categorias
}

@Composable
fun PapeleraMenu(
    onNavigate: (PapeleraView) -> Unit
) {

    Column(

        verticalArrangement =
            Arrangement.spacedBy(12.dp)

    ) {

        PapeleraMenuOption(
            "Productos",
            Icons.Default.Inventory
        ) {
            onNavigate(PapeleraView.Productos)
        }

        PapeleraMenuOption(
            "Entradas",
            Icons.AutoMirrored.Filled.Input
        ) {
            onNavigate(PapeleraView.Entradas)
        }

        PapeleraMenuOption(
            "Salidas",
            Icons.AutoMirrored.Filled.Logout
        ) {
            onNavigate(PapeleraView.Salidas)
        }

        PapeleraMenuOption(
            "Facturas",
            Icons.Default.Receipt
        ) {
            onNavigate(PapeleraView.Facturas)
        }

        PapeleraMenuOption(
            "Categorías",
            Icons.Default.Category
        ) {
            onNavigate(PapeleraView.Categorias)
        }

        PapeleraMenuOption(
            "Bodegas",
            Icons.Default.Warehouse
        ) {
            onNavigate(PapeleraView.Bodegas)
        }

        PapeleraMenuOption(
            "Usuarios",
            Icons.Default.People
        ) {
            onNavigate(PapeleraView.Usuarios)
        }
    }
}

@Composable
fun PapeleraMenuOption(
    titulo: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {

    Card(

        onClick = onClick,

        modifier =
            Modifier.fillMaxWidth()

    ) {

        Row(

            modifier =
                Modifier.padding(18.dp),

            verticalAlignment =
                Alignment.CenterVertically

        ) {

            Icon(
                icono,
                null
            )

            Spacer(
                modifier =
                    Modifier.width(16.dp)
            )

            Text(
                titulo,
                modifier =
                    Modifier.weight(1f)
            )

            Icon(
                Icons.Default.ChevronRight,
                null
            )
        }
    }
}

@Composable
fun ProductosPapelera(
    viewModel: ProductoViewModel
) {
    Text("Productos eliminados")
}

@Composable
fun EntradasPapelera(
    viewModel: EntradaViewModel
) {
    Text("Entradas eliminadas")
}

@Composable
fun SalidasPapelera(
    viewModel: SalidaViewModel
) {
    Text("Salidas eliminadas")
}

@Composable
fun FacturasPapelera(
    viewModel: FacturaViewModel
) {
    Text("Facturas eliminadas")
}

@Composable
fun BodegasPapelera(
    viewModel: BodegaViewModel
) {
    val bodegas by viewModel.obtenerPapelera().collectAsState(initial = emptyList())
    val fmt = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    if (bodegas.isEmpty()) {
        Text(
            "No hay bodegas en la papelera",
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(bodegas, key = { it.id }) { bodega ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(bodega.nombre, fontWeight = FontWeight.Bold)
                        Text(
                            "${bodega.codigoCorto} · ${bodega.descripcion.ifBlank { "Sin descripción" }}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        bodega.deletionDate?.let {
                            Text(
                                "Eliminada: ${fmt.format(Date(it))}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    IconButton(onClick = { viewModel.restaurarBodega(bodega) }) {
                        Icon(Icons.Default.Restore, contentDescription = "Restaurar", tint = Color(0xFF2E7D32))
                    }
                    IconButton(onClick = { viewModel.eliminarPermanente(bodega) }) {
                        Icon(Icons.Default.DeleteForever, contentDescription = "Eliminar permanente", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun UsuariosPapelera(
    viewModel: UsuarioViewModel
) {
    Text("Usuarios eliminados")
}

@Composable
fun CategoriasPapelera(
    viewModel: CategoriaViewModel
) {
    Text("Categorías eliminadas")
}