package co.edu.uniquindio.petadoptionapp.features.notification

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.card.NotificationCard
import co.edu.uniquindio.petadoptionapp.core.component.card.SwipeToDeleteBox
import co.edu.uniquindio.petadoptionapp.core.component.header.AppTopBar
import co.edu.uniquindio.petadoptionapp.core.component.navigation.AppChipRow
import co.edu.uniquindio.petadoptionapp.core.component.text.StatusMessage
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme
import co.edu.uniquindio.petadoptionapp.domain.model.notification.NotificationType

/**
Pantalla de notificaciones del usuario.
Muestra una barra superior con botón de volver, chips de filtro (todas, sin leer, mensajes) y una
lista paginada de notificaciones que se pueden tocar para abrirlas o deslizar para borrarlas.
Estado: NotificationsUiState expuesto por NotificationsViewModel (lista, filtro, carga, errores,
paginación). Acciones: cambiar filtro, tocar, borrar, marcar todas como leídas y cargar más.
Casos de uso (a través del ViewModel): GetNotificationsUseCase, MarkNotificationReadUseCase,
MarkAllNotificationsReadUseCase, DeleteNotificationUseCase y los de insignias.
Navegación: los eventos OpenPost, OpenChat y OpenMetrics se convierten en los callbacks
onOpenPost, onOpenChat y onOpenMetrics, que AppNavigation usa para ir a la pantalla destino.
**/

// Margen horizontal estándar de la pantalla.
private val ScreenHorizontalPadding = 24.dp

/**
Pantalla de notificaciones con filtros (todas, sin leer, mensajes).
Conecta el [NotificationsViewModel] con la UI: observa el estado, reenvía las acciones
y reacciona a los eventos de un solo uso. [NotificationsContent] solo dibuja.
**/
// Marca la función como composable.
@Composable
fun NotificationsScreen(
    // Evento para volver a la pantalla anterior.
    onBack: () -> Unit,
    // Navega al detalle de una publicación (recibe su id).
    onOpenPost: (String) -> Unit,
    // Navega a una conversación (recibe el id del chat).
    onOpenChat: (chatId: String) -> Unit,
    // Navega al dashboard de métricas (insignias y niveles).
    onOpenMetrics: () -> Unit,
    // ViewModel inyectado por Hilt.
    viewModel: NotificationsViewModel = hiltViewModel(),
) {
    // Observa el estado de la pantalla respetando el ciclo de vida.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Guarda siempre la versión más reciente de cada callback sin reiniciar el LaunchedEffect.
    val currentOnOpenPost by rememberUpdatedState(onOpenPost)
    // Versión actualizada del callback para abrir chats.
    val currentOnOpenChat by rememberUpdatedState(onOpenChat)
    // Versión actualizada del callback para abrir métricas.
    val currentOnOpenMetrics by rememberUpdatedState(onOpenMetrics)

    // Escucha los eventos de un solo uso (abrir publicación, chat o métricas).
    LaunchedEffect(viewModel) {
        // Recolecta cada evento emitido por el ViewModel.
        viewModel.events.collect { event ->
            // Traduce el evento al callback de navegación correspondiente.
            when (event) {
                // Abre el detalle de la publicación.
                is NotificationsEvent.OpenPost -> currentOnOpenPost(event.postId)
                // Abre la conversación.
                is NotificationsEvent.OpenChat -> currentOnOpenChat(event.chatId)
                // Abre el dashboard de métricas.
                NotificationsEvent.OpenMetrics -> currentOnOpenMetrics()
            }
        }
    }

    // Dibuja el contenido conectando cada acción con su función del ViewModel.
    NotificationsContent(
        // Estado actual.
        uiState = uiState,
        // Volver atrás.
        onBack = onBack,
        // Cambiar el filtro.
        onFilterSelected = viewModel::onFilterSelected,
        // Tocar una notificación.
        onNotificationClick = viewModel::onNotificationClick,
        // Marcar todas como leídas.
        onMarkAllRead = viewModel::onMarkAllRead,
        // Cargar la siguiente página.
        onLoadMore = viewModel::onLoadMore,
        // Borrar al deslizar.
        onDelete = viewModel::onDeleteNotification,
        // Avisar que el error de borrado ya se mostró.
        onDeleteErrorShown = viewModel::onDeleteErrorShown,
    )
}

