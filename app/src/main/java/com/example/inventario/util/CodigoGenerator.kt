package com.example.inventario.util

import com.example.inventario.data.bodega.Categoria
import java.text.Normalizer

object CodigoGenerator {

    private const val DIGITOS_BODEGA = 6
    private const val DIGITOS_PRODUCTO = 4

    /** Bodegas / entradas / salidas: zacapa000001 */
    private val CODIGO_SLUG_REGEX = Regex("^([a-z][a-z0-9]{0,24})(\\d{$DIGITOS_BODEGA})$")

    /** Productos: T0001, TU0001 */
    private val CODIGO_AUTO_REGEX = Regex("^[A-Za-z]{1,3}\\d{$DIGITOS_PRODUCTO}$")

    /** Legacy: TOR-0001 */
    private val CODIGO_LEGACY_REGEX = Regex("^[A-Za-z]{2,4}-\\d{4}$")

    fun primeraPalabra(texto: String): String =
        texto.trim().split(Regex("\\s+")).firstOrNull().orEmpty()
            .uppercase()
            .filter { it.isLetter() }

    fun slugDesdeNombre(texto: String): String {
        val sinAcentos = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
        val palabra = sinAcentos.split(Regex("\\s+")).firstOrNull().orEmpty()
        return palabra.lowercase().filter { it.isLetterOrDigit() }.ifBlank { "item" }
    }

    // ── Bodegas / movimientos (slug + 6 dígitos) ─────────────────────────────

    fun construirCodigoSlug(base: String, correlativo: Int): String {
        val slug = base.lowercase().filter { it.isLetterOrDigit() }.ifBlank { "item" }
        return "$slug${correlativo.toString().padStart(DIGITOS_BODEGA, '0')}"
    }

    fun esCodigoSlug(codigo: String): Boolean =
        CODIGO_SLUG_REGEX.matches(codigo.trim().lowercase())

    /** @deprecated alias */
    fun esCodigoNuevo(codigo: String): Boolean = esCodigoSlug(codigo)

    fun generarCodigoBodega(nombre: String, codigosExistentes: List<String>): String =
        generarSiguienteSlug(nombre, codigosExistentes)

    fun generarSiguienteSlug(nombre: String, codigosExistentes: List<String>): String {
        val slug = slugDesdeNombre(nombre)
        return construirCodigoSlug(slug, siguienteCorrelativoSlug(slug, codigosExistentes))
    }

    fun generarCodigoTipo(tipo: String, codigosExistentes: List<String>): String =
        generarSiguienteSlug(tipo, codigosExistentes)

    fun siguienteCorrelativoSlug(base: String, codigos: List<String>): Int {
        val slug = slugDesdeNombre(base)
        val max = codigos.mapNotNull { cod ->
            val b = extraerBaseSlug(cod) ?: return@mapNotNull null
            if (!b.equals(slug, ignoreCase = true)) return@mapNotNull null
            extraerCorrelativoSlug(cod)
        }.maxOrNull() ?: 0
        return max + 1
    }

    fun extraerBaseSlug(codigo: String): String? {
        CODIGO_SLUG_REGEX.matchEntire(codigo.trim().lowercase())?.let {
            return it.groupValues[1]
        }
        return null
    }

    fun extraerCorrelativoSlug(codigo: String): Int? {
        CODIGO_SLUG_REGEX.matchEntire(codigo.trim().lowercase())?.let {
            return it.groupValues[2].toIntOrNull()
        }
        return null
    }

    /** Alias histórico para bodegas */
    fun generarSiguiente(nombre: String, codigosExistentes: List<String>): String =
        generarSiguienteSlug(nombre, codigosExistentes)

    fun construirCodigo(base: String, correlativo: Int): String =
        construirCodigoSlug(base, correlativo)

    // ── Productos / categorías (prefijo + 4 dígitos) ─────────────────────────

    fun construirCodigoAutomatico(prefijo: String, correlativo: Int): String {
        val p = prefijo.filter { it.isLetter() }.uppercase().ifBlank { "PRD" }
        return "$p${correlativo.toString().padStart(DIGITOS_PRODUCTO, '0')}"
    }

    /**
     * Resuelve prefijo según categoría o descripción.
     * Tornillo → T; Tubería (T ocupado por otra familia) → TU.
     */
    fun resolverPrefijoDescripcion(
        texto: String,
        productosExistentes: List<Pair<String, String>>
    ): String {
        val palabra = primeraPalabra(texto)
        if (palabra.isBlank()) return "PRD"

        for (len in 1..minOf(3, palabra.length)) {
            val candidato = palabra.take(len)
            val conflicto = productosExistentes.any { (codigo, familia) ->
                val prefExistente = extraerPrefijoProducto(codigo) ?: return@any false
                if (!prefExistente.equals(candidato, ignoreCase = true)) return@any false
                val palabraExistente = primeraPalabra(familia)
                !mismaFamiliaProducto(palabra, palabraExistente)
            }
            if (!conflicto) return candidato
        }
        return palabra.take(3).ifBlank { "PRD" }
    }

    private fun mismaFamiliaProducto(nueva: String, existente: String): Boolean {
        if (existente.isBlank()) return false
        if (nueva.equals(existente, ignoreCase = true)) return true
        val minLen = minOf(nueva.length, existente.length, 3).coerceAtLeast(2)
        return nueva.take(minLen).equals(existente.take(minLen), ignoreCase = true)
    }

