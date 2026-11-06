package co.edu.uniquindio.petadoptionapp.core.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import co.edu.uniquindio.petadoptionapp.R

/**
Utilidad de Compose para mostrar la antigüedad de algo (por ejemplo, una publicación en el feed o en su detalle)
como texto relativo ("hace 3 horas", "hace 2 días"). Usa recursos plurales para que el singular y el plural sean
correctos en cada idioma.
**/

/**
Antigüedad de una publicación: menos de 24 horas se muestra en horas ("hace 3 horas"); desde ahí, en días.
**/
@Composable
fun agoText(ageHours: Int): String =
    // Menos de un día: se expresa en horas.
    if (ageHours < 24) {
        // Texto plural de horas (elige singular o plural según la cantidad).
        pluralStringResource(R.plurals.common_time_hours_ago, ageHours, ageHours)
    } else {
        // Convierte las horas a días completos.
        val days = ageHours / 24
        // Texto plural de días.
        pluralStringResource(R.plurals.common_time_days_ago, days, days)
    }
