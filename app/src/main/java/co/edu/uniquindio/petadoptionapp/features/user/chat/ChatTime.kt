package co.edu.uniquindio.petadoptionapp.features.user.chat

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import co.edu.uniquindio.petadoptionapp.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
Utilidades de formato de fechas para el módulo de chat (lista de chats y conversación).
Funcionan como en WhatsApp: muestran la hora si el mensaje fue hoy, "Ayer" si fue el día
anterior y "Hace N días" (en la lista) o la fecha completa (en los separadores de la conversación).
Los cálculos se hacen con el calendario local y no con horas exactas transcurridas: un mensaje
enviado anoche ya cuenta como "Ayer" aunque hayan pasado pocas horas.
No llaman casos de uso ni participan en la navegación; solo las usan ChatListScreen y ChatScreen.
**/

// Calcula cuántos días de calendario hay entre el momento dado y hoy (0 = hoy, 1 = ayer).
private fun daysAgo(millis: Long, now: Long = System.currentTimeMillis()): Int {
    // Función local que obtiene el instante de la medianoche (inicio del día) de un tiempo dado.
    fun startOfDay(time: Long) = Calendar.getInstance().apply {
        // Ubica el calendario en el instante recibido.
        timeInMillis = time
        // Pone en cero la hora, los minutos, los segundos y los milisegundos para quedar en el inicio del día.
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    // Resta los inicios de ambos días y divide entre los milisegundos de un día para obtener la diferencia en días.
    return ((startOfDay(now) - startOfDay(millis)) / (24 * 60 * 60 * 1000L)).toInt()
}

// Devuelve la hora del mensaje en formato de 24 horas ("10:24") según el idioma del dispositivo.
fun chatClockText(millis: Long): String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(millis))

// Devuelve el texto de fecha para la lista de chats: "10:24", "Ayer" o "Hace 3 días".
@Composable
fun chatListTimeText(millis: Long): String = when (val days = daysAgo(millis)) {
    // Si el mensaje es de hoy, muestra solo la hora.
    0 -> chatClockText(millis)
    // Si el mensaje es de ayer, muestra el texto "Ayer" desde strings.xml.
    1 -> stringResource(R.string.chats_time_yesterday)
    // En otro caso muestra "Hace N días" usando el plural adecuado según la cantidad.
    else -> pluralStringResource(R.plurals.notifications_time_days, days, days)
}

// Devuelve el texto del separador de día dentro de la conversación: "Hoy", "Ayer" o la fecha ("12 oct. 2026").
@Composable
fun chatDayText(millis: Long): String = when (daysAgo(millis)) {
    // Si el día es hoy, muestra "Hoy".
    0 -> stringResource(R.string.chat_today)
    // Si el día es ayer, muestra "Ayer".
    1 -> stringResource(R.string.chats_time_yesterday)
    // En otro caso muestra la fecha corta con día, mes abreviado y año.
    else -> SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(millis))
}

// Indica si los dos momentos caen en el mismo día de calendario; sirve para insertar un separador cuando cambia el día.
fun isSameChatDay(a: Long, b: Long): Boolean = daysAgo(a, now = b) == 0
