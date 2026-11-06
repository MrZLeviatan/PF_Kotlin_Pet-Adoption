package co.edu.uniquindio.petadoptionapp.features.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.petadoptionapp.domain.model.notification.Notification
import co.edu.uniquindio.petadoptionapp.domain.model.notification.NotificationType
import co.edu.uniquindio.petadoptionapp.domain.usecase.notification.DeleteNotificationUseCase
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

/**
ViewModel de la pantalla de notificaciones.
Estado de UI (NotificationsUiState): filtro seleccionado (todas, no leídas, mensajes), la lista de
notificaciones ya convertidas a NotificationUi, banderas de carga, error, paginación y error de borrado.
Eventos de un solo uso (NotificationsEvent): abrir una publicación, abrir un chat o abrir el dashboard
de métricas; se envían por un Channel y la pantalla los traduce en callbacks de navegación que
AppNavigation resuelve.
Casos de uso: GetNotificationsUseCase (paginación), MarkNotificationReadUseCase,
MarkAllNotificationsReadUseCase, DeleteNotificationUseCase, y LoadBadgesUseCase/ObserveBadgesUseCase
para mostrar el nombre de las insignias en lugar de su id.
Las acciones de leer y borrar son optimistas: se reflejan al instante y se revierten si fallan.
**/

// Filtros disponibles en las chips superiores de la pantalla.
enum class NotificationFilter { ALL, UNREAD, MESSAGES }

// Notificación lista para dibujar. Solo guarda datos neutros: los textos los pone la UI con strings.xml.
data class NotificationUi(
    // Id único de la notificación.
    val id: String,
    // Tipo de notificación (define el ícono, el texto y el destino).
    val type: NotificationType,
    // Nombre de quien generó la notificación (puede no existir).
    val actorName: String?,
    // Motivo, nombre de insignia (ya resuelto) o nivel.
    val extra: String?,
    // Destino de la navegación; null si no tiene.
    val postId: String?,
    // Minutos transcurridos desde que se creó (para mostrar "hace X").
    val ageMinutes: Int,
    // true si el usuario todavía no la ha leído.
    val isUnread: Boolean,
    // Chat asociado (mensajes y solicitudes de adopción): tiene prioridad sobre la publicación.
    val chatId: String? = null,
)

// Estado completo de la pantalla de notificaciones.
data class NotificationsUiState(
    // Filtro (chip) seleccionado actualmente.
    val filter: NotificationFilter = NotificationFilter.ALL,
    // Todas las notificaciones cargadas hasta el momento.
    val notifications: List<NotificationUi> = emptyList(),
    // true mientras se está cargando una página.
    val isLoading: Boolean = false,
    // true si la última carga falló.
    val hasError: Boolean = false,
    // true si puede haber más páginas por cargar.
    val canLoadMore: Boolean = false,
    // No se pudo borrar una notificación (se volvió a poner en su lugar).
    val deleteError: Boolean = false,
) {
    // Cantidad de notificaciones sin leer.
    val unreadCount: Int get() = notifications.count { it.isUnread }

    // Notificaciones de la chip seleccionada.
    val visibleNotifications: List<NotificationUi>
        // Se recalcula cada vez que se lee, según el filtro actual.
        get() = when (filter) {
            // Sin filtro: todas las notificaciones.
            NotificationFilter.ALL -> notifications
            // Solo las que siguen sin leer.
            NotificationFilter.UNREAD -> notifications.filter { it.isUnread }
            // Solo las de tipo conversación.
            NotificationFilter.MESSAGES -> notifications.filter { it.type in CONVERSATION_TYPES }
        }

    // Constantes privadas del estado.
    private companion object {
        // Eventos de conversación: mensajes, comentarios, respuestas y adopciones.
        val CONVERSATION_TYPES = setOf(
            NotificationType.NEW_MESSAGE, NotificationType.NEW_COMMENT, NotificationType.COMMENT_REPLY,
            NotificationType.ADOPTION_REQUEST, NotificationType.ADOPTION_RESPONSE
        )
    }
}

