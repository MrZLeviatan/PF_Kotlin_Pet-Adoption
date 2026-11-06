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
Pantalla de la pestaña Mensajes del usuario: muestra un buscador y la lista de chats, ordenada
del más reciente al más antiguo. Cada chat muestra a la otra persona, la publicación de la que
trata, el último mensaje, su fecha y cuántos mensajes hay sin leer.
Estado: observa ChatListUiState desde ChatListViewModel (chats, búsqueda, carga, errores y el chat
seleccionado para eliminar).
Eventos: escribir en el buscador (onQueryChange), tocar un chat (onChatClick), mantenerlo presionado
(onChatLongClick) para abrir DeleteChatDialog y confirmar o cancelar la eliminación.
Casos de uso: no los llama directamente; el ViewModel usa GetMyChatsUseCase, GetUserProfileUseCase,
GetPostDetailUseCase y DeleteChatUseCase.
Navegación: no decide a dónde navegar; avisa con onChatClick y AppNavigation abre la conversación
(ChatScreen). Al volver a la pantalla se recarga la lista.
**/

// Composable principal de la pestaña Mensajes, conectado al ViewModel.
@Composable
fun ChatListScreen(
    // Márgenes que impone el Scaffold contenedor (barra superior e inferior).
    padding: PaddingValues,
    // Acción para abrir la conversación del chat tocado; la resuelve la navegación.
    onChatClick: (chatId: String) -> Unit,
    // ViewModel de la lista, inyectado por Hilt.
    viewModel: ChatListViewModel = hiltViewModel(),
) {
    // Observa el estado del ViewModel respetando el ciclo de vida de la pantalla.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Al volver de una conversación se recarga (último mensaje y no leídos).
    LifecycleResumeEffect(Unit) {
        // Recarga la lista cada vez que la pantalla pasa a primer plano.
        viewModel.refresh()
        // No hay nada que limpiar al pausar o salir.
        onPauseOrDispose { }
    }

    // Dibuja el contenido de la lista con el estado actual.
    ChatListContent(
        // Estado de la pantalla.
        uiState = uiState,
        // Márgenes del contenedor.
        padding = padding,
        // Cambios del buscador van al ViewModel.
        onQueryChange = viewModel::onQueryChange,
        // Tocar un chat abre la conversación.
        onChatClick = onChatClick,
        // Mantener presionado un chat abre el diálogo de eliminación.
        onChatLongClick = viewModel::onChatLongClick,
    )

    // Si hay un chat seleccionado para eliminar, muestra el diálogo de confirmación.
    if (uiState.chatToDelete != null) {
        // Diálogo con las opciones de eliminar para ambos, solo para mí o cancelar.
        DeleteChatDialog(
            // Elimina el chat para ambos participantes.
            onDeleteForEveryone = { viewModel.onDeleteConfirm(forEveryone = true) },
            // Elimina el chat solo para el usuario actual.
            onDeleteForMe = { viewModel.onDeleteConfirm(forEveryone = false) },
            // Cierra el diálogo sin eliminar.
            onDismiss = viewModel::onDeleteDismiss,
            // Muestra carga mientras se elimina.
            isLoading = uiState.isDeleting,
        )
    }
}

// Sin lógica ni ViewModel: recibe el estado y dibuja. Es lo que se previsualiza.
@Composable
private fun ChatListContent(
    // Estado que se va a dibujar.
    uiState: ChatListUiState,
    // Márgenes del contenedor.
    padding: PaddingValues,
    // Acción al escribir en el buscador.
    onQueryChange: (String) -> Unit,
    // Acción al tocar un chat.
    onChatClick: (String) -> Unit,
    // Acción al mantener presionado un chat (vacía por defecto para la previsualización).
    onChatLongClick: (String) -> Unit = {},
) {
    // Columna que organiza el buscador y la lista verticalmente.
    Column(
        modifier = Modifier
            // Ocupa toda la pantalla disponible.
            .fillMaxSize()
            // Pinta el fondo con el color de superficie elevada del tema.
            .background(AppTheme.colors.surfaceElevated)
            // Respeta los márgenes del Scaffold.
            .padding(padding),
    ) {
        // Campo de búsqueda por persona o publicación.
        SearchField(
            // Texto actual de la búsqueda.
            query = uiState.query,
            // Notifica cada cambio del texto.
            onQueryChange = onQueryChange,
            // Texto de ayuda cuando el campo está vacío.
            placeholder = stringResource(R.string.chats_search_placeholder_full),
            // Descripción del ícono para accesibilidad.
            iconDescription = stringResource(R.string.chats_search_description),
            // Color del borde del campo.
            borderColor = AppTheme.colors.border,
            // Márgenes alrededor del buscador.
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 12.dp),
        )

        // Chats que coinciden con la búsqueda actual.
        val chats = uiState.visibleChats
        // Mensaje de estado a mostrar (o null si no hay que mostrar ninguno).
        val message = when {
            // Mientras carga no se muestra mensaje.
            uiState.isLoading -> null
            // Error al cargar la lista.
            uiState.hasError -> stringResource(R.string.chats_error)
            // Error al eliminar un chat.
            uiState.deleteError -> stringResource(R.string.chat_delete_error)
            // El usuario no tiene chats.
            uiState.chats.isEmpty() -> stringResource(R.string.chats_empty)
            // Hay chats, pero ninguno coincide con la búsqueda.
            chats.isEmpty() -> stringResource(R.string.chats_search_empty)
            // En cualquier otro caso no hay mensaje.
            else -> null
        }
        // Si hay mensaje, lo muestra.
        if (message != null) {
            // Texto informativo o de error centrado.
            StatusMessage(
                // Texto del mensaje.
                text = message,
                // Se pinta como error si falló la carga o la eliminación.
                isError = uiState.hasError || uiState.deleteError,
                modifier = Modifier
                    // Ocupa todo el ancho.
                    .fillMaxWidth()
                    // Deja aire alrededor del mensaje.
                    .padding(horizontal = 32.dp, vertical = 48.dp),
            )
        }

        // Lista perezosa que solo compone los chats visibles en pantalla.
        LazyColumn(
            // Márgenes internos de la lista.
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            // Separación vertical entre tarjetas.
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Un elemento por chat, usando su id como clave estable.
            items(chats, key = { it.id }) { chat ->
                // Tarjeta del chat con sus acciones de toque y de mantener presionado.
                ChatRow(chat = chat, onClick = { onChatClick(chat.id) }, onLongClick = { onChatLongClick(chat.id) })
            }
        }
    }
}

