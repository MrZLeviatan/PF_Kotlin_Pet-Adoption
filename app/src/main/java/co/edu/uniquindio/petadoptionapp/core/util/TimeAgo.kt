package co.edu.uniquindio.petadoptionapp.core.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import co.edu.uniquindio.petadoptionapp.R

/** Antigüedad de una publicación: menos de 24 horas se muestra en horas ("hace 3 horas"); desde ahí, en días. */
@Composable
fun agoText(ageHours: Int): String =
    if (ageHours < 24) {
        pluralStringResource(R.plurals.common_time_hours_ago, ageHours, ageHours)
    } else {
        val days = ageHours / 24
        pluralStringResource(R.plurals.common_time_days_ago, days, days)
    }
