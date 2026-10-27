package co.edu.uniquindio.petadoptionapp.features.notification

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
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
import co.edu.uniquindio.petadoptionapp.core.component.header.AppTopBar
import co.edu.uniquindio.petadoptionapp.core.component.navigation.AppChipRow
import co.edu.uniquindio.petadoptionapp.core.component.text.StatusMessage
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme
import co.edu.uniquindio.petadoptionapp.domain.model.notification.NotificationType

private val ScreenHorizontalPadding = 24.dp

/**
Pantalla de notificaciones con filtros (todas, sin leer, mensajes).
Conecta el [NotificationsViewModel] con la UI: observa el estado, reenvía las acciones
y reacciona a los eventos de un solo uso. [NotificationsContent] solo dibuja.
 */
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onOpenPost: (String) -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnOpenPost by rememberUpdatedState(onOpenPost)

    // Escucha los eventos de un solo uso (abrir una publicación).
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is NotificationsEvent.OpenPost -> currentOnOpenPost(event.postId)
            }
        }
    }

    NotificationsContent(
        uiState = uiState,
        onBack = onBack,
        onFilterSelected = viewModel::onFilterSelected,
        onNotificationClick = viewModel::onNotificationClick,
        onMarkAllRead = viewModel::onMarkAllRead,
        onLoadMore = viewModel::onLoadMore,
    )
}

// Sin lógica ni ViewModel: recibe el estado y dibuja. Es lo que se previsualiza.
@Composable
private fun NotificationsContent(
    uiState: NotificationsUiState,
    onBack: () -> Unit,
    onFilterSelected: (NotificationFilter) -> Unit,
    onNotificationClick: (String) -> Unit,
    onMarkAllRead: () -> Unit,
    onLoadMore: () -> Unit,
) {
    val filters = NotificationFilter.entries
    val chipLabels = filters.map { stringResource(it.labelRes()) }
    val notifications = uiState.visibleNotifications

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .systemBarsPadding(),
    ) {
        AppTopBar(
            title = stringResource(R.string.notifications_title),
            onBack = onBack,
            modifier = Modifier.padding(horizontal = 8.dp),
        )

        AppChipRow(
            labels = chipLabels,
            selectedIndex = filters.indexOf(uiState.filter),
            onSelected = { index -> onFilterSelected(filters[index]) },
            modifier = Modifier.padding(horizontal = ScreenHorizontalPadding - 4.dp),
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = ScreenHorizontalPadding, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // "Marcar todas como leídas" solo si hay pendientes.
            if (uiState.unreadCount > 0) {
                item(key = "mark_all") {
                    TextButton(onClick = onMarkAllRead) {
                        Text(stringResource(R.string.notifications_mark_all))
                    }
                }
            }

            // Error de carga, o mensaje de lista vacía.
            if (uiState.hasError) {
                item(key = "error") {
                    StatusMessage(
                        text = stringResource(R.string.notifications_error),
                        isError = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                    )
                }
            } else if (notifications.isEmpty() && !uiState.isLoading) {
                item(key = "empty") {
                    StatusMessage(
                        text = stringResource(R.string.notifications_empty),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                    )
                }
            }

            items(notifications, key = { it.id }) { notification ->
                NotificationCard(
                    icon = notification.type.icon(),
                    title = notificationTitle(notification.type),
                    timeText = timeAgoText(notification.ageMinutes),
                    message = notificationMessage(notification),
                    isNew = notification.isUnread,
                    onClick = { onNotificationClick(notification.id) },
                )
            }

            // Paginación: solo si la página llegó completa.
            if (uiState.canLoadMore) {
                item(key = "more") {
                    TextButton(onClick = onLoadMore) {
                        Text(stringResource(R.string.notifications_load_more))
                    }
                }
            }
        }
    }
}

// Minutos → "Hace 2 min", horas → "Hace 3 h", 24 a 47 h → "Ayer", después "Hace X días".
@Composable
private fun timeAgoText(ageMinutes: Int): String = when {
    ageMinutes < 60 -> stringResource(R.string.notifications_time_minutes, ageMinutes)
    ageMinutes < 24 * 60 -> stringResource(R.string.notifications_time_hours, ageMinutes / 60)
    ageMinutes < 48 * 60 -> stringResource(R.string.notifications_time_yesterday)
    else -> {
        val days = ageMinutes / (24 * 60)
        pluralStringResource(R.plurals.notifications_time_days, days, days)
    }
}

// El icono es presentación: vive en la pantalla y no en el estado.
private fun NotificationType.icon(): ImageVector = when (this) {
    NotificationType.NEW_COMMENT, NotificationType.COMMENT_REPLY -> Icons.Outlined.ChatBubbleOutline
    NotificationType.POST_IMPORTANT -> Icons.Outlined.FavoriteBorder
    NotificationType.ADOPTION_REQUEST, NotificationType.ADOPTION_RESPONSE -> Icons.Outlined.Pets
    NotificationType.POST_VERIFIED, NotificationType.POST_REJECTED,
    NotificationType.POST_FINISHED -> Icons.Outlined.VerifiedUser
    NotificationType.BADGE_UNLOCKED, NotificationType.LEVEL_UP -> Icons.Outlined.EmojiEvents
}

