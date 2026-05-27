package com.example.inventario.viewModel

import com.example.inventario.security.AppPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object AppThemeState {

    private val _tema = MutableStateFlow("verde")
    val tema = _tema.asStateFlow()

    private val _darkMode = MutableStateFlow(false)
    val darkMode = _darkMode.asStateFlow()

    fun initFromPreferences() {
        _tema.value = AppPreferences.accentTheme
        _darkMode.value = when (AppPreferences.temaGlobal) {
            "dark" -> true
            "light" -> false
            else -> _darkMode.value
        }
    }

    fun cambiarTema(nuevo: String) {
        AppPreferences.accentTheme = nuevo
        _tema.value = nuevo
        if (nuevo == "oscuro") {
            setDarkMode(true)
        }
    }

    fun setDarkMode(enabled: Boolean) {
        AppPreferences.temaGlobal = if (enabled) "dark" else "light"
        _darkMode.value = enabled
    }
}
