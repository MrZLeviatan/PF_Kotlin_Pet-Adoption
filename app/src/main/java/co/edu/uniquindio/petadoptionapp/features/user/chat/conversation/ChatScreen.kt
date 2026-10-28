package co.edu.uniquindio.petadoptionapp.features.user.chat.conversation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.uniquindio.petadoptionapp.R
import co.edu.uniquindio.petadoptionapp.core.component.button.BackButton
import co.edu.uniquindio.petadoptionapp.core.component.form.MessageInputBar
import co.edu.uniquindio.petadoptionapp.core.component.icon.UserAvatar
import co.edu.uniquindio.petadoptionapp.core.component.text.ErrorText
import co.edu.uniquindio.petadoptionapp.core.component.text.StatusMessage
import co.edu.uniquindio.petadoptionapp.core.theme.AppTheme
import co.edu.uniquindio.petadoptionapp.core.theme.PetAdoptionAppTheme
import co.edu.uniquindio.petadoptionapp.domain.model.messaging.MessageType
import co.edu.uniquindio.petadoptionapp.domain.usecase.validator.ChatValidator
import co.edu.uniquindio.petadoptionapp.features.user.chat.DeleteChatDialog
import co.edu.uniquindio.petadoptionapp.features.user.chat.chatClockText
import co.edu.uniquindio.petadoptionapp.features.user.chat.chatDayText
import co.edu.uniquindio.petadoptionapp.features.user.chat.isSameChatDay

/**
 Conversación con otra persona (tipo WhatsApp): mis mensajes a la derecha en amarillo, los suyos a la izquierda,
 separados por día, con "Visto" bajo mi último mensaje cuando ya lo leyó.
 El "Me interesa adoptar" se muestra como mensaje automático con el título de la publicación.
 El menú ⋮ permite eliminar el chat para mí o para ambos; al eliminarlo se vuelve atrás.
 No decide a dónde navegar: avisa y AppNavigation resuelve el destino.
 */
@Composable
fun ChatScreen(
    onBack: () -> Unit,
    onUserClick: (userId: String) -> Unit,
    onPostClick: (postId: String) -> Unit,
    viewModel: ChatViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnBack by rememberUpdatedState(onBack)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                ChatEvent.Deleted -> currentOnBack()
            }
        }
    }

    ChatContent(
        uiState = uiState,
        actions = ChatActions(
            onBack = onBack,
            onUserClick = { if (uiState.otherUserId.isNotEmpty()) onUserClick(uiState.otherUserId) },
            onPostClick = { uiState.postId?.let(onPostClick) },
            onTextChange = viewModel::onTextChange,
            onSend = viewModel::onSend,
            onLoadOlder = viewModel::onLoadOlder,
            onDelete = viewModel::onDeleteClick,
            onDeleteForEveryone = { viewModel.onDeleteConfirm(forEveryone = true) },
            onDeleteForMe = { viewModel.onDeleteConfirm(forEveryone = false) },
            onDeleteDismiss = viewModel::onDeleteDismiss,
        ),
    )
}

// Acciones de la pantalla (mismo patrón que las demás); con valores vacíos para las previews.
private data class ChatActions(
    val onBack: () -> Unit = {},
    val onUserClick: () -> Unit = {},
    val onPostClick: () -> Unit = {},
    val onTextChange: (String) -> Unit = {},
    val onSend: () -> Unit = {},
    val onLoadOlder: () -> Unit = {},
    val onDelete: () -> Unit = {},
    val onDeleteForEveryone: () -> Unit = {},
    val onDeleteForMe: () -> Unit = {},
    val onDeleteDismiss: () -> Unit = {},
)

// Sin lógica ni ViewModel: recibe el estado y dibuja. Es lo que se previsualiza.
@Composable
private fun ChatContent(uiState: ChatUiState, actions: ChatActions) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.surfaceElevated)
            .statusBarsPadding()
            .imePadding(),   // Sube la barra de escribir cuando aparece el teclado
    ) {
        ChatHeader(uiState = uiState, actions = actions)
        HorizontalDivider(color = AppTheme.colors.border)

        if (uiState.showDeleteDialog) {
            DeleteChatDialog(
                onDeleteForEveryone = actions.onDeleteForEveryone,
                onDeleteForMe = actions.onDeleteForMe,
                onDismiss = actions.onDeleteDismiss,
                isLoading = uiState.isDeleting,
            )
        }

        if (uiState.notFound) {
            StatusMessage(
                text = stringResource(R.string.chat_not_found),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(48.dp),
            )
            return@Column
        }

        val listState = rememberLazyListState()
        val messages = uiState.messages
        // Al llegar un mensaje nuevo (o al abrir), baja hasta el último.
        LaunchedEffect(messages.lastOrNull()?.id) {
            if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size)
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            item(key = "older") {
                if (uiState.canLoadOlder) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        TextButton(onClick = actions.onLoadOlder, enabled = !uiState.isLoadingOlder) {
                            Text(stringResource(R.string.chat_load_older), color = AppTheme.colors.textLink)
                        }
                    }
                }
            }
            itemsIndexed(messages, key = { _, message -> message.id }) { index, message ->
                Column {
                    val previous = messages.getOrNull(index - 1)
                    if (previous == null || !isSameChatDay(previous.sentAt, message.sentAt)) {
                        DaySeparator(chatDayText(message.sentAt))
                    }
                    MessageBubble(message = message, postTitle = uiState.postTitle)
                    if (message.id == uiState.seenMessageId) {
                        Text(
                            text = stringResource(R.string.chat_seen),
                            style = MaterialTheme.typography.labelSmall,
                            color = AppTheme.colors.textPrimary,
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(top = 2.dp, end = 4.dp),
                        )
                    }
                }
            }
        }

        if (uiState.sendError || uiState.deleteError) {
            ErrorText(
                text = stringResource(if (uiState.deleteError) R.string.chat_delete_error else R.string.chat_send_error),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
        MessageInputBar(
            text = uiState.text,
            onTextChange = actions.onTextChange,
            onSend = actions.onSend,
            placeholder = stringResource(R.string.chat_placeholder),
            sendDescription = stringResource(R.string.chat_send),
            maxLength = ChatValidator.MAX_MESSAGE_LENGTH,
            isSending = uiState.isSending,
        )
    }
}