// Título según el tipo de notificación.
@Composable
private fun notificationTitle(type: NotificationType): String = stringResource(
    when (type) {
        NotificationType.NEW_COMMENT -> R.string.notif_title_new_comment
        NotificationType.COMMENT_REPLY -> R.string.notif_title_comment_reply
        NotificationType.POST_VERIFIED -> R.string.notif_title_post_verified
        NotificationType.POST_REJECTED -> R.string.notif_title_post_rejected
        NotificationType.POST_IMPORTANT -> R.string.notif_title_post_important
        NotificationType.ADOPTION_REQUEST -> R.string.notif_title_adoption_request
        NotificationType.ADOPTION_RESPONSE -> R.string.notif_title_adoption_response
        NotificationType.POST_FINISHED -> R.string.notif_title_post_finished
        NotificationType.BADGE_UNLOCKED -> R.string.notif_title_badge
        NotificationType.LEVEL_UP -> R.string.notif_title_level_up
    }
)

// Mensaje según el tipo, con los datos que guardó la notificación (alias, motivo, insignia...).
@Composable
private fun notificationMessage(n: NotificationUi): String {
    val actor = n.actorName ?: stringResource(R.string.notif_someone)
    return when (n.type) {
        NotificationType.NEW_COMMENT -> stringResource(R.string.notif_msg_new_comment, actor)
        NotificationType.COMMENT_REPLY -> stringResource(R.string.notif_msg_comment_reply, actor)
        NotificationType.POST_VERIFIED -> stringResource(R.string.notif_msg_post_verified)
        NotificationType.POST_REJECTED -> stringResource(R.string.notif_msg_post_rejected, n.extra.orEmpty())
        NotificationType.POST_IMPORTANT -> stringResource(R.string.notif_msg_post_important, actor)
        NotificationType.ADOPTION_REQUEST -> stringResource(R.string.notif_msg_adoption_request, actor)
        NotificationType.ADOPTION_RESPONSE ->
            if (n.extra == "ACCEPTED") stringResource(R.string.notif_msg_adoption_accepted, actor)
            else stringResource(R.string.notif_msg_adoption_declined, actor)
        NotificationType.POST_FINISHED -> stringResource(R.string.notif_msg_post_finished)
        NotificationType.BADGE_UNLOCKED -> stringResource(R.string.notif_msg_badge, n.extra.orEmpty())
        NotificationType.LEVEL_UP -> stringResource(R.string.notif_msg_level_up, levelText(n.extra))
    }
}

// Traduce el nombre del nivel guardado en `extra` ("PROTECTOR") al texto del recurso.
@Composable
private fun levelText(name: String?): String = stringResource(
    when (name) {
        "PROTECTOR" -> R.string.level_protector
        "GUARDIAN" -> R.string.level_guardian
        "PET_HERO" -> R.string.level_pet_hero
        else -> R.string.level_animal_friend
    }
)

@StringRes
private fun NotificationFilter.labelRes() = when (this) {
    NotificationFilter.ALL -> R.string.notifications_filter_all
    NotificationFilter.UNREAD -> R.string.notifications_filter_unread
    NotificationFilter.MESSAGES -> R.string.notifications_filter_messages
}

// Datos de ejemplo solo para las previews.
private val previewNotifications = listOf(
    NotificationUi("1", NotificationType.NEW_COMMENT, "luis_9", null, "p1", 2, true),
    NotificationUi("2", NotificationType.POST_VERIFIED, "mod_01", null, "p1", 18, true),
    NotificationUi("3", NotificationType.BADGE_UNLOCKED, null, "Primera publicación", null, 180, false),
    NotificationUi("4", NotificationType.LEVEL_UP, null, "PROTECTOR", null, 24 * 60, false),
)

@Preview(name = "Todas", showBackground = true, heightDp = 1100)
@Composable
private fun NotificationsAllPreview() {
    PetAdoptionAppTheme {
        NotificationsContent(
            uiState = NotificationsUiState(notifications = previewNotifications),
            onBack = {}, onFilterSelected = {}, onNotificationClick = {},
            onMarkAllRead = {}, onLoadMore = {},
        )
    }
}

@Preview(name = "Sin leer", showBackground = true, heightDp = 800)
@Composable
private fun NotificationsUnreadPreview() {
    PetAdoptionAppTheme {
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

@Preview(name = "Vacía", showBackground = true, heightDp = 800)
@Composable
private fun NotificationsEmptyPreview() {
    PetAdoptionAppTheme {
        NotificationsContent(
            uiState = NotificationsUiState(),
            onBack = {}, onFilterSelected = {}, onNotificationClick = {},
            onMarkAllRead = {}, onLoadMore = {},
        )
    }
}