// Eventos de un solo uso.
sealed interface NotificationsEvent {
    // Abrir el detalle de una publicación.
    data class OpenPost(val postId: String) : NotificationsEvent
    // Abrir una conversación de chat.
    data class OpenChat(val chatId: String) : NotificationsEvent
    // Insignias y niveles: se ven en el dashboard de métricas.
    data object OpenMetrics : NotificationsEvent
}

// Insignias y niveles: no tienen publicación; se abren en el dashboard de métricas.
private val REPUTATION_TYPES = setOf(NotificationType.BADGE_UNLOCKED, NotificationType.LEVEL_UP)

// Marca la clase como ViewModel inyectable por Hilt.
@HiltViewModel
// Declara el ViewModel y pide a Hilt los casos de uso por constructor.
class NotificationsViewModel @Inject constructor(
    // Obtiene una página de notificaciones.
    private val getNotifications: GetNotificationsUseCase,
    // Marca una notificación como leída o no leída.
    private val markRead: MarkNotificationReadUseCase,
    // Marca todas las notificaciones como leídas.
    private val markAllRead: MarkAllNotificationsReadUseCase,
    // Carga el catálogo de insignias.
    private val loadBadges: LoadBadgesUseCase,
    // Expone el catálogo de insignias ya cargado.
    private val observeBadges: ObserveBadgesUseCase,
    // Borra una notificación.
    private val deleteNotification: DeleteNotificationUseCase,
) : ViewModel() {

    // Estado mutable interno de la pantalla.
    private val _uiState = MutableStateFlow(NotificationsUiState())
    // Estado de solo lectura que observa la UI.
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    // Canal con búfer para los eventos de navegación de un solo uso.
    private val _events = Channel<NotificationsEvent>(Channel.BUFFERED)
    // Flujo de eventos que la pantalla recolecta (cada evento se entrega una sola vez).
    val events = _events.receiveAsFlow()

    // Id de la última notificación cargada: cursor de la paginación.
    private var lastId: String? = null

    // Al crearse el ViewModel carga la primera página.
    init { load(reset = true) }

    // Carga la primera página (reset) o la siguiente.
    private fun load(reset: Boolean) {
        // Lanza la carga en una corrutina ligada al ciclo de vida del ViewModel.
        viewModelScope.launch {
            // Activa el indicador de carga y limpia el error anterior.
            _uiState.update { it.copy(isLoading = true, hasError = false) }
            // Carga las insignias para mostrar su nombre.
            loadBadges()
            // Pide la página: desde el inicio si es reset, o a partir del último id cargado.
            getNotifications(if (reset) null else lastId)
                // Si la carga fue exitosa, procesa la página recibida.
                .onSuccess { page ->
                    // Actualiza el cursor con el último id de la página (si llegó vacía, conserva el anterior).
                    lastId = page.lastOrNull()?.id ?: lastId
                    // Convierte cada notificación del dominio al modelo de UI.
                    val items = page.map { it.toUi() }
                    // Actualiza el estado con la nueva página.
                    _uiState.update {
                        it.copy(
                            // Reemplaza la lista si es reset o agrega los nuevos al final.
                            notifications = if (reset) items else it.notifications + items,
                            // Termina la carga.
                            isLoading = false,
                            // Si la página vino completa puede haber más.
                            canLoadMore = page.size >= GetNotificationsUseCase.PAGE_SIZE
                        )
                    }
                }
                // Si falla, quita la carga y marca el error.
                .onFailure { _uiState.update { s -> s.copy(isLoading = false, hasError = true) } }
        }
    }

    // Cambia el filtro (chip) seleccionado.
    fun onFilterSelected(filter: NotificationFilter) = _uiState.update { it.copy(filter = filter) }

    // Carga la siguiente página si no hay otra carga en curso.
    fun onLoadMore() { if (!_uiState.value.isLoading) load(reset = false) }

    // Maneja el toque sobre una notificación: la marca como leída y emite el evento de navegación.
    fun onNotificationClick(notificationId: String) {
        // Busca la notificación tocada; si no existe, termina.
        val item = _uiState.value.notifications.firstOrNull { it.id == notificationId } ?: return

        // Marca como leída al instante en pantalla y guarda en segundo plano.
        if (item.isUnread) {
            // Actualización optimista en la UI.
            setUnread(notificationId, false)
            // Persiste el cambio en segundo plano.
            viewModelScope.launch {
                // Si falla el guardado, la vuelve a marcar como no leída.
                markRead(notificationId, true).onFailure { setUnread(notificationId, true) }
            }
        }
        // Destino según el tema: el chat (mensajes, solicitudes de adopción), el dashboard (insignias y nivel)
        // o la publicación (comentarios, votos, verificación, rechazo, finalización).
        when {
            // Si tiene chat asociado, abre la conversación.
            item.chatId != null -> _events.trySend(NotificationsEvent.OpenChat(item.chatId))
            // Si es de reputación, abre el dashboard de métricas.
            item.type in REPUTATION_TYPES -> _events.trySend(NotificationsEvent.OpenMetrics)
            // Si tiene publicación, abre su detalle.
            item.postId != null -> _events.trySend(NotificationsEvent.OpenPost(item.postId))
        }
    }

    // Deslizar para borrar: sale de la lista al instante y, si el borrado falla, vuelve a su lugar.
    fun onDeleteNotification(notificationId: String) {
        // Lista actual de notificaciones.
        val list = _uiState.value.notifications
        // Posición de la notificación a borrar.
        val index = list.indexOfFirst { it.id == notificationId }
        // Si no está en la lista, no hace nada.
        if (index < 0) return
        // Guarda la notificación para poder restaurarla si falla.
        val removed = list[index]
        // Quita la notificación de la lista al instante y limpia el error de borrado.
        _uiState.update { s -> s.copy(notifications = s.notifications.filterNot { it.id == notificationId }, deleteError = false) }
        // Borra en segundo plano.
        viewModelScope.launch {
            // Ejecuta el caso de uso de borrado; si falla, revierte.
            deleteNotification(notificationId).onFailure {
                // Restaura la notificación en su posición original y marca el error.
                _uiState.update { s ->
                    // Inserta la notificación en su índice (sin pasarse del tamaño actual de la lista).
                    val restored = s.notifications.toMutableList().apply { add(index.coerceAtMost(size), removed) }
                    // Publica la lista restaurada junto con el error.
                    s.copy(notifications = restored, deleteError = true)
                }
            }
        }
    }

    // La UI avisa que ya mostró el error de borrado para limpiarlo.
    fun onDeleteErrorShown() = _uiState.update { it.copy(deleteError = false) }

    // Marca todas las notificaciones como leídas.
    fun onMarkAllRead() {
        // Ejecuta la operación en segundo plano.
        viewModelScope.launch {
            // Llama al caso de uso y, solo si tiene éxito, actualiza la UI.
            markAllRead().onSuccess {
                // Pone todas las notificaciones cargadas como leídas.
                _uiState.update { s -> s.copy(notifications = s.notifications.map { it.copy(isUnread = false) }) }
            }
        }
    }

    // Cambia el estado de leída/no leída de una notificación en el estado de UI.
    private fun setUnread(id: String, unread: Boolean) = _uiState.update { s ->
        // Copia la lista modificando solo la notificación con ese id.
        s.copy(notifications = s.notifications.map { if (it.id == id) it.copy(isUnread = unread) else it })
    }

    // Convierte la notificación del dominio en la que dibuja la pantalla.
    private fun Notification.toUi() = NotificationUi(
        // Copia el id.
        id = id,
        // Copia el tipo.
        type = type,
        // Copia el nombre del actor.
        actorName = actorName,
        // Resuelve el dato extra según el tipo.
        extra = when (type) {
            // El id de la insignia se cambia por su nombre del catálogo.
            NotificationType.BADGE_UNLOCKED ->
                observeBadges().value.firstOrNull { it.id == extra }?.name ?: extra
            // En los demás tipos se usa tal cual.
            else -> extra
        },
        // Copia la publicación asociada.
        postId = postId,
        // Calcula los minutos transcurridos desde la creación (nunca negativos).
        ageMinutes = ((System.currentTimeMillis() - createdAt) / 60_000L).toInt().coerceAtLeast(0),
        // Invierte la bandera "read" del dominio.
        isUnread = !read,
        // Usa el chat solo si el id no está vacío.
        chatId = chatId?.takeIf { it.isNotBlank() }
    )


}
