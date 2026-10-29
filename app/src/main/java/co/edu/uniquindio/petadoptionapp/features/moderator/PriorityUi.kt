package co.edu.uniquindio.petadoptionapp.features.moderator

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.domain.model.post.PriorityLevel

// Color y texto de la prioridad sugerida por la IA (inicio y publicaciones del moderador).

@Composable
fun priorityColor(priority: PriorityLevel?): Color = when (priority) {
    PriorityLevel.URGENT, PriorityLevel.HIGH -> AppTheme.colors.stateError
    PriorityLevel.MEDIUM -> AppTheme.colors.stateWarning
    PriorityLevel.LOW -> AppTheme.colors.stateSuccess
    null -> AppTheme.colors.stateDisabled
}

@Composable
fun priorityText(priority: PriorityLevel?): String = stringResource(
    when (priority) {
        PriorityLevel.URGENT -> R.string.mod_priority_urgent
        PriorityLevel.HIGH -> R.string.mod_priority_high
        PriorityLevel.MEDIUM -> R.string.mod_priority_medium
        PriorityLevel.LOW -> R.string.mod_priority_low
        null -> R.string.mod_priority_none
    }
)
