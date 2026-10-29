package co.edu.uniquindio.petadoptionapp.features.user.chat.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.card.AppCard
import co.edu.uniquindio.petadoptionapp.core.component.form.SearchField
import co.edu.uniquindio.petadoptionapp.core.component.icon.UserAvatar
import co.edu.uniquindio.petadoptionapp.core.component.text.IconTextRow
import co.edu.uniquindio.petadoptionapp.core.component.text.StatusMessage
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme
import co.edu.uniquindio.petadoptionapp.features.user.chat.DeleteChatDialog
import co.edu.uniquindio.petadoptionapp.features.user.chat.chatListTimeText

/**
 Pestaña Mensajes: buscador y la lista de chats, del más reciente al más antiguo.
 Cada chat muestra a la otra persona, el último mensaje, su fecha y cuántos hay sin leer.
 Mantener presionado un chat abre las opciones para eliminarlo (para mí o para ambos).
 No decide a dónde navegar: avisa con [onChatClick] y AppNavigation abre la conversación.
 */
@Composable
fun ChatListScreen(
    padding: PaddingValues,
    onChatClick: (chatId: String) -> Unit,
    viewModel: ChatListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Al volver de una conversación se recarga (último mensaje y no leídos).
    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }

    ChatListContent(
        uiState = uiState,
        padding = padding,
        onQueryChange = viewModel::onQueryChange,
        onChatClick = onChatClick,
        onChatLongClick = viewModel::onChatLongClick,
    )

    if (uiState.chatToDelete != null) {
        DeleteChatDialog(
            onDeleteForEveryone = { viewModel.onDeleteConfirm(forEveryone = true) },
            onDeleteForMe = { viewModel.onDeleteConfirm(forEveryone = false) },
            onDismiss = viewModel::onDeleteDismiss,
            isLoading = uiState.isDeleting,
        )
    }
}

// Sin lógica ni ViewModel: recibe el estado y dibuja. Es lo que se previsualiza.
@Composable
private fun ChatListContent(
    uiState: ChatListUiState,
    padding: PaddingValues,
    onQueryChange: (String) -> Unit,
    onChatClick: (String) -> Unit,
    onChatLongClick: (String) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.surfaceElevated)
            .padding(padding),
    ) {
        SearchField(
            query = uiState.query,
            onQueryChange = onQueryChange,
            placeholder = stringResource(R.string.chats_search_placeholder_full),
            iconDescription = stringResource(R.string.chats_search_description),
            borderColor = AppTheme.colors.border,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 12.dp),
        )

        val chats = uiState.visibleChats
        val message = when {
            uiState.isLoading -> null
            uiState.hasError -> stringResource(R.string.chats_error)
            uiState.deleteError -> stringResource(R.string.chat_delete_error)
            uiState.chats.isEmpty() -> stringResource(R.string.chats_empty)
            chats.isEmpty() -> stringResource(R.string.chats_search_empty)
            else -> null
        }
        if (message != null) {
            StatusMessage(
                text = message,
                isError = uiState.hasError || uiState.deleteError,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 48.dp),
            )
        }

        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(chats, key = { it.id }) { chat ->
                ChatRow(chat = chat, onClick = { onChatClick(chat.id) }, onLongClick = { onChatLongClick(chat.id) })
            }
        }
    }
}

// Tarjeta de un chat: avatar, nombre y fecha arriba; último mensaje y no leídos abajo.
@Composable
private fun ChatRow(chat: ChatSummaryUi, onClick: () -> Unit, onLongClick: () -> Unit) {
    val hasUnread = chat.unreadCount > 0
    AppCard(
        onClick = onClick,
        onLongClick = onLongClick,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val name = if (chat.isOtherDeleted) stringResource(R.string.chat_deleted_user) else chat.otherName
            UserAvatar(name = name, photoUrl = chat.otherPhotoUrl, size = 44.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = chatListTimeText(chat.lastMessageAt),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.stateSuccess,
                    )
                }
                // Publicación de la que trata el chat (dos chats con la misma persona se distinguen así).
                chat.postTitle?.let { title ->
                    IconTextRow(
                        icon = Icons.Outlined.Pets,
                        text = title,
                        iconSize = 12.dp,
                        spacing = 4.dp,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                    )
                }
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = previewText(chat),
                        style = MaterialTheme.typography.bodySmall,
                        // Con mensajes sin leer, la vista previa resalta (como en WhatsApp).
                        fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.Normal,
                        color = AppTheme.colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (hasUnread) {
                        Spacer(Modifier.width(8.dp))
                        UnreadBadge(chat.unreadCount)
                    }
                }
            }
        }
    }
}

// Globito con la cantidad de mensajes sin leer.
@Composable
private fun UnreadBadge(count: Int) {
    val description = pluralStringResource(R.plurals.chats_unread_description, count, count)
    Box(
        modifier = Modifier
            .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
            .background(AppTheme.colors.actionPrimary, CircleShape)
            .padding(horizontal = 6.dp)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (count > 99) "99+" else count.toString(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colors.onActionPrimary,
            textAlign = TextAlign.Center,
        )
    }
}

// Texto de la vista previa: el mensaje automático se traduce y los míos llevan "Tú: ".
@Composable
private fun previewText(chat: ChatSummaryUi): String = when (chat.previewKind) {
    ChatPreviewKind.INTEREST_SENT -> stringResource(R.string.chats_preview_interest_sent)
    ChatPreviewKind.INTEREST_RECEIVED -> stringResource(R.string.chats_preview_interest_received)
    ChatPreviewKind.PHOTO -> stringResource(R.string.chats_preview_photo).let { photo ->
        if (chat.isLastFromMe) stringResource(R.string.chats_you_prefix, photo) else photo
    }
    ChatPreviewKind.TEXT -> if (chat.isLastFromMe) stringResource(R.string.chats_you_prefix, chat.lastMessage) else chat.lastMessage
}

@Preview(showBackground = true, heightDp = 640)
@Composable
private fun ChatListPreview() {
    val now = System.currentTimeMillis()
    PetAdoptionAppTheme {
        ChatListContent(
            uiState = ChatListUiState(
                isLoading = false,
                chats = listOf(
                    ChatSummaryUi("1", "u1", "María González", null, false, "Luna busca hogar", ChatPreviewKind.TEXT, "¿Podemos vernos mañana para conocer a Luna?", false, now - 60_000, 2),
                    ChatSummaryUi("2", "u2", "Carlos Ruiz", null, false, "Luna busca hogar", ChatPreviewKind.TEXT, "¿Cómo es su temperamento con niños pequeños?", true, now - 3_600_000, 0),
                    ChatSummaryUi("3", "u3", "Ana Martínez", null, false, "Luna busca hogar", ChatPreviewKind.INTEREST_RECEIVED, "", false, now - 26 * 3_600_000, 0),
                    ChatSummaryUi("4", "u4", "Pedro López", null, false, "Luna busca hogar", ChatPreviewKind.TEXT, "¿Aceptan solicitudes de adopción desde Medellín?", false, now - 50 * 3_600_000, 0),
                ),
            ),
            padding = PaddingValues(),
            onQueryChange = {},
            onChatClick = {},
        )
    }
}
