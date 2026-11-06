package co.edu.uniquindio.petadoptionapp.features.moderator

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.domain.model.post.PriorityLevel

/**
Utilidades de presentación compartidas por las pantallas del moderador (inicio y "Mis revisiones").
Traducen la prioridad sugerida por la IA (PriorityLevel) a un color del tema y a un texto localizado,
para que todas las tarjetas y chips de prioridad se vean igual. No tienen estado ni llaman casos de uso:
solo reciben la prioridad (o null si la IA aún no la calculó) y devuelven el valor visual correspondiente.
**/

// Color y texto de la prioridad sugerida por la IA (inicio y publicaciones del moderador).

// Devuelve el color del tema asociado a la prioridad indicada.
@Composable
fun priorityColor(priority: PriorityLevel?): Color = when (priority) {
    // Las prioridades urgente y alta se pintan con el color de error (rojo).
    PriorityLevel.URGENT, PriorityLevel.HIGH -> AppTheme.colors.stateError
    // La prioridad media se pinta con el color de advertencia.
    PriorityLevel.MEDIUM -> AppTheme.colors.stateWarning
    // La prioridad baja se pinta con el color de éxito.
    PriorityLevel.LOW -> AppTheme.colors.stateSuccess
    // Sin prioridad (aún sin análisis) se usa el color deshabilitado.
    null -> AppTheme.colors.stateDisabled
}

// Devuelve el texto localizado que describe la prioridad indicada.
@Composable
fun priorityText(priority: PriorityLevel?): String = stringResource(
    // Elige el recurso de texto según el nivel de prioridad.
    when (priority) {
        // Texto para prioridad urgente.
        PriorityLevel.URGENT -> R.string.mod_priority_urgent
        // Texto para prioridad alta.
        PriorityLevel.HIGH -> R.string.mod_priority_high
        // Texto para prioridad media.
        PriorityLevel.MEDIUM -> R.string.mod_priority_medium
        // Texto para prioridad baja.
        PriorityLevel.LOW -> R.string.mod_priority_low
        // Texto para cuando la publicación no tiene prioridad asignada.
        null -> R.string.mod_priority_none
    }
)
