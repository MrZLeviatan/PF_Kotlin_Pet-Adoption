package co.edu.uniquindio.petadoptionapp.features.user.notifications

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Tipo de notificación. La pantalla decide el icono según el tipo. */
enum class NotificationTypeUi { NEW_ADOPTION, MESSAGE, COMMUNITY, SECURITY, REMINDER }

enum class NotificationFilter { ALL, UNREAD, MESSAGES }

data class NotificationUi(
    val id: String,
    val type: NotificationTypeUi,
    val title: String,
    val message: String,
    // Antigüedad en minutos: la pantalla la convierte a "Hace X min / h" o "Ayer".
    val ageMinutes: Int,
    val isUnread: Boolean,
)

/**
Estado de la pantalla. Los valores por defecto son datos QUEMADOS de prueba.
Los textos viven aquí y no en strings.xml porque vendrán del backend.
TODO: reemplazar por las notificaciones reales cuando exista el caso de uso
(ObserveNotificationsUseCase).
 */
data class NotificationsUiState(
    val filter: NotificationFilter = NotificationFilter.ALL,
    val notifications: List<NotificationUi> = listOf(
        NotificationUi(
            id = "1",
            type = NotificationTypeUi.NEW_ADOPTION,
            title = "Nueva adopción disponible",
            message = "Una nueva mascota fue publicada en tu zona. Revisa la ficha para ver fotos, edad y características.",
            ageMinutes = 2,
            isUnread = true,
        ),
        NotificationUi(
            id = "2",
            type = NotificationTypeUi.MESSAGE,
            title = "Mensaje de una adoptante",
            message = "Lucía te escribió sobre la publicación de Luna. Quiere saber más sobre el temperamento y el horario de visita.",
            ageMinutes = 18,
            isUnread = true,
        ),
        NotificationUi(
            id = "3",
            type = NotificationTypeUi.COMMUNITY,
            title = "Actividad comunitaria",
            message = "Se publicó un nuevo evento de adopción en tu zona. Podrás compartirlo con tus contactos y ayudar a difundir la causa.",
            ageMinutes = 60,
            isUnread = false,
        ),
        NotificationUi(
            id = "4",
            type = NotificationTypeUi.SECURITY,
            title = "Actualización de seguridad",
            message = "Tu sesión fue renovada correctamente. Si no reconoces esta actividad, revisa tus dispositivos conectados.",
            ageMinutes = 180,
            isUnread = false,
        ),
        NotificationUi(
            id = "5",
            type = NotificationTypeUi.REMINDER,
            title = "Recordatorio de visita",
            message = "Mañana tienes una visita programada con una familia interesada en adoptar. Te recomendamos confirmar el horario y la dirección.",
            ageMinutes = 24 * 60,
            isUnread = false,
        ),
    ),
) {
    /** Cantidad de notificaciones sin leer (para el contador de la chip o una insignia). */
    val unreadCount: Int get() = notifications.count { it.isUnread }

    /** Notificaciones de la chip seleccionada. */
    val visibleNotifications: List<NotificationUi>
        get() = when (filter) {
            NotificationFilter.ALL -> notifications
            NotificationFilter.UNREAD -> notifications.filter { it.isUnread }
            NotificationFilter.MESSAGES -> notifications.filter { it.type == NotificationTypeUi.MESSAGE }
        }
}

class NotificationsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    fun onFilterSelected(filter: NotificationFilter) = _uiState.update { it.copy(filter = filter) }

    fun onNotificationClick(notificationId: String) {
        // TODO: marcar como leída y navegar al destino según el tipo (publicación, chat...).
    }
}