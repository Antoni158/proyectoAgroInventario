package com.example.inventario.security

import android.content.Context
import android.content.SharedPreferences

object AppPreferences {

    private const val PREFS = "inventario_agro_prefs"
    private const val PREFS_LEGACY = "erp_inventario_prefs"
    private const val KEY_NOTIFICACIONES = "notificaciones_activas"
    private const val KEY_NOTIF_ENTRADAS = "notif_entradas"
    private const val KEY_NOTIF_SALIDAS = "notif_salidas"
    private const val KEY_NOTIF_STOCK_BAJO = "notif_stock_bajo"
    private const val KEY_NOTIF_AUDITORIA = "notif_auditoria"
    private const val KEY_NOTIF_CRITICOS = "notif_criticos"
    private const val KEY_NOTIF_SONIDOS = "notif_sonidos"
    private const val KEY_NOTIF_VIBRACION = "notif_vibracion"
    private const val KEY_TEMA = "tema_global"
    private const val KEY_PANEL_COMPACTO = "panel_compacto"
    private const val KEY_REMEMBER_SESSION = "remember_session"
    private const val KEY_SAVED_USERNAME = "saved_username"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        if (!::prefs.isInitialized) {
            val appContext = context.applicationContext
            prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            migrarPrefsLegacy(appContext)
        }
    }

    private fun migrarPrefsLegacy(context: Context) {
        if (prefs.getBoolean("_migrated_from_legacy", false)) return
        val legacy = context.getSharedPreferences(PREFS_LEGACY, Context.MODE_PRIVATE)
        if (legacy.all.isEmpty()) {
            prefs.edit().putBoolean("_migrated_from_legacy", true).apply()
            return
        }
        val editor = prefs.edit()
        legacy.all.forEach { (key, value) ->
            when (value) {
                is Boolean -> editor.putBoolean(key, value)
                is String -> editor.putString(key, value)
                is Int -> editor.putInt(key, value)
                is Long -> editor.putLong(key, value)
                is Float -> editor.putFloat(key, value)
            }
        }
        editor.putBoolean("_migrated_from_legacy", true).apply()
    }

    var notificacionesActivas: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICACIONES, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFICACIONES, value).apply()

    var notifEntradas: Boolean
        get() = prefs.getBoolean(KEY_NOTIF_ENTRADAS, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIF_ENTRADAS, value).apply()

    var notifSalidas: Boolean
        get() = prefs.getBoolean(KEY_NOTIF_SALIDAS, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIF_SALIDAS, value).apply()

    var notifStockBajo: Boolean
        get() = prefs.getBoolean(KEY_NOTIF_STOCK_BAJO, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIF_STOCK_BAJO, value).apply()

    var notifAuditoria: Boolean
        get() = prefs.getBoolean(KEY_NOTIF_AUDITORIA, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIF_AUDITORIA, value).apply()

    var notifCriticos: Boolean
        get() = prefs.getBoolean(KEY_NOTIF_CRITICOS, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIF_CRITICOS, value).apply()

    var notifSonidos: Boolean
        get() = prefs.getBoolean(KEY_NOTIF_SONIDOS, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIF_SONIDOS, value).apply()

    var notifVibracion: Boolean
        get() = prefs.getBoolean(KEY_NOTIF_VIBRACION, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIF_VIBRACION, value).apply()

    var accentTheme: String
        get() = prefs.getString("accent_theme", "verde") ?: "verde"
        set(value) = prefs.edit().putString("accent_theme", value).apply()

    var temaGlobal: String
        get() = prefs.getString(KEY_TEMA, "system") ?: "system"
        set(value) = prefs.edit().putString(KEY_TEMA, value).apply()

    var darkModeEnabled: Boolean
        get() = temaGlobal == "dark"
        set(value) {
            temaGlobal = if (value) "dark" else "light"
        }

    var panelCompacto: Boolean
        get() = prefs.getBoolean(KEY_PANEL_COMPACTO, false)
        set(value) = prefs.edit().putBoolean(KEY_PANEL_COMPACTO, value).apply()

    var rememberSession: Boolean
        get() = prefs.getBoolean(KEY_REMEMBER_SESSION, true)
        set(value) = prefs.edit().putBoolean(KEY_REMEMBER_SESSION, value).apply()

    var savedUsername: String
        get() = prefs.getString(KEY_SAVED_USERNAME, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_SAVED_USERNAME, value).apply()

    fun permiteNotificacionTipo(tipo: String): Boolean {
        if (!notificacionesActivas) return false
        return when (tipo.uppercase()) {
            "ENTRADA" -> notifEntradas
            "SALIDA" -> notifSalidas
            "STOCK_BAJO" -> notifStockBajo
            "AUDITORIA" -> notifAuditoria
            "CRITICO" -> notifCriticos || notifStockBajo
            else -> true
        }
    }

    fun guardarSesion(username: String) {
        prefs.edit()
            .putString(KEY_SAVED_USERNAME, username)
            .putBoolean(KEY_REMEMBER_SESSION, true)
            .apply()
    }

    fun limpiarSesion() {
        prefs.edit().remove(KEY_SAVED_USERNAME).apply()
    }
}