    fun prefijoDesdeCategoria(
        nombreCategoria: String,
        prefijoGuardado: String = "",
        productosExistentes: List<Pair<String, String>> = emptyList(),
        otrasCategorias: List<Pair<String, String>> = emptyList()
    ): String {
        val guardado = normalizarPrefijoGuardado(prefijoGuardado)
        if (guardado != null) return guardado
        return resolverPrefijoDescripcion(
            nombreCategoria,
            combinarFamilias(productosExistentes, otrasCategorias)
        )
    }

    private fun normalizarPrefijoGuardado(prefijo: String): String? {
        val p = prefijo.trim().filter { it.isLetter() }.uppercase()
        return p.takeIf { it.length in 1..3 }
    }

    private fun combinarFamilias(
        productos: List<Pair<String, String>>,
        categorias: List<Pair<String, String>>
    ): List<Pair<String, String>> {
        val deCategorias = categorias.mapNotNull { (pref, nombre) ->
            val p = normalizarPrefijoGuardado(pref)
                ?: normalizarPrefijoGuardado(primeraPalabra(nombre).take(1))
                ?: return@mapNotNull null
            construirCodigoAutomatico(p, 0) to nombre
        }
        return productos + deCategorias
    }

    fun previewCodigoCategoria(
        nombreCategoria: String,
        prefijoGuardado: String,
        productosExistentes: List<Pair<String, String>>,
        otrasCategorias: List<Pair<String, String>> = emptyList()
    ): String {
        if (nombreCategoria.isBlank()) return "Seleccione categoría…"
        val familias = combinarFamilias(productosExistentes, otrasCategorias)
        val prefijo = prefijoDesdeCategoria(
            nombreCategoria,
            prefijoGuardado,
            productosExistentes,
            otrasCategorias
        )
        val siguiente = siguienteCorrelativoPrefijo(prefijo, familias.map { it.first })
        return construirCodigoAutomatico(prefijo, siguiente)
    }

    fun generarCodigoCategoria(
        nombreCategoria: String,
        prefijoGuardado: String,
        productosExistentes: List<Pair<String, String>>,
        otrasCategorias: List<Pair<String, String>> = emptyList()
    ): String = previewCodigoCategoria(
        nombreCategoria,
        prefijoGuardado,
        productosExistentes,
        otrasCategorias
    )

    fun previewCodigoDescripcion(
        descripcion: String,
        productosExistentes: List<Pair<String, String>>
    ): String {
        if (descripcion.isBlank()) return "Escriba descripción…"
        return previewCodigoCategoria(descripcion, "", productosExistentes)
    }

    fun siguienteCorrelativoPrefijo(prefijo: String, codigos: List<String>): Int {
        val max = codigos.mapNotNull { cod ->
            val p = extraerPrefijoProducto(cod) ?: return@mapNotNull null
            if (!p.equals(prefijo, ignoreCase = true)) return@mapNotNull null
            extraerCorrelativoProducto(cod)
        }.maxOrNull() ?: 0
        return max + 1
    }

    fun extraerPrefijoProducto(codigo: String): String? {
        val t = codigo.trim()
        CODIGO_AUTO_REGEX.matchEntire(t)?.let {
            return t.takeWhile { c -> c.isLetter() }.uppercase()
        }
        CODIGO_LEGACY_REGEX.matchEntire(t)?.let {
            return t.substringBefore("-").uppercase()
        }
        return null
    }

    fun extraerCorrelativoProducto(codigo: String): Int? {
        val t = codigo.trim()
        CODIGO_AUTO_REGEX.matchEntire(t)?.let {
            return t.takeLast(DIGITOS_PRODUCTO).toIntOrNull()
        }
        return t.substringAfterLast("-").toIntOrNull()
    }

    // ── Validación / extracción unificada ────────────────────────────────────

    fun esCodigoValido(codigo: String): Boolean {
        val t = codigo.trim()
        return esCodigoSlug(t) ||
            CODIGO_AUTO_REGEX.matches(t) ||
            CODIGO_LEGACY_REGEX.matches(t)
    }

    fun esCodigoLegacy(codigo: String): Boolean {
        val t = codigo.trim()
        return t.isNotBlank() && !esCodigoSlug(t) &&
            !CODIGO_AUTO_REGEX.matches(t)
    }

    fun extraerBase(codigo: String): String? =
        extraerBaseSlug(codigo) ?: extraerPrefijoProducto(codigo)?.lowercase()

    fun extraerPrefijo(codigo: String): String? = extraerPrefijoProducto(codigo)
        ?: extraerBaseSlug(codigo)

    fun extraerCorrelativo(codigo: String): Int? =
        extraerCorrelativoSlug(codigo) ?: extraerCorrelativoProducto(codigo)

    fun generarPrefijo(nombre: String, prefijoExistente: String = ""): String =
        prefijoDesdeCategoria(nombre, prefijoExistente)

    fun previewSiguienteCodigo(categoria: Categoria?): String {
        if (categoria == null) return "---"
        val prefijo = prefijoDesdeCategoria(categoria.nombre, categoria.prefijo)
        return construirCodigoAutomatico(
            prefijo,
            (categoria.correlativoActual + 1).coerceAtLeast(1)
        )
    }

    /** @deprecated usar prefijoDesdeCategoria */
    fun slugDesdeCategoria(nombreCategoria: String, slugGuardado: String = ""): String =
        prefijoDesdeCategoria(nombreCategoria, slugGuardado).lowercase()
}