// Sin lógica ni ViewModel: recibe el estado y dibuja. Es lo que se previsualiza.
@Composable
private fun NotificationsContent(
    // Estado a dibujar.
    uiState: NotificationsUiState,
    // Volver atrás.
    onBack: () -> Unit,
    // Cambio de filtro.
    onFilterSelected: (NotificationFilter) -> Unit,
    // Toque sobre una notificación (recibe su id).
    onNotificationClick: (String) -> Unit,
    // Marcar todas como leídas.
    onMarkAllRead: () -> Unit,
    // Cargar más.
    onLoadMore: () -> Unit,
    // Borrar una notificación (recibe su id).
    onDelete: (String) -> Unit = {},
    // Confirmar que se mostró el error de borrado.
    onDeleteErrorShown: () -> Unit = {},
) {
    // Lista de filtros disponibles, en el orden del enum.
    val filters = NotificationFilter.entries
    // Estado del snackbar, recordado entre recomposiciones.
    val snackbarHostState = remember { SnackbarHostState() }
    // Texto del error de borrado, resuelto aquí porque showSnackbar no es composable.
    val deleteErrorText = stringResource(R.string.notifications_delete_error)
    // Se ejecuta cada vez que cambia la bandera de error de borrado.
    LaunchedEffect(uiState.deleteError) {
        // Solo actúa cuando hay un error pendiente.
        if (uiState.deleteError) {
            // Muestra el mensaje y espera a que el snackbar desaparezca.
            snackbarHostState.showSnackbar(deleteErrorText)
            // Avisa al ViewModel para limpiar la bandera.
            onDeleteErrorShown()
        }
    }
    // Textos de cada chip de filtro.
    val chipLabels = filters.map { stringResource(it.labelRes()) }
    // Notificaciones que corresponden al filtro actual.
    val notifications = uiState.visibleNotifications

    // Caja raíz que permite superponer el snackbar sobre el contenido.
    Box(Modifier.fillMaxSize()) {
        // Columna principal: barra, chips y lista.
        Column(
            modifier = Modifier
                // Ocupa toda la pantalla.
                .fillMaxSize()
                // Fondo según el tema.
                .background(AppTheme.colors.background)
                // Evita las barras del sistema.
                .systemBarsPadding(),
        ) {
            // Barra superior con título y botón de volver.
            AppTopBar(
                // Título de la pantalla.
                title = stringResource(R.string.notifications_title),
                // Acción del botón volver.
                onBack = onBack,
                // Pequeño margen horizontal.
                modifier = Modifier.padding(horizontal = 8.dp),
            )

            // Fila de chips de filtro.
            AppChipRow(
                // Textos de las chips.
                labels = chipLabels,
                // Chip seleccionada según el filtro actual.
                selectedIndex = filters.indexOf(uiState.filter),
                // Convierte el índice tocado en filtro y lo notifica.
                onSelected = { index -> onFilterSelected(filters[index]) },
                // Margen horizontal ligeramente menor para alinear visualmente las chips.
                modifier = Modifier.padding(horizontal = ScreenHorizontalPadding - 4.dp),
            )

            // Lista perezosa con las notificaciones.
            LazyColumn(
                modifier = Modifier
                    // Ocupa todo el ancho.
                    .fillMaxWidth()
                    // Toma el espacio vertical restante.
                    .weight(1f),
                // Márgenes internos de la lista.
                contentPadding = PaddingValues(horizontal = ScreenHorizontalPadding, vertical = 12.dp),
                // Separación de 12 dp entre elementos.
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // "Marcar todas como leídas" solo si hay pendientes.
                if (uiState.unreadCount > 0) {
                    // Elemento con clave fija para el botón.
                    item(key = "mark_all") {
                        // Botón de texto que marca todas como leídas.
                        TextButton(onClick = onMarkAllRead) {
                            // Texto del botón.
                            Text(stringResource(R.string.notifications_mark_all))
                        }
                    }
                }

                // Error de carga, o mensaje de lista vacía.
                if (uiState.hasError) {
                    // Elemento con el mensaje de error.
                    item(key = "error") {
                        // Mensaje de estado en modo error.
                        StatusMessage(
                            text = stringResource(R.string.notifications_error),
                            isError = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                        )
                    }
                // Si no hay error ni carga en curso y la lista está vacía, muestra el mensaje de vacío.
                } else if (notifications.isEmpty() && !uiState.isLoading) {
                    // Elemento con el mensaje de lista vacía.
                    item(key = "empty") {
                        // Mensaje de estado informativo.
                        StatusMessage(
                            text = stringResource(R.string.notifications_empty),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                        )
                    }
                }

                // Deslizar hacia la izquierda la borra (papelera en rojo, como en Instagram).
                items(notifications, key = { it.id }) { notification ->
                    // Contenedor que habilita el gesto de deslizar para borrar.
                    SwipeToDeleteBox(
                        // Al completar el gesto, borra la notificación.
                        onDelete = { onDelete(notification.id) },
                        // Descripción accesible de la acción de borrar.
                        deleteDescription = stringResource(R.string.notifications_delete),
                        // Anima la entrada, salida y reordenamiento del elemento.
                        modifier = Modifier.animateItem(),
                    ) {
                        // Tarjeta con la información de la notificación.
                        NotificationCard(
                            // Ícono según el tipo.
                            icon = notification.type.icon(),
                            // Título según el tipo.
                            title = notificationTitle(notification.type),
                            // Texto de tiempo relativo ("Hace 2 min").
                            timeText = timeAgoText(notification.ageMinutes),
                            // Mensaje con los datos de la notificación.
                            message = notificationMessage(notification),
                            // Resalta las no leídas.
                            isNew = notification.isUnread,
                            // Al tocarla, notifica con su id.
                            onClick = { onNotificationClick(notification.id) },
                        )
                    }
                }

                // Paginación: solo si la página llegó completa.
                if (uiState.canLoadMore) {
                    // Elemento con el botón de cargar más.
                    item(key = "more") {
                        // Botón que pide la siguiente página.
                        TextButton(onClick = onLoadMore) {
                            // Texto del botón.
                            Text(stringResource(R.string.notifications_load_more))
                        }
                    }
                }
            }
        }
        // Contenedor del snackbar de errores.
        SnackbarHost(
            // Estado que controla qué snackbar se muestra.
            hostState = snackbarHostState,
            modifier = Modifier
                // Lo ubica abajo en el centro.
                .align(Alignment.BottomCenter)
                // Evita que lo tape la barra de navegación del sistema.
                .systemBarsPadding(),
        )
    }
}

