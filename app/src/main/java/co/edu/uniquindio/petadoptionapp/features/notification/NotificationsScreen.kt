package co.edu.uniquindio.petadoptionapp.features.user.notifications

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
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.card.NotificationCard
import co.edu.uniquindio.petadoptionapp.core.component.header.AppTopBar
import co.edu.uniquindio.petadoptionapp.core.component.navigation.AppChipRow
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme

private val ScreenHorizontalPadding = 24.dp

/**
Pantalla de notificaciones con filtros (todas, sin leer, mensajes).
Conecta el [NotificationsViewModel] con la UI; [NotificationsContent] solo dibuja.
 */
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    viewModel: NotificationsViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    NotificationsContent(
        uiState = uiState,
        onBack = onBack,
        onFilterSelected = viewModel::onFilterSelected,
        onNotificationClick = viewModel::onNotificationClick,
    )
}

// Sin lógica ni ViewModel: recibe el estado y dibuja. Es lo que se previsualiza.
@Composable
private fun NotificationsContent(
    uiState: NotificationsUiState,
    onBack: () -> Unit,
    onFilterSelected: (NotificationFilter) -> Unit,
    onNotificationClick: (String) -> Unit,
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
            if (notifications.isEmpty()) {
                item(key = "empty") { EmptyState() }
            }

            items(notifications, key = { it.id }) { notification ->
                NotificationCard(
                    icon = notification.type.icon(),
                    title = notification.title,
                    timeText = timeAgoText(notification.ageMinutes),
                    message = notification.message,
                    isNew = notification.isUnread,
                    onClick = { onNotificationClick(notification.id) },
                )
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Text(
        text = stringResource(R.string.notifications_empty),
        style = MaterialTheme.typography.bodyMedium,
        color = AppTheme.colors.textPrimary,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
    )
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
private fun NotificationTypeUi.icon(): ImageVector = when (this) {
    NotificationTypeUi.NEW_ADOPTION -> Icons.Outlined.FavoriteBorder
    NotificationTypeUi.MESSAGE -> Icons.Outlined.ChatBubbleOutline
    NotificationTypeUi.COMMUNITY -> Icons.Outlined.Notifications
    NotificationTypeUi.SECURITY -> Icons.Outlined.Lock
    NotificationTypeUi.REMINDER -> Icons.Outlined.FavoriteBorder
}

@StringRes
private fun NotificationFilter.labelRes() = when (this) {
    NotificationFilter.ALL -> R.string.notifications_filter_all
    NotificationFilter.UNREAD -> R.string.notifications_filter_unread
    NotificationFilter.MESSAGES -> R.string.notifications_filter_messages
}

@Preview(name = "Todas", showBackground = true, heightDp = 1100)
@Composable
private fun NotificationsAllPreview() {
    PetAdoptionAppTheme {
        NotificationsContent(
            uiState = NotificationsUiState(),
            onBack = {}, onFilterSelected = {}, onNotificationClick = {},
        )
    }
}

@Preview(name = "Sin leer", showBackground = true, heightDp = 800)
@Composable
private fun NotificationsUnreadPreview() {
    PetAdoptionAppTheme {
        NotificationsContent(
            uiState = NotificationsUiState(filter = NotificationFilter.UNREAD),
            onBack = {}, onFilterSelected = {}, onNotificationClick = {},
        )
    }
}

@Preview(name = "Vacía", showBackground = true, heightDp = 800)
@Composable
private fun NotificationsEmptyPreview() {
    PetAdoptionAppTheme {
        NotificationsContent(
            uiState = NotificationsUiState(notifications = emptyList()),
            onBack = {}, onFilterSelected = {}, onNotificationClick = {},
        )
    }
}