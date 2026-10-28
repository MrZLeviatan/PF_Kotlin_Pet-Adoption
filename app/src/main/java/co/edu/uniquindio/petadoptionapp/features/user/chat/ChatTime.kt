package co.edu.uniquindio.petadoptionapp.features.user.chat

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import co.edu.uniquindio.petadoptionapp.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/*
 Fechas de los chats, como en WhatsApp: la hora si fue hoy, "Ayer" y luego "Hace N días".
 Se calculan con el calendario local (no con horas exactas): un mensaje de anoche ya es "Ayer".
 */

// Días de calendario entre el momento dado y hoy (0 = hoy, 1 = ayer).
private fun daysAgo(millis: Long, now: Long = System.currentTimeMillis()): Int {
    fun startOfDay(time: Long) = Calendar.getInstance().apply {
        timeInMillis = time
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    return ((startOfDay(now) - startOfDay(millis)) / (24 * 60 * 60 * 1000L)).toInt()
}

/** Hora del mensaje ("10:24"). */
fun chatClockText(millis: Long): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(millis))

/** Fecha de la lista de chats: "10:24", "Ayer" o "Hace 3 días". */
@Composable
fun chatListTimeText(millis: Long): String = when (val days = daysAgo(millis)) {
    0 -> chatClockText(millis)
    1 -> stringResource(R.string.chats_time_yesterday)
    else -> pluralStringResource(R.plurals.notifications_time_days, days, days)
}

/** Separador de día en la conversación: "Hoy", "Ayer" o la fecha ("12 oct. 2026"). */
@Composable
fun chatDayText(millis: Long): String = when (daysAgo(millis)) {
    0 -> stringResource(R.string.chat_today)
    1 -> stringResource(R.string.chats_time_yesterday)
    else -> SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(millis))
}

/** ¿Los dos momentos caen en el mismo día? Sirve para poner un separador al cambiar de día. */
fun isSameChatDay(a: Long, b: Long): Boolean = daysAgo(a, now = b) == 0