// Tarjeta de un chat: avatar, nombre y fecha arriba; último mensaje y no leídos abajo.
@Composable
private fun ChatRow(chat: ChatSummaryUi, onClick: () -> Unit, onLongClick: () -> Unit) {
    // Indica si el chat tiene mensajes sin leer.
    val hasUnread = chat.unreadCount > 0
    // Tarjeta táctil de la aplicación.
    AppCard(
        // Acción al tocar la tarjeta.
        onClick = onClick,
        // Acción al mantener presionada la tarjeta.
        onLongClick = onLongClick,
        // Márgenes internos de la tarjeta.
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
    ) {
        // Fila con el avatar a la izquierda y la información a la derecha.
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Nombre a mostrar: "Usuario eliminado" si la cuenta ya no existe, si no el alias.
            val name = if (chat.isOtherDeleted) stringResource(R.string.chat_deleted_user) else chat.otherName
            // Avatar circular con la foto o las iniciales de la otra persona.
            UserAvatar(name = name, photoUrl = chat.otherPhotoUrl, size = 44.dp)
            // Espacio entre el avatar y el texto.
            Spacer(Modifier.width(12.dp))
            // Columna con la información del chat que ocupa el ancho restante.
            Column(Modifier.weight(1f)) {
                // Fila superior: nombre y fecha del último mensaje.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Nombre de la otra persona.
                    Text(
                        // Texto del nombre.
                        text = name,
                        // Estilo de título pequeño.
                        style = MaterialTheme.typography.titleSmall,
                        // En negrita.
                        fontWeight = FontWeight.Bold,
                        // Color de texto principal.
                        color = AppTheme.colors.textPrimary,
                        // Una sola línea.
                        maxLines = 1,
                        // Si no cabe, se corta con puntos suspensivos.
                        overflow = TextOverflow.Ellipsis,
                        // Ocupa el espacio sobrante para empujar la fecha a la derecha.
                        modifier = Modifier.weight(1f),
                    )
                    // Espacio entre el nombre y la fecha.
                    Spacer(Modifier.width(8.dp))
                    // Fecha del último mensaje ("10:24", "Ayer" o "Hace N días").
                    Text(
                        // Texto de la fecha calculado con chatListTimeText.
                        text = chatListTimeText(chat.lastMessageAt),
                        // Estilo de etiqueta pequeña.
                        style = MaterialTheme.typography.labelSmall,
                        // En negrita.
                        fontWeight = FontWeight.Bold,
                        // Color verde de éxito.
                        color = AppTheme.colors.stateSuccess,
                    )
                }
                // Publicación de la que trata el chat (dos chats con la misma persona se distinguen así).
                chat.postTitle?.let { title ->
                    // Fila con el ícono de huella y el título de la publicación.
                    IconTextRow(
                        // Ícono de huella.
                        icon = Icons.Outlined.Pets,
                        // Título de la publicación.
                        text = title,
                        // Tamaño del ícono.
                        iconSize = 12.dp,
                        // Separación entre ícono y texto.
                        spacing = 4.dp,
                        // Estilo de etiqueta pequeña.
                        style = MaterialTheme.typography.labelSmall,
                        // Una sola línea.
                        maxLines = 1,
                    )
                }
                // Pequeño espacio antes de la vista previa.
                Spacer(Modifier.height(2.dp))
                // Fila inferior: vista previa del último mensaje y contador de no leídos.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Vista previa del último mensaje.
                    Text(
                        // Texto de la vista previa calculado con previewText.
                        text = previewText(chat),
                        // Estilo de cuerpo pequeño.
                        style = MaterialTheme.typography.bodySmall,
                        // Con mensajes sin leer, la vista previa resalta (como en WhatsApp).
                        fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.Normal,
                        // Color de texto principal.
                        color = AppTheme.colors.textPrimary,
                        // Una sola línea.
                        maxLines = 1,
                        // Si no cabe, se corta con puntos suspensivos.
                        overflow = TextOverflow.Ellipsis,
                        // Ocupa el espacio sobrante para dejar el contador a la derecha.
                        modifier = Modifier.weight(1f),
                    )
                    // Solo si hay mensajes sin leer se muestra el contador.
                    if (hasUnread) {
                        // Espacio entre la vista previa y el contador.
                        Spacer(Modifier.width(8.dp))
                        // Globito con la cantidad de no leídos.
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
    // Descripción accesible en plural ("3 mensajes sin leer") para lectores de pantalla.
    val description = pluralStringResource(R.plurals.chats_unread_description, count, count)
    // Contenedor circular del número.
    Box(
        modifier = Modifier
            // Tamaño mínimo para que siempre se vea como un círculo.
            .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
            // Fondo circular con el color de acción principal.
            .background(AppTheme.colors.actionPrimary, CircleShape)
            // Margen horizontal para números de varias cifras.
            .padding(horizontal = 6.dp)
            // Asigna la descripción accesible al globito.
            .semantics { contentDescription = description },
        // Centra el número dentro del círculo.
        contentAlignment = Alignment.Center,
    ) {
        // Número de mensajes sin leer.
        Text(
            // Muestra "99+" si hay más de 99, si no el número exacto.
            text = if (count > 99) "99+" else count.toString(),
            // Tamaño de letra pequeño.
            fontSize = 11.sp,
            // En negrita.
            fontWeight = FontWeight.Bold,
            // Color de texto sobre el color de acción principal.
            color = AppTheme.colors.onActionPrimary,
            // Texto centrado.
            textAlign = TextAlign.Center,
        )
    }
}

// Texto de la vista previa: el mensaje automático se traduce y los míos llevan "Tú: ".
@Composable
private fun previewText(chat: ChatSummaryUi): String = when (chat.previewKind) {
    // Interés de adopción enviado por mí.
    ChatPreviewKind.INTEREST_SENT -> stringResource(R.string.chats_preview_interest_sent)
    // Interés de adopción recibido de la otra persona.
    ChatPreviewKind.INTEREST_RECEIVED -> stringResource(R.string.chats_preview_interest_received)
    // Foto: muestra "Foto" y, si es mía, le antepone "Tú: ".
    ChatPreviewKind.PHOTO -> stringResource(R.string.chats_preview_photo).let { photo ->
        // Añade el prefijo "Tú: " si el último mensaje es mío.
        if (chat.isLastFromMe) stringResource(R.string.chats_you_prefix, photo) else photo
    }
    // Texto: muestra el mensaje y, si es mío, le antepone "Tú: ".
    ChatPreviewKind.TEXT -> if (chat.isLastFromMe) stringResource(R.string.chats_you_prefix, chat.lastMessage) else chat.lastMessage
}

// Previsualización de la lista de chats con datos de ejemplo en Android Studio.
@Preview(showBackground = true, heightDp = 640)
@Composable
private fun ChatListPreview() {
    // Momento actual para calcular fechas de ejemplo.
    val now = System.currentTimeMillis()
    // Aplica el tema de la aplicación.
    PetAdoptionAppTheme {
        // Dibuja el contenido sin ViewModel.
        ChatListContent(
            // Estado de ejemplo ya cargado.
            uiState = ChatListUiState(
                // Sin carga en curso.
                isLoading = false,
                // Chats de ejemplo con distintas fechas y tipos de vista previa.
                chats = listOf(
                    ChatSummaryUi("1", "u1", "María González", null, false, "Luna busca hogar", ChatPreviewKind.TEXT, "¿Podemos vernos mañana para conocer a Luna?", false, now - 60_000, 2),
                    ChatSummaryUi("2", "u2", "Carlos Ruiz", null, false, "Luna busca hogar", ChatPreviewKind.TEXT, "¿Cómo es su temperamento con niños pequeños?", true, now - 3_600_000, 0),
                    ChatSummaryUi("3", "u3", "Ana Martínez", null, false, "Luna busca hogar", ChatPreviewKind.INTEREST_RECEIVED, "", false, now - 26 * 3_600_000, 0),
                    ChatSummaryUi("4", "u4", "Pedro López", null, false, "Luna busca hogar", ChatPreviewKind.TEXT, "¿Aceptan solicitudes de adopción desde Medellín?", false, now - 50 * 3_600_000, 0),
                ),
            ),
            // Sin márgenes externos.
            padding = PaddingValues(),
            // Acciones vacías en la previsualización.
            onQueryChange = {},
            onChatClick = {},
        )
    }
}