// Minutos → "Hace 2 min", horas → "Hace 3 h", 24 a 47 h → "Ayer", después "Hace X días".
@Composable
private fun timeAgoText(ageMinutes: Int): String = when {
    // Menos de una hora: muestra minutos.
    ageMinutes < 60 -> stringResource(R.string.notifications_time_minutes, ageMinutes)
    // Menos de un día: muestra horas.
    ageMinutes < 24 * 60 -> stringResource(R.string.notifications_time_hours, ageMinutes / 60)
    // Entre 24 y 47 horas: "Ayer".
    ageMinutes < 48 * 60 -> stringResource(R.string.notifications_time_yesterday)
    // Más tiempo: cantidad de días con plural.
    else -> {
        // Convierte minutos en días completos.
        val days = ageMinutes / (24 * 60)
        // Usa el recurso plural según la cantidad de días.
        pluralStringResource(R.plurals.notifications_time_days, days, days)
    }
}

// El icono es presentación: vive en la pantalla y no en el estado.
private fun NotificationType.icon(): ImageVector = when (this) {
    // Comentarios, respuestas y mensajes: burbuja de chat.
    NotificationType.NEW_COMMENT, NotificationType.COMMENT_REPLY,
    NotificationType.NEW_MESSAGE -> Icons.Outlined.ChatBubbleOutline
    // Publicación marcada como importante: corazón.
    NotificationType.POST_IMPORTANT -> Icons.Outlined.FavoriteBorder
    // Solicitudes y respuestas de adopción: huella.
    NotificationType.ADOPTION_REQUEST, NotificationType.ADOPTION_RESPONSE -> Icons.Outlined.Pets
    // Cambios de estado de la publicación por moderación: escudo verificado.
    NotificationType.POST_VERIFIED, NotificationType.POST_REJECTED,
    NotificationType.POST_FINISHED -> Icons.Outlined.VerifiedUser
    // Insignias y niveles: trofeo.
    NotificationType.BADGE_UNLOCKED, NotificationType.LEVEL_UP -> Icons.Outlined.EmojiEvents
}

