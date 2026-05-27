package com.example.inventario.ui.export

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class PdfExportState {
    var exporting by mutableStateOf(false)
    var progress by mutableStateOf(0f)
    var message by mutableStateOf<String?>(null)
    var error by mutableStateOf<String?>(null)

    fun reset() {
        exporting = false
        progress = 0f
        message = null
        error = null
    }
}

@Composable
fun rememberPdfExportState(): PdfExportState = remember { PdfExportState() }

@Composable
fun PdfExportProgressDialog(state: PdfExportState, onDismiss: () -> Unit = { state.reset() }) {
    if (!state.exporting && state.message == null && state.error == null) return

    AlertDialog(
        onDismissRequest = {
            if (!state.exporting) onDismiss()
        },
        confirmButton = {
            if (!state.exporting) {
                TextButton(onClick = onDismiss) { Text("Cerrar") }
            }
        },
        title = {
            Text(
                when {
                    state.exporting -> "Generando PDF…"
                    state.error != null -> "Error al exportar"
                    else -> "PDF listo"
                }
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when {
                    state.exporting -> {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(progress = { state.progress.coerceIn(0f, 1f) })
                        Spacer(Modifier.height(8.dp))
                        Text("Procesando documento…", style = MaterialTheme.typography.bodySmall)
                    }
                    state.error != null -> Text(state.error.orEmpty(), color = MaterialTheme.colorScheme.error)
                    else -> Text(state.message.orEmpty())
                }
            }
        }
    )
}

fun launchPdfExport(
    context: Context,
    state: PdfExportState,
    scope: kotlinx.coroutines.CoroutineScope,
    generateFile: suspend (onProgress: (Float) -> Unit) -> File
) {
    if (state.exporting) return
    state.reset()
    state.exporting = true
    state.progress = 0.05f

    scope.launch {
        try {
            val file = withContext(Dispatchers.IO) {
                generateFile { p -> state.progress = p }
            }
            state.progress = 1f
            withContext(Dispatchers.Main) {
                ExportShareUtil.abrirPdf(context, file)
            }
            state.message = "Archivo generado correctamente"
        } catch (e: Exception) {
            state.error = e.message ?: "No se pudo generar el PDF"
        } finally {
            state.exporting = false
        }
    }
}
