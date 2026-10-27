package co.edu.uniquindio.petadoptionapp.features.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.petadoptionapp.domain.model.notification.Notification
import co.edu.uniquindio.petadoptionapp.domain.model.notification.NotificationType
import co.edu.uniquindio.petadoptionapp.domain.usecase.notification.GetNotificationsUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.notification.MarkAllNotificationsReadUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.notification.MarkNotificationReadUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.reputation.LoadBadgesUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.reputation.ObserveBadgesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class NotificationFilter { ALL, UNREAD, MESSAGES }

// Notificación lista para dibujar. Solo guarda datos neutros: los textos los pone la UI con strings.xml.
data class NotificationUi(
    val id: String,
    val type: NotificationType,
    val actorName: String?,
    val extra: String?,          // Motivo, nombre de insignia (ya resuelto) o nivel.
    val postId: String?,         // Destino de la navegación; null si no tiene.
    val ageMinutes: Int,
    val isUnread: Boolean,
)

data class NotificationsUiState(
    val filter: NotificationFilter = NotificationFilter.ALL,
    val notifications: List<NotificationUi> = emptyList(),
    val isLoading: Boolean = false,
    val hasError: Boolean = false,
    val canLoadMore: Boolean = false,
) {
    /** Cantidad de notificaciones sin leer. */
    val unreadCount: Int get() = notifications.count { it.isUnread }

    /** Notificaciones de la chip seleccionada. */
    val visibleNotifications: List<NotificationUi>
        get() = when (filter) {
            NotificationFilter.ALL -> notifications
            NotificationFilter.UNREAD -> notifications.filter { it.isUnread }
            NotificationFilter.MESSAGES -> notifications.filter { it.type in CONVERSATION_TYPES }
        }

    private companion object {
        // Eventos de conversación: comentarios, respuestas y adopciones.
        val CONVERSATION_TYPES = setOf(
            NotificationType.NEW_COMMENT, NotificationType.COMMENT_REPLY,
            NotificationType.ADOPTION_REQUEST, NotificationType.ADOPTION_RESPONSE
        )
    }
}

// Eventos de un solo uso.
sealed interface NotificationsEvent {
    data class OpenPost(val postId: String) : NotificationsEvent
}

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val getNotifications: GetNotificationsUseCase,
    private val markRead: MarkNotificationReadUseCase,
    private val markAllRead: MarkAllNotificationsReadUseCase,
    private val loadBadges: LoadBadgesUseCase,
    private val observeBadges: ObserveBadgesUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    private val _events = Channel<NotificationsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // Id de la última notificación cargada: cursor de la paginación.
    private var lastId: String? = null

    init { load(reset = true) }

    // Carga la primera página (reset) o la siguiente.
    private fun load(reset: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, hasError = false) }
            loadBadges()   // Para mostrar el nombre de las insignias.
            getNotifications(if (reset) null else lastId)
                .onSuccess { page ->
                    lastId = page.lastOrNull()?.id ?: lastId
                    val items = page.map { it.toUi() }
                    _uiState.update {
                        it.copy(
                            notifications = if (reset) items else it.notifications + items,
                            isLoading = false,
                            canLoadMore = page.size >= GetNotificationsUseCase.PAGE_SIZE
                        )
                    }
                }
                .onFailure { _uiState.update { s -> s.copy(isLoading = false, hasError = true) } }
        }
    }

    fun onFilterSelected(filter: NotificationFilter) = _uiState.update { it.copy(filter = filter) }

    fun onLoadMore() { if (!_uiState.value.isLoading) load(reset = false) }

    fun onNotificationClick(notificationId: String) {
        val item = _uiState.value.notifications.firstOrNull { it.id == notificationId } ?: return

        // Marca como leída al instante en pantalla y guarda en segundo plano.
        if (item.isUnread) {
            setUnread(notificationId, false)
            viewModelScope.launch {
                markRead(notificationId, true).onFailure { setUnread(notificationId, true) }
            }
        }
        // Navega solo si tiene publicación asociada.
        item.postId?.let { _events.trySend(NotificationsEvent.OpenPost(it)) }
    }

    fun onMarkAllRead() {
        viewModelScope.launch {
            markAllRead().onSuccess {
                _uiState.update { s -> s.copy(notifications = s.notifications.map { it.copy(isUnread = false) }) }
            }
        }
    }

    private fun setUnread(id: String, unread: Boolean) = _uiState.update { s ->
        s.copy(notifications = s.notifications.map { if (it.id == id) it.copy(isUnread = unread) else it })
    }

    // Convierte la notificación del dominio en la que dibuja la pantalla.
    private fun Notification.toUi() = NotificationUi(
        id = id,
        type = type,
        actorName = actorName,
        extra = when (type) {
            // El id de la insignia se cambia por su nombre del catálogo.
            NotificationType.BADGE_UNLOCKED ->
                observeBadges().value.firstOrNull { it.id == extra }?.name ?: extra
            else -> extra
        },
        postId = postId,
        ageMinutes = ((System.currentTimeMillis() - createdAt) / 60_000L).toInt().coerceAtLeast(0),
        isUnread = !read
    )


}