// Título según el tipo de notificación.
@Composable
private fun notificationTitle(type: NotificationType): String = stringResource(
    // Elige el recurso de texto correspondiente a cada tipo.
    when (type) {
        // Nuevo comentario.
        NotificationType.NEW_COMMENT -> R.string.notif_title_new_comment
        // Respuesta a un comentario.
        NotificationType.COMMENT_REPLY -> R.string.notif_title_comment_reply
        // Publicación verificada.
        NotificationType.POST_VERIFIED -> R.string.notif_title_post_verified
        // Publicación rechazada.
        NotificationType.POST_REJECTED -> R.string.notif_title_post_rejected
        // Publicación marcada como importante.
        NotificationType.POST_IMPORTANT -> R.string.notif_title_post_important
        // Solicitud de adopción recibida.
        NotificationType.ADOPTION_REQUEST -> R.string.notif_title_adoption_request
        // Respuesta a una solicitud de adopción.
        NotificationType.ADOPTION_RESPONSE -> R.string.notif_title_adoption_response
        // Publicación finalizada.
        NotificationType.POST_FINISHED -> R.string.notif_title_post_finished
        // Nuevo mensaje de chat.
        NotificationType.NEW_MESSAGE -> R.string.notif_title_new_message
        // Insignia desbloqueada.
        NotificationType.BADGE_UNLOCKED -> R.string.notif_title_badge
        // Subida de nivel.
        NotificationType.LEVEL_UP -> R.string.notif_title_level_up
    }
)

// Mensaje según el tipo, con los datos que guardó la notificación (alias, motivo, insignia...).
@Composable
private fun notificationMessage(n: NotificationUi): String {
    // Nombre de quien generó la notificación, o "Alguien" si no se conoce.
    val actor = n.actorName ?: stringResource(R.string.notif_someone)
    // Devuelve el texto según el tipo, insertando los datos necesarios.
    return when (n.type) {
        // "X comentó tu publicación".
        NotificationType.NEW_COMMENT -> stringResource(R.string.notif_msg_new_comment, actor)
        // "X respondió tu comentario".
        NotificationType.COMMENT_REPLY -> stringResource(R.string.notif_msg_comment_reply, actor)
        // Publicación verificada (sin datos extra).
        NotificationType.POST_VERIFIED -> stringResource(R.string.notif_msg_post_verified)
        // Publicación rechazada, con el motivo guardado en extra.
        NotificationType.POST_REJECTED -> stringResource(R.string.notif_msg_post_rejected, n.extra.orEmpty())
        // Alguien marcó la publicación como importante.
        NotificationType.POST_IMPORTANT -> stringResource(R.string.notif_msg_post_important, actor)
        // Alguien quiere adoptar.
        NotificationType.ADOPTION_REQUEST -> stringResource(R.string.notif_msg_adoption_request, actor)
        // Respuesta de adopción: aceptada o rechazada según extra.
        NotificationType.ADOPTION_RESPONSE ->
            if (n.extra == "ACCEPTED") stringResource(R.string.notif_msg_adoption_accepted, actor)
            else stringResource(R.string.notif_msg_adoption_declined, actor)
        // Publicación finalizada.
        NotificationType.POST_FINISHED -> stringResource(R.string.notif_msg_post_finished)
        // Nuevo mensaje de chat.
        NotificationType.NEW_MESSAGE -> stringResource(R.string.notif_msg_new_message, actor)
        // Insignia desbloqueada, con su nombre ya resuelto.
        NotificationType.BADGE_UNLOCKED -> stringResource(R.string.notif_msg_badge, n.extra.orEmpty())
        // Subida de nivel, con el nombre del nivel traducido.
        NotificationType.LEVEL_UP -> stringResource(R.string.notif_msg_level_up, levelText(n.extra))
    }
}

