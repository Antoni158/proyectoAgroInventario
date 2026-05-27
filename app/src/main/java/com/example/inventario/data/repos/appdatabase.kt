package com.example.inventario.data.repos

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.inventario.data.Auditoria.Auditoria
import com.example.inventario.data.Auditoria.AuditoriaDao
import com.example.inventario.data.administracion.Log
import com.example.inventario.data.administracion.LogDao
import com.example.inventario.data.administracion.Usuario
import com.example.inventario.data.administracion.UsuarioDao
import com.example.inventario.data.bodega.Bodega
import com.example.inventario.data.bodega.BodegaDao
import com.example.inventario.data.bodega.Categoria
import com.example.inventario.data.bodega.CategoriaDao
import com.example.inventario.data.bodega.DetalleFactura
import com.example.inventario.data.bodega.DetalleVale
import com.example.inventario.data.bodega.DetalleValeDao
import com.example.inventario.data.bodega.Entrada
import com.example.inventario.data.bodega.EntradaDao
import com.example.inventario.data.bodega.Factura
import com.example.inventario.data.bodega.FacturaConDetallesDao
import com.example.inventario.data.bodega.FacturaDao
import com.example.inventario.data.bodega.FacturaDetalleDao
import com.example.inventario.data.bodega.Kardex
import com.example.inventario.data.bodega.KardexDao
import com.example.inventario.data.bodega.PresupuestoBodega
import com.example.inventario.data.bodega.PresupuestoBodegaDao
import com.example.inventario.data.bodega.Producto
import com.example.inventario.data.bodega.ProductoDao
import com.example.inventario.data.bodega.Salida
import com.example.inventario.data.bodega.SalidaDao
import com.example.inventario.data.bodega.Traslado
import com.example.inventario.data.bodega.TrasladoDao
import com.example.inventario.data.bodega.Vale
import com.example.inventario.data.bodega.ValeConDetallesDao
import com.example.inventario.data.bodega.ValeDao
import com.example.inventario.data.bodega.ValeSalida
import com.example.inventario.data.bodega.ValeSalidaDao
import com.example.inventario.data.notificacion.AppNotificacion
import com.example.inventario.data.notificacion.AppNotificacionDao

@Database(
    entities = [
        Usuario::class,
        Bodega::class,
        Producto::class,
        Categoria::class,
        Entrada::class,
        Salida::class,
        Factura::class,
        DetalleFactura::class,
        Log::class,
        Auditoria::class,
        Kardex::class,
        Vale::class,
        DetalleVale::class,
        ValeSalida::class,
        Traslado::class,
        AppNotificacion::class,
        PresupuestoBodega::class
    ],
    version = 31,
    exportSchema = false
)
abstract class appdatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao

    abstract fun bodegaDao(): BodegaDao

    abstract fun productoDao(): ProductoDao

    abstract fun categoriaDao(): CategoriaDao

    abstract fun entradaDao(): EntradaDao

    abstract fun salidaDao(): SalidaDao

    abstract fun facturaDao(): FacturaDao

    abstract fun logDao(): LogDao

    abstract fun auditoriaDao(): AuditoriaDao

    abstract fun facturaConDetallesDao(): FacturaConDetallesDao

    abstract fun detalleFacturaDao(): FacturaDetalleDao

    abstract fun kardexDao(): KardexDao

    abstract fun ValeSalidaDao(): ValeSalidaDao

    abstract fun TrasladoDao(): TrasladoDao

    abstract fun ValeDao(): ValeDao

    abstract fun ValeConDetallesDao(): ValeConDetallesDao

    abstract fun DetalleValeDao(): DetalleValeDao

    abstract fun appNotificacionDao(): AppNotificacionDao

    abstract fun presupuestoBodegaDao(): PresupuestoBodegaDao

    companion object {

        @Volatile
        private var INSTANCE: appdatabase? = null

        fun getDatabase(
            context: Context
        ): appdatabase {

            return INSTANCE ?: synchronized(this) {

                val instance =
                    Room.databaseBuilder(
                        context.applicationContext,
                        appdatabase::class.java,
                        "inventario_db"
                    )
                        .fallbackToDestructiveMigration()
                        .fallbackToDestructiveMigrationOnDowngrade()
                        .build()

                INSTANCE = instance

                instance
            }
        }

        fun closeDatabase() {
            synchronized(this) {
                INSTANCE?.close()
                INSTANCE = null
            }
        }
    }
}