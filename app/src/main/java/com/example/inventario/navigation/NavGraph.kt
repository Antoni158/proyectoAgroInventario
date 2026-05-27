package com.example.inventario.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.inventario.security.AppPermission
import com.example.inventario.ui.CrearEntradasScreen
import com.example.inventario.ui.EditarEntradaScreen
import com.example.inventario.ui.Facturas.CrearDetalleFacturaScreen
import com.example.inventario.ui.Facturas.CrearFacturaScreen
import com.example.inventario.ui.Facturas.EditarDetalleFacturaScreen
import com.example.inventario.ui.Facturas.EditarFacturaScreen
import com.example.inventario.ui.Facturas.FacturaDetallesScreen
import com.example.inventario.ui.Facturas.FacturasScreen
import com.example.inventario.ui.Kardex.KardexScreen
import com.example.inventario.ui.Movimientos.MovimientoSalidasScreen
import com.example.inventario.ui.StockBajo.StockBajoScreen
import com.example.inventario.ui.Vales.CrearTrasladoScreen
import com.example.inventario.ui.Vales.CrearValeScreen
import com.example.inventario.ui.Vales.DetalleValeScreen
import com.example.inventario.ui.Vales.TrasladoScreen
import com.example.inventario.ui.Vales.ValeSalidaScreen
import com.example.inventario.ui.auditoria.AuditoriaHubScreen
import com.example.inventario.ui.auditoria.AuditoriaScreen
import com.example.inventario.ui.categorias.CategoriasScreen
import com.example.inventario.ui.components.ModuleRouteGuard
import com.example.inventario.ui.components.ScreenWithSafeBack
import com.example.inventario.ui.config.PerfilUsuarioScreen
import com.example.inventario.ui.config.CrearUsuarioScreen
import com.example.inventario.ui.config.TemasScreen
import com.example.inventario.ui.config.UsuariosScreen
import com.example.inventario.ui.config.notifications.EditarUsuarioScreen
import com.example.inventario.ui.Notificaciones.NotificacionesScreen
import com.example.inventario.ui.dashboard.DashboardScreen
import com.example.inventario.ui.entradas.EntradasScreen
import com.example.inventario.ui.inventario.CrearProductoScreen
import com.example.inventario.ui.inventario.EditarProductoScreen
import com.example.inventario.ui.inventario.InventarioScreen
import com.example.inventario.ui.logs.LogsScreen
import com.example.inventario.ui.login.LoginScreen
import com.example.inventario.ui.login.RecuperarContrasenaScreen
import com.example.inventario.ui.login.SplashScreen
import com.example.inventario.ui.menu.CrearBodegaScreen
import com.example.inventario.ui.menu.MenuInternoBodegaScreen
import com.example.inventario.ui.menu.MenuPScreen
import com.example.inventario.ui.menu.MenuPrincipalScreen
import com.example.inventario.ui.papelera.PapeleraScreen
import com.example.inventario.ui.panel.PanelPrincipalScreen
import com.example.inventario.ui.presupuesto.PresupuestoScreen
import com.example.inventario.ui.reportes.ReportesOperativosScreen
import com.example.inventario.ui.salidas.CrearSalidasScreen
import com.example.inventario.ui.salidas.EditarSalidaScreen
import com.example.inventario.ui.salidas.SalidasScreen
import com.example.inventario.viewModel.SessionManager

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val bodegaArg = listOf(navArgument("bodegaId") { type = NavType.StringType })

    NavHost(navController = navController, startDestination = NavRoutes.SPLASH) {

        composable(NavRoutes.SPLASH) { SplashScreen(navController) }
        composable(NavRoutes.LOGIN) { LoginScreen(navController) }
        composable(NavRoutes.RECUPERAR_PASSWORD) {
            ScreenWithSafeBack(navController) { RecuperarContrasenaScreen(navController) }
        }

        composable(NavRoutes.MENU_PRINCIPAL) {
            ScreenWithSafeBack(navController) { MenuPrincipalScreen(navController) }
        }

        composable(NavRoutes.MENU_BODEGAS) {
            ScreenWithSafeBack(navController) {
                ModuleRouteGuard(AppPermission.VER_MODULO_BODEGA, navController) {
                    MenuPScreen(navController)
                }
            }
        }

        composable(NavRoutes.PANEL) {
            ScreenWithSafeBack(navController) {
                ModuleRouteGuard(AppPermission.VER_MODULO_PANEL, navController) {
                    PanelPrincipalScreen(navController)
                }
            }
        }

        composable(NavRoutes.AUDITORIA_HUB) {
            ScreenWithSafeBack(navController) {
                ModuleRouteGuard(AppPermission.VER_MODULO_AUDITORIA, navController) {
                    AuditoriaHubScreen(navController)
                }
            }
        }

        composable(NavRoutes.CONFIGURACION) {
            ScreenWithSafeBack(navController) { PerfilUsuarioScreen(navController, viewModel()) }
        }

        composable(NavRoutes.PERFIL) {
            ScreenWithSafeBack(navController) { PerfilUsuarioScreen(navController, viewModel()) }
        }

        composable(NavRoutes.CREAR_BODEGA) {
            ScreenWithSafeBack(navController) { CrearBodegaScreen(navController, viewModel()) }
        }

        composable(NavRoutes.USUARIOS) {
            ScreenWithSafeBack(navController) {
                ModuleRouteGuard(AppPermission.ADMINISTRAR_USUARIOS, navController) {
                    UsuariosScreen(navController)
                }
            }
        }

        composable(NavRoutes.CREAR_USUARIO) {
            ScreenWithSafeBack(navController) { CrearUsuarioScreen(navController, viewModel()) }
        }

        composable(
            NavRoutes.editarUsuarioRoute(),
            arguments = listOf(navArgument("usuarioId") { type = NavType.IntType })
        ) { entry ->
            val id = entry.arguments?.getInt("usuarioId") ?: 0
            ScreenWithSafeBack(navController) { EditarUsuarioScreen(navController, id) }
        }

        composable(NavRoutes.LOGS) {
            ScreenWithSafeBack(navController) {
                ModuleRouteGuard(AppPermission.VER_LOGS, navController) { LogsScreen(navController) }
            }
        }

        composable(NavRoutes.NOTIFICACIONES) {
            val bodegaId = SessionManager.obtenerBodegaActual().ifBlank { "global" }
            ScreenWithSafeBack(navController) { NotificacionesScreen(navController, bodegaId) }
        }

        composable(NavRoutes.TEMAS) {
            ScreenWithSafeBack(navController) { TemasScreen(navController) }
        }

        composable(NavRoutes.PAPELERA) {
            val bodegaId = SessionManager.obtenerBodegaActual().ifBlank { "global" }
            ScreenWithSafeBack(navController) {
                ModuleRouteGuard(AppPermission.VER_PAPELERA, navController) {
                    PapeleraScreen(navController, bodegaId)
                }
            }
        }

        // ── Rutas con bodegaId ──

        composable(NavRoutes.menuBodega("{bodegaId}"), arguments = bodegaArg) { entry ->
            val bodegaId = entry.arguments?.getString("bodegaId").orEmpty()
            BindBodegaContext(bodegaId)
            ScreenWithSafeBack(navController, bodegaId) {
                MenuInternoBodegaScreen(navController, bodegaId)
            }
        }

        composable(NavRoutes.inventario("{bodegaId}"), arguments = bodegaArg) { entry ->
            val bodegaId = entry.arguments?.getString("bodegaId").orEmpty()
            BindBodegaContext(bodegaId)
            ScreenWithSafeBack(navController, bodegaId) {
                InventarioScreen(navController, bodegaId)
            }
        }

        composable(NavRoutes.crearProducto("{bodegaId}"), arguments = bodegaArg) { entry ->
            val bodegaId = entry.arguments?.getString("bodegaId").orEmpty()
            BindBodegaContext(bodegaId)
            ScreenWithSafeBack(navController, bodegaId) {
                CrearEntradasScreen(navController, bodegaId)
            }
        }

        composable(
            NavRoutes.editarProductoRoute(),
            arguments = listOf(
                navArgument("productoId") { type = NavType.IntType },
                navArgument("bodegaId") { type = NavType.StringType }
            )
        ) { entry ->
            val bodegaId = entry.arguments?.getString("bodegaId").orEmpty()
            BindBodegaContext(bodegaId)
            ScreenWithSafeBack(navController, bodegaId) {
                EditarProductoScreen(navController, entry.arguments?.getInt("productoId") ?: 0, bodegaId)
            }
        }

        composable(NavRoutes.entradas("{bodegaId}"), arguments = bodegaArg) { entry ->
            val bodegaId = entry.arguments?.getString("bodegaId").orEmpty()
            BindBodegaContext(bodegaId)
            ScreenWithSafeBack(navController, bodegaId) { EntradasScreen(navController, bodegaId) }
        }

        composable(NavRoutes.crearEntrada("{bodegaId}"), arguments = bodegaArg) { entry ->
            val bodegaId = entry.arguments?.getString("bodegaId").orEmpty()
            BindBodegaContext(bodegaId)
            ScreenWithSafeBack(navController, bodegaId) { CrearEntradasScreen(navController, bodegaId) }
        }

        composable(
            NavRoutes.editarEntradaRoute(),
            arguments = listOf(
                navArgument("entradaId") { type = NavType.IntType },
                navArgument("bodegaId") { type = NavType.StringType }
            )
        ) { entry ->
            val bodegaId = entry.arguments?.getString("bodegaId").orEmpty()
            BindBodegaContext(bodegaId)
            ScreenWithSafeBack(navController, bodegaId) {
                EditarEntradaScreen(navController, entry.arguments?.getInt("entradaId") ?: 0, bodegaId)
            }
        }

        composable(NavRoutes.salidas("{bodegaId}"), arguments = bodegaArg) { entry ->
            val bodegaId = entry.arguments?.getString("bodegaId").orEmpty()
            BindBodegaContext(bodegaId)
            ScreenWithSafeBack(navController, bodegaId) { SalidasScreen(navController, bodegaId) }
        }

        composable(NavRoutes.crearSalida("{bodegaId}"), arguments = bodegaArg) { entry ->
            val bodegaId = entry.arguments?.getString("bodegaId").orEmpty()
            BindBodegaContext(bodegaId)
            ScreenWithSafeBack(navController, bodegaId) { CrearSalidasScreen(navController, bodegaId) }
        }

        composable(
            NavRoutes.editarSalidaRoute(),
            arguments = listOf(
                navArgument("salidaId") { type = NavType.IntType },
                navArgument("bodegaId") { type = NavType.StringType }
            )
        ) { entry ->
            val bodegaId = entry.arguments?.getString("bodegaId").orEmpty()
            BindBodegaContext(bodegaId)
            ScreenWithSafeBack(navController, bodegaId) {
                EditarSalidaScreen(navController, entry.arguments?.getInt("salidaId") ?: 0, bodegaId)
            }
        }

        composable(NavRoutes.facturas("{bodegaId}"), arguments = bodegaArg) { entry ->
            val bodegaId = entry.arguments?.getString("bodegaId").orEmpty()
            BindBodegaContext(bodegaId)
            ScreenWithSafeBack(navController, bodegaId) { FacturasScreen(navController, bodegaId) }
        }

        composable(NavRoutes.crearFactura("{bodegaId}"), arguments = bodegaArg) { entry ->
            val bodegaId = entry.arguments?.getString("bodegaId").orEmpty()
            BindBodegaContext(bodegaId)
            ScreenWithSafeBack(navController, bodegaId) { CrearFacturaScreen(navController, bodegaId) }
        }

        composable(
            NavRoutes.editarFacturaRoute(),
            arguments = listOf(navArgument("facturaId") { type = NavType.IntType })
        ) { entry ->
            ScreenWithSafeBack(navController) {
                EditarFacturaScreen(navController, entry.arguments?.getInt("facturaId") ?: 0)
            }
        }

        composable(
            NavRoutes.crearDetalleFacturaRoute(),
            arguments = listOf(navArgument("facturaId") { type = NavType.IntType })
        ) { entry ->
            ScreenWithSafeBack(navController) {
                CrearDetalleFacturaScreen(navController, entry.arguments?.getInt("facturaId") ?: 0)
            }
        }

        composable(
            NavRoutes.detalleFacturaRoute(),
            arguments = listOf(navArgument("facturaId") { type = NavType.IntType })
        ) { entry ->
            ScreenWithSafeBack(navController) {
                FacturaDetallesScreen(navController, entry.arguments?.getInt("facturaId") ?: 0)
            }
        }

        composable(
            NavRoutes.editarDetalleFacturaRoute(),
            arguments = listOf(navArgument("detalleId") { type = NavType.IntType })
        ) { entry ->
            ScreenWithSafeBack(navController) {
                EditarDetalleFacturaScreen(navController, entry.arguments?.getInt("detalleId") ?: 0)
            }
        }

        composable(NavRoutes.kardex("{bodegaId}"), arguments = bodegaArg) { entry ->
            val bodegaId = entry.arguments?.getString("bodegaId").orEmpty()
            BindBodegaContext(bodegaId)
            ScreenWithSafeBack(navController, bodegaId) { KardexScreen(navController, bodegaId) }
        }

        composable(NavRoutes.movimientos("{bodegaId}"), arguments = bodegaArg) { entry ->
            val bodegaId = entry.arguments?.getString("bodegaId").orEmpty()
            BindBodegaContext(bodegaId)
            ScreenWithSafeBack(navController, bodegaId) {
                MovimientoSalidasScreen(navController, bodegaId)
            }
        }

        composable(NavRoutes.categorias("{bodegaId}"), arguments = bodegaArg) { entry ->
            val bodegaId = entry.arguments?.getString("bodegaId").orEmpty()
            BindBodegaContext(bodegaId)
            ScreenWithSafeBack(navController, bodegaId) { CategoriasScreen(navController, bodegaId) }
        }

        composable(NavRoutes.auditoria("{bodegaId}"), arguments = bodegaArg) { entry ->
            val bodegaId = entry.arguments?.getString("bodegaId").orEmpty()
            BindBodegaContext(bodegaId)
            ScreenWithSafeBack(navController, bodegaId) { AuditoriaScreen(navController, bodegaId) }
        }

        composable(NavRoutes.presupuesto("{bodegaId}"), arguments = bodegaArg) { entry ->
            val bodegaId = entry.arguments?.getString("bodegaId").orEmpty()
            BindBodegaContext(bodegaId)
            ScreenWithSafeBack(navController, bodegaId) { PresupuestoScreen(navController, bodegaId) }
        }

        composable(NavRoutes.reportesOperativos("{bodegaId}"), arguments = bodegaArg) { entry ->
            val bodegaId = entry.arguments?.getString("bodegaId").orEmpty()
            BindBodegaContext(bodegaId)
            ScreenWithSafeBack(navController, bodegaId) {
                ReportesOperativosScreen(navController, bodegaId)
            }
        }

        // Alias: panelBodega, dashboard, existencias, stockBajo, vales
        listOf(
            NavRoutes.dashboard("{bodegaId}"),
            NavRoutes.panelBodega("{bodegaId}")
        ).forEach { route ->
            composable(route, arguments = bodegaArg) { entry ->
                val bodegaId = entry.arguments?.getString("bodegaId").orEmpty()
                BindBodegaContext(bodegaId)
                ScreenWithSafeBack(navController, bodegaId) { DashboardScreen(navController, bodegaId) }
            }
        }

        listOf(
            NavRoutes.stockBajo("{bodegaId}"),
            NavRoutes.existencias("{bodegaId}")
        ).forEach { route ->
            composable(route, arguments = bodegaArg) { entry ->
                val bodegaId = entry.arguments?.getString("bodegaId").orEmpty()
                BindBodegaContext(bodegaId)
                ScreenWithSafeBack(navController, bodegaId) { StockBajoScreen(navController, bodegaId) }
            }
        }

        composable(NavRoutes.vales("{bodegaId}"), arguments = bodegaArg) { entry ->
            val bodegaId = entry.arguments?.getString("bodegaId").orEmpty()
            BindBodegaContext(bodegaId)
            ScreenWithSafeBack(navController, bodegaId) { ValeSalidaScreen(navController, bodegaId) }
        }

        composable(NavRoutes.crearVale("{bodegaId}"), arguments = bodegaArg) { entry ->
            val bodegaId = entry.arguments?.getString("bodegaId").orEmpty()
            BindBodegaContext(bodegaId)
            ScreenWithSafeBack(navController, bodegaId) { CrearValeScreen(navController, bodegaId) }
        }

        composable(
            NavRoutes.detalleValeRoute(),
            arguments = listOf(
                navArgument("valeId") { type = NavType.IntType },
                navArgument("bodegaId") { type = NavType.StringType }
            )
        ) { entry ->
            val bodegaId = entry.arguments?.getString("bodegaId").orEmpty()
            BindBodegaContext(bodegaId)
            ScreenWithSafeBack(navController, bodegaId) {
                DetalleValeScreen(navController, entry.arguments?.getInt("valeId") ?: 0, bodegaId)
            }
        }

        composable(NavRoutes.traslados("{bodegaId}"), arguments = bodegaArg) { entry ->
            val bodegaId = entry.arguments?.getString("bodegaId").orEmpty()
            BindBodegaContext(bodegaId)
            ScreenWithSafeBack(navController, bodegaId) { TrasladoScreen(navController, bodegaId) }
        }

        composable(NavRoutes.crearTraslado("{bodegaId}"), arguments = bodegaArg) { entry ->
            val bodegaId = entry.arguments?.getString("bodegaId").orEmpty()
            BindBodegaContext(bodegaId)
            ScreenWithSafeBack(navController, bodegaId) { CrearTrasladoScreen(navController, bodegaId) }
        }

        composable(NavRoutes.configuracionBodega("{bodegaId}"), arguments = bodegaArg) { entry ->
            val bodegaId = entry.arguments?.getString("bodegaId").orEmpty()
            BindBodegaContext(bodegaId)
            ScreenWithSafeBack(navController, bodegaId) {
                PerfilUsuarioScreen(navController, viewModel())
            }
        }

        composable(NavRoutes.papeleraBodega("{bodegaId}"), arguments = bodegaArg) { entry ->
            val bodegaId = entry.arguments?.getString("bodegaId").orEmpty()
            BindBodegaContext(bodegaId)
            ScreenWithSafeBack(navController, bodegaId) {
                ModuleRouteGuard(AppPermission.VER_PAPELERA, navController) {
                    PapeleraScreen(navController, bodegaId)
                }
            }
        }
    }
}