// Traduce el nombre del nivel guardado en `extra` ("PROTECTOR") al texto del recurso.
@Composable
private fun levelText(name: String?): String = stringResource(
    // Elige el recurso según el nombre del nivel.
    when (name) {
        // Nivel Protector.
        "PROTECTOR" -> R.string.level_protector
        // Nivel Guardián.
        "GUARDIAN" -> R.string.level_guardian
        // Nivel Héroe de mascotas.
        "PET_HERO" -> R.string.level_pet_hero
        // Cualquier otro valor: nivel inicial "Amigo de los animales".
        else -> R.string.level_animal_friend
    }
)

// Devuelve el id del texto de cada chip de filtro.
@StringRes
private fun NotificationFilter.labelRes() = when (this) {
    // Chip "Todas".
    NotificationFilter.ALL -> R.string.notifications_filter_all
    // Chip "Sin leer".
    NotificationFilter.UNREAD -> R.string.notifications_filter_unread
    // Chip "Mensajes".
    NotificationFilter.MESSAGES -> R.string.notifications_filter_messages
}

// Datos de ejemplo solo para las previews.
private val previewNotifications = listOf(
    // Comentario nuevo sin leer.
    NotificationUi("1", NotificationType.NEW_COMMENT, "luis_9", null, "p1", 2, true),
    // Publicación verificada sin leer.
    NotificationUi("2", NotificationType.POST_VERIFIED, "mod_01", null, "p1", 18, true),
    // Insignia desbloqueada ya leída.
    NotificationUi("3", NotificationType.BADGE_UNLOCKED, null, "Primera publicación", null, 180, false),
    // Subida de nivel ya leída.
    NotificationUi("4", NotificationType.LEVEL_UP, null, "PROTECTOR", null, 24 * 60, false),
)

// Vista previa con el filtro "Todas".
@Preview(name = "Todas", showBackground = true, heightDp = 1100)
@Composable
private fun NotificationsAllPreview() {
    // Aplica el tema de la aplicación.
    PetAdoptionAppTheme {
        // Dibuja el contenido con los datos de ejemplo y callbacks vacíos.
        NotificationsContent(
            uiState = NotificationsUiState(notifications = previewNotifications),
            onBack = {}, onFilterSelected = {}, onNotificationClick = {},
            onMarkAllRead = {}, onLoadMore = {},
        )
    }
}

// Vista previa con el filtro "Sin leer".
@Preview(name = "Sin leer", showBackground = true, heightDp = 800)
@Composable
private fun NotificationsUnreadPreview() {
    // Aplica el tema de la aplicación.
    PetAdoptionAppTheme {
        // Dibuja el contenido filtrando solo las no leídas.
        NotificationsContent(
            uiState = NotificationsUiState(
                filter = NotificationFilter.UNREAD,
                notifications = previewNotifications,
            ),
            onBack = {}, onFilterSelected = {}, onNotificationClick = {},
            onMarkAllRead = {}, onLoadMore = {},
        )
    }
}

// Vista previa del estado vacío.
@Preview(name = "Vacía", showBackground = true, heightDp = 800)
@Composable
private fun NotificationsEmptyPreview() {
    // Aplica el tema de la aplicación.
    PetAdoptionAppTheme {
        // Dibuja el contenido sin notificaciones.
        NotificationsContent(
            uiState = NotificationsUiState(),
            onBack = {}, onFilterSelected = {}, onNotificationClick = {},
            onMarkAllRead = {}, onLoadMore = {},
        )
    }
}