// Encabezado: volver, avatar y nombre (abren el perfil) y la publicación del chat (abre su detalle).
@Composable
private fun ChatHeader(uiState: ChatUiState, actions: ChatActions) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BackButton(onClick = actions.onBack)
        UserAvatar(
            name = uiState.otherName,
            photoUrl = uiState.otherPhotoUrl,
            size = 40.dp,
            modifier = Modifier.clickable(role = Role.Button, onClick = actions.onUserClick),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = uiState.otherName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable(role = Role.Button, onClick = actions.onUserClick),
            )
            uiState.postTitle?.let { title ->
                Text(
                    text = stringResource(R.string.chat_about_post, title),
                    style = MaterialTheme.typography.labelMedium,
                    color = AppTheme.colors.textLink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clickable(role = Role.Button, onClick = actions.onPostClick),
                )
            }
        }
        // Opciones del chat (por ahora, eliminarlo). Sin chat no hay nada que eliminar.
        if (!uiState.notFound && !uiState.isLoading) {
            Box {
                var menuOpen by remember { mutableStateOf(false) }
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Outlined.MoreVert, contentDescription = stringResource(R.string.chat_options), tint = AppTheme.colors.textPrimary)
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }, containerColor = AppTheme.colors.surface) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.chat_delete), color = AppTheme.colors.stateError) },
                        leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = AppTheme.colors.stateError) },
                        onClick = {
                            menuOpen = false
                            actions.onDelete()
                        },
                    )
                }
            }
        } else {
            Spacer(Modifier.width(8.dp))
        }
    }
}

// "Hoy", "Ayer" o la fecha, centrado en una píldora.
@Composable
private fun DaySeparator(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = AppTheme.colors.textPrimary,
            modifier = Modifier
                .background(AppTheme.colors.surfaceTag, RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 4.dp),
        )
    }
}

// Burbuja: a la derecha y amarilla si es mía; a la izquierda y clara si es de la otra persona.
@Composable
private fun MessageBubble(message: ChatMessageUi, postTitle: String?) {
    val shape = if (message.isMine) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isMine) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(if (message.isMine) AppTheme.colors.actionPrimary else AppTheme.colors.surface, shape)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            if (message.type == MessageType.ADOPTION_INTEREST) {
                // Mensaje automático de "Me interesa adoptar": etiqueta y texto armado con el título.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Pets, contentDescription = null, tint = AppTheme.colors.textPrimary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.chat_auto_label),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.textPrimary,
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (postTitle != null) stringResource(R.string.chat_auto_interest, postTitle)
                    else stringResource(R.string.chat_auto_interest_no_title),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.colors.textPrimary,
                )
            }
            // En el automático, [text] es el mensaje opcional que escribió quien solicitó.
            if (message.text.isNotBlank()) {
                Text(text = message.text, style = MaterialTheme.typography.bodyMedium, color = AppTheme.colors.textPrimary)
            }
            Text(
                text = chatClockText(message.sentAt),
                fontSize = 10.sp,
                color = AppTheme.colors.textPrimary.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 2.dp),
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 720)
@Composable
private fun ChatPreview() {
    val now = System.currentTimeMillis()
    val day = 24 * 60 * 60 * 1000L
    PetAdoptionAppTheme {
        ChatContent(
            uiState = ChatUiState(
                isLoading = false,
                otherName = "luis_9",
                postId = "p1",
                postTitle = "Perrito en adopción",
                messages = listOf(
                    ChatMessageUi("1", false, MessageType.ADOPTION_INTEREST, "", now - 2 * day),
                    ChatMessageUi("2", true, MessageType.TEXT, "¡Hola Luis! Gracias por escribir. Es muy juguetón y sociable.", now - 2 * day + 600_000),
                    ChatMessageUi("3", false, MessageType.TEXT, "¿Podemos vernos mañana para conocerlo?", now - 1_200_000),
                    ChatMessageUi("4", true, MessageType.TEXT, "¡Claro! ¿A las 10 te sirve?", now - 600_000),
                ),
                seenMessageId = "4",
            ),
            actions = ChatActions(),
        )
    }
}
