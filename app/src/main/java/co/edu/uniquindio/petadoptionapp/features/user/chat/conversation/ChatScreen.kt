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
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
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
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import co.edu.uniquindio.petadoptionapp.core.component.photo.ImageSourcePicker
import co.edu.uniquindio.petadoptionapp.core.component.photo.PhotoViewerDialog
import coil3.compose.AsyncImage
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
Pantalla de conversación con otra persona (tipo WhatsApp): mis mensajes a la derecha en amarillo,
los suyos a la izquierda, separados por día, con la hora en cada burbuja, un check (enviado) o doble
check (visto) en los míos y "Visto" bajo mi último mensaje cuando la otra persona ya lo leyó.
El "Me interesa adoptar" se muestra como mensaje automático con el título de la publicación, y las
fotos se pueden ampliar a pantalla completa.
Estado: observa ChatUiState desde ChatViewModel. Eventos: escribir y enviar texto, adjuntar foto
(cámara o galería con ImageSourcePicker), cargar mensajes anteriores, abrir el perfil o la
publicación y eliminar el chat desde el menú ⋮ (DeleteChatDialog).
Casos de uso: no los llama directamente; los usa ChatViewModel (envío, lectura, mensajes en vivo,
perfil, publicación y eliminación).
Navegación: no decide a dónde navegar; avisa con onBack, onUserClick y onPostClick y AppNavigation
resuelve el destino. Al recibir ChatEvent.Deleted vuelve atrás.
**/

// Composable principal de la conversación, conectado al ViewModel.
@Composable
fun ChatScreen(
    // Acción para volver a la pantalla anterior.
    onBack: () -> Unit,
    // Acción para abrir el perfil de la otra persona.
    onUserClick: (userId: String) -> Unit,
    // Acción para abrir el detalle de la publicación del chat.
    onPostClick: (postId: String) -> Unit,
    // ViewModel de la conversación, inyectado por Hilt.
    viewModel: ChatViewModel = hiltViewModel(),
) {
    // Observa el estado del ViewModel respetando el ciclo de vida.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Mantiene siempre la versión más reciente de onBack para usarla dentro del efecto.
    val currentOnBack by rememberUpdatedState(onBack)

    // Escucha los eventos de una sola vez del ViewModel mientras la pantalla está activa.
    LaunchedEffect(viewModel) {
        // Recoge cada evento emitido.
        viewModel.events.collect { event ->
            // Reacciona según el tipo de evento.
            when (event) {
                // Si el chat se eliminó, vuelve atrás.
                ChatEvent.Deleted -> currentOnBack()
            }
        }
    }

    // Dibuja el contenido con el estado y las acciones conectadas al ViewModel.
    ChatContent(
        // Estado actual de la pantalla.
        uiState = uiState,
        // Agrupa todas las acciones de la pantalla.
        actions = ChatActions(
            // Volver atrás.
            onBack = onBack,
            // Una cuenta eliminada no tiene perfil que abrir.
            onUserClick = { if (uiState.otherUserId.isNotEmpty() && !uiState.isOtherDeleted) onUserClick(uiState.otherUserId) },
            // Abre la publicación solo si el chat tiene una.
            onPostClick = { uiState.postId?.let(onPostClick) },
            // Cambios del texto del mensaje.
            onTextChange = viewModel::onTextChange,
            // Enviar el mensaje escrito.
            onSend = viewModel::onSend,
            // Cargar mensajes anteriores.
            onLoadOlder = viewModel::onLoadOlder,
            // Enviar la foto elegida.
            onImagePicked = viewModel::onImagePicked,
            // Abrir el diálogo de eliminación.
            onDelete = viewModel::onDeleteClick,
            // Eliminar el chat para ambos.
            onDeleteForEveryone = { viewModel.onDeleteConfirm(forEveryone = true) },
            // Eliminar el chat solo para mí.
            onDeleteForMe = { viewModel.onDeleteConfirm(forEveryone = false) },
            // Cerrar el diálogo de eliminación.
            onDeleteDismiss = viewModel::onDeleteDismiss,
        ),
    )
}

// Acciones de la pantalla (mismo patrón que las demás); con valores vacíos para las previews.
private data class ChatActions(
    // Volver atrás.
    val onBack: () -> Unit = {},
    // Abrir el perfil de la otra persona.
    val onUserClick: () -> Unit = {},
    // Abrir la publicación del chat.
    val onPostClick: () -> Unit = {},
    // Cambiar el texto del mensaje.
    val onTextChange: (String) -> Unit = {},
    // Enviar el mensaje.
    val onSend: () -> Unit = {},
    // Cargar mensajes anteriores.
    val onLoadOlder: () -> Unit = {},
    // Enviar una foto ya subida (recibe su URL).
    val onImagePicked: (String) -> Unit = {},
    // Abrir el diálogo de eliminación.
    val onDelete: () -> Unit = {},
    // Eliminar para ambos.
    val onDeleteForEveryone: () -> Unit = {},
    // Eliminar solo para mí.
    val onDeleteForMe: () -> Unit = {},
    // Cerrar el diálogo de eliminación.
    val onDeleteDismiss: () -> Unit = {},
)

// Sin lógica ni ViewModel: recibe el estado y dibuja. Es lo que se previsualiza.
@Composable
private fun ChatContent(uiState: ChatUiState, actions: ChatActions) {
    // Columna principal: encabezado, mensajes y barra de escritura.
    Column(
        modifier = Modifier
            // Ocupa toda la pantalla.
            .fillMaxSize()
            // Fondo con el color de superficie elevada.
            .background(AppTheme.colors.surfaceElevated)
            // Deja espacio para la barra de estado del sistema.
            .statusBarsPadding()
            // Sube la barra de escribir cuando aparece el teclado.
            .imePadding(),
    ) {
        // Encabezado con volver, avatar, nombre, publicación y menú.
        ChatHeader(uiState = uiState, actions = actions)
        // Línea divisoria bajo el encabezado.
        HorizontalDivider(color = AppTheme.colors.border)

        // Si se pidió eliminar, muestra el diálogo de confirmación.
        if (uiState.showDeleteDialog) {
            // Diálogo con las opciones de eliminar.
            DeleteChatDialog(
                // Eliminar para ambos.
                onDeleteForEveryone = actions.onDeleteForEveryone,
                // Eliminar solo para mí.
                onDeleteForMe = actions.onDeleteForMe,
                // Cerrar sin eliminar.
                onDismiss = actions.onDeleteDismiss,
                // Carga mientras se elimina.
                isLoading = uiState.isDeleting,
            )
        }

        // Si el chat no existe o no se tiene acceso, muestra un aviso y no dibuja nada más.
        if (uiState.notFound) {
            // Mensaje de chat no encontrado.
            StatusMessage(
                // Texto del aviso.
                text = stringResource(R.string.chat_not_found),
                modifier = Modifier
                    // Ocupa todo el ancho.
                    .fillMaxWidth()
                    // Margen amplio alrededor.
                    .padding(48.dp),
            )
            // Termina el contenido de la columna aquí.
            return@Column
        }

        // Estado de desplazamiento de la lista de mensajes.
        val listState = rememberLazyListState()
        // Mensajes a mostrar.
        val messages = uiState.messages
        // Al llegar un mensaje nuevo (o al abrir), baja hasta el último.
        LaunchedEffect(messages.lastOrNull()?.id) {
            // Solo desplaza si hay mensajes; el índice messages.size apunta al final de la lista.
            if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size)
        }

        // Lista perezosa de mensajes.
        LazyColumn(
            // Estado de desplazamiento controlado arriba.
            state = listState,
            modifier = Modifier
                // Ocupa el espacio restante entre el encabezado y la barra de escritura.
                .weight(1f)
                // Ocupa todo el ancho.
                .fillMaxWidth(),
            // Márgenes internos de la lista.
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
            // Separación vertical entre mensajes.
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // Primer elemento: botón para cargar mensajes anteriores.
            item(key = "older") {
                // Solo se muestra si puede haber mensajes anteriores.
                if (uiState.canLoadOlder) {
                    // Contenedor que centra el botón.
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        // Botón de texto deshabilitado mientras se cargan los anteriores.
                        TextButton(onClick = actions.onLoadOlder, enabled = !uiState.isLoadingOlder) {
                            // Texto "Ver mensajes anteriores" con color de enlace.
                            Text(stringResource(R.string.chat_load_older), color = AppTheme.colors.textLink)
                        }
                    }
                }
            }
            // Un elemento por mensaje, con su id como clave estable.
            itemsIndexed(messages, key = { _, message -> message.id }) { index, message ->
                // Columna que agrupa separador de día, burbuja y "Visto".
                Column {
                    // Mensaje anterior, para saber si cambió el día.
                    val previous = messages.getOrNull(index - 1)
                    // Si es el primero o cambió el día respecto al anterior, pone un separador.
                    if (previous == null || !isSameChatDay(previous.sentAt, message.sentAt)) {
                        // Píldora con "Hoy", "Ayer" o la fecha.
                        DaySeparator(chatDayText(message.sentAt))
                    }
                    // Burbuja del mensaje.
                    MessageBubble(
                        // Datos del mensaje.
                        message = message,
                        // Título de la publicación para el mensaje automático.
                        postTitle = uiState.postTitle,
                        // Es "visto" si es mío y se envió antes de la última lectura de la otra persona.
                        isSeen = message.isMine && message.sentAt <= uiState.seenUntil,
                    )
                    // "Visto" bajo mi último mensaje leído, con el mismo estilo que la hora de la burbuja.
                    if (message.id == uiState.seenMessageId) {
                        // Fila con el doble check y el texto "Visto".
                        Row(
                            modifier = Modifier
                                // Alineada a la derecha, como mis mensajes.
                                .align(Alignment.End)
                                // Pequeño margen superior y derecho.
                                .padding(top = 4.dp, end = 6.dp),
                            // Centra verticalmente ícono y texto.
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Ícono de doble check decorativo.
                            Icon(Icons.Filled.DoneAll, contentDescription = null, tint = AppTheme.colors.textLink, modifier = Modifier.size(14.dp))
                            // Espacio entre ícono y texto.
                            Spacer(Modifier.width(4.dp))
                            // Texto "Visto".
                            Text(
                                // Texto desde strings.xml.
                                text = stringResource(R.string.chat_seen),
                                // Tamaño de letra pequeño.
                                fontSize = 11.sp,
                                // Peso medio.
                                fontWeight = FontWeight.Medium,
                                // Color de enlace.
                                color = AppTheme.colors.textLink,
                            )
                        }
                    }
                }
            }
        }

        // Si falló el envío o la eliminación, muestra el error sobre la barra de escritura.
        if (uiState.sendError || uiState.deleteError) {
            // Texto de error.
            ErrorText(
                // Elige el mensaje según el tipo de error.
                text = stringResource(if (uiState.deleteError) R.string.chat_delete_error else R.string.chat_send_error),
                modifier = Modifier
                    // Ocupa todo el ancho.
                    .fillMaxWidth()
                    // Márgenes alrededor.
                    .padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
        // Adjuntar foto: cámara (pide permiso) o galería; se envía apenas se elige. Este estado sobrevive a rotaciones.
        var showPhotoSheet by rememberSaveable { mutableStateOf(false) }
        // Barra inferior para escribir y enviar mensajes.
        MessageInputBar(
            // Texto actual.
            text = uiState.text,
            // Cambios del texto.
            onTextChange = actions.onTextChange,
            // Enviar.
            onSend = actions.onSend,
            // Texto de ayuda del campo.
            placeholder = stringResource(R.string.chat_placeholder),
            // Descripción accesible del botón enviar.
            sendDescription = stringResource(R.string.chat_send),
            // Límite de caracteres definido por el validador del chat.
            maxLength = ChatValidator.MAX_MESSAGE_LENGTH,
            // Indica envío en curso.
            isSending = uiState.isSending,
            // Contenido a la izquierda del campo: botón para adjuntar foto.
            leading = {
                // Botón que abre la hoja de cámara o galería (deshabilitado mientras se envía).
                IconButton(onClick = { showPhotoSheet = true }, enabled = !uiState.isSending) {
                    // Ícono de añadir foto.
                    Icon(
                        // Imagen del ícono.
                        Icons.Outlined.AddPhotoAlternate,
                        // Descripción accesible.
                        contentDescription = stringResource(R.string.chat_attach_photo),
                        // Color del ícono.
                        tint = AppTheme.colors.textPrimary,
                    )
                }
            },
        )
        // Hoja para elegir la foto desde cámara o galería y subirla.
        ImageSourcePicker(
            // Visible cuando se tocó el botón de adjuntar.
            visible = showPhotoSheet,
            // Al cerrarla se oculta.
            onDismiss = { showPhotoSheet = false },
            // Al elegir, envía la primera URL subida.
            onPicked = { urls -> urls.firstOrNull()?.let(actions.onImagePicked) },
        )
    }
}

// Encabezado: volver, avatar y nombre (abren el perfil) y la publicación del chat (abre su detalle).
@Composable
private fun ChatHeader(uiState: ChatUiState, actions: ChatActions) {
    // Fila horizontal del encabezado.
    Row(
        modifier = Modifier
            // Ocupa todo el ancho.
            .fillMaxWidth()
            // Márgenes internos.
            .padding(horizontal = 4.dp, vertical = 8.dp),
        // Centra verticalmente los elementos.
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Botón para volver atrás.
        BackButton(onClick = actions.onBack)
        // Nombre a mostrar: "Usuario eliminado" si la cuenta ya no existe, si no el alias.
        val name = if (uiState.isOtherDeleted) stringResource(R.string.chat_deleted_user) else uiState.otherName
        // Avatar de la otra persona.
        UserAvatar(
            // Nombre (para las iniciales si no hay foto).
            name = name,
            // Foto de perfil.
            photoUrl = uiState.otherPhotoUrl,
            // Tamaño del avatar.
            size = 40.dp,
            // Tocar el avatar abre el perfil.
            modifier = Modifier.clickable(role = Role.Button, onClick = actions.onUserClick),
        )
        // Espacio entre avatar y textos.
        Spacer(Modifier.width(12.dp))
        // Columna con nombre y publicación que ocupa el espacio restante.
        Column(Modifier.weight(1f)) {
            // Nombre de la otra persona.
            Text(
                // Texto del nombre.
                text = name,
                // Estilo de título mediano.
                style = MaterialTheme.typography.titleMedium,
                // En negrita.
                fontWeight = FontWeight.Bold,
                // Color principal.
                color = AppTheme.colors.textPrimary,
                // Una sola línea.
                maxLines = 1,
                // Corta con puntos suspensivos.
                overflow = TextOverflow.Ellipsis,
                // Tocar el nombre abre el perfil.
                modifier = Modifier.clickable(role = Role.Button, onClick = actions.onUserClick),
            )
            // Si hay publicación visible, muestra "Sobre: título".
            uiState.postTitle?.let { title ->
                // Texto de la publicación como enlace.
                Text(
                    // Texto armado con el título.
                    text = stringResource(R.string.chat_about_post, title),
                    // Estilo de etiqueta mediana.
                    style = MaterialTheme.typography.labelMedium,
                    // Color de enlace.
                    color = AppTheme.colors.textLink,
                    // Una sola línea.
                    maxLines = 1,
                    // Corta con puntos suspensivos.
                    overflow = TextOverflow.Ellipsis,
                    // Tocarlo abre el detalle de la publicación.
                    modifier = Modifier.clickable(role = Role.Button, onClick = actions.onPostClick),
                )
            }
        }
        // Opciones del chat (por ahora, eliminarlo). Sin chat no hay nada que eliminar.
        if (!uiState.notFound && !uiState.isLoading) {
            // Contenedor que ancla el menú desplegable al botón.
            Box {
                // Estado local que indica si el menú está abierto.
                var menuOpen by remember { mutableStateOf(false) }
                // Botón ⋮ que abre el menú.
                IconButton(onClick = { menuOpen = true }) {
                    // Ícono de tres puntos con descripción accesible.
                    Icon(Icons.Outlined.MoreVert, contentDescription = stringResource(R.string.chat_options), tint = AppTheme.colors.textPrimary)
                }
                // Menú desplegable de opciones.
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }, containerColor = AppTheme.colors.surface) {
                    // Opción "Eliminar chat".
                    DropdownMenuItem(
                        // Texto en rojo.
                        text = { Text(stringResource(R.string.chat_delete), color = AppTheme.colors.stateError) },
                        // Ícono de papelera en rojo.
                        leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = AppTheme.colors.stateError) },
                        // Al tocarla:
                        onClick = {
                            // Cierra el menú.
                            menuOpen = false
                            // Abre el diálogo de eliminación.
                            actions.onDelete()
                        },
                    )
                }
            }
        } else {
            // Sin menú, deja un pequeño espacio al final.
            Spacer(Modifier.width(8.dp))
        }
    }
}

// "Hoy", "Ayer" o la fecha, centrado en una píldora.
@Composable
private fun DaySeparator(text: String) {
    // Contenedor que centra la píldora.
    Box(
        modifier = Modifier
            // Ocupa todo el ancho.
            .fillMaxWidth()
            // Margen vertical.
            .padding(vertical = 8.dp),
        // Centra el contenido.
        contentAlignment = Alignment.Center,
    ) {
        // Texto del día.
        Text(
            // Texto recibido.
            text = text,
            // Estilo de etiqueta pequeña.
            style = MaterialTheme.typography.labelSmall,
            // Color principal.
            color = AppTheme.colors.textPrimary,
            modifier = Modifier
                // Fondo en forma de píldora (esquinas al 50 %).
                .background(AppTheme.colors.surfaceTag, RoundedCornerShape(50))
                // Margen interno de la píldora.
                .padding(horizontal = 12.dp, vertical = 4.dp),
        )
    }
}

// Burbuja: a la derecha y amarilla si es mía; a la izquierda y clara si es de la otra persona.
@Composable
private fun MessageBubble(message: ChatMessageUi, postTitle: String?, isSeen: Boolean) {
    // Forma de la burbuja: la esquina inferior del lado del remitente queda casi recta.
    val shape = if (message.isMine) {
        // Mía: esquina inferior derecha pequeña.
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
    } else {
        // De la otra persona: esquina inferior izquierda pequeña.
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
    }
    // Fila que alinea la burbuja a la derecha o a la izquierda.
    Row(
        // Ocupa todo el ancho.
        modifier = Modifier.fillMaxWidth(),
        // Derecha si es mía, izquierda si no.
        horizontalArrangement = if (message.isMine) Arrangement.End else Arrangement.Start,
    ) {
        // Contenido de la burbuja.
        Column(
            modifier = Modifier
                // Ancho máximo para que la burbuja no ocupe toda la pantalla.
                .widthIn(max = 280.dp)
                // Fondo amarillo (acción principal) si es mía o de superficie si no, con la forma calculada.
                .background(if (message.isMine) AppTheme.colors.actionPrimary else AppTheme.colors.surface, shape)
                // Margen interno.
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            // Mensaje automático de "Me interesa adoptar": etiqueta y texto armado con el título.
            if (message.type == MessageType.ADOPTION_INTEREST) {
                // Fila con ícono de huella y la etiqueta "Mensaje automático".
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Ícono decorativo de huella.
                    Icon(Icons.Outlined.Pets, contentDescription = null, tint = AppTheme.colors.textPrimary, modifier = Modifier.size(14.dp))
                    // Espacio entre ícono y etiqueta.
                    Spacer(Modifier.width(4.dp))
                    // Etiqueta del mensaje automático.
                    Text(
                        // Texto desde strings.xml.
                        text = stringResource(R.string.chat_auto_label),
                        // Letra pequeña.
                        fontSize = 11.sp,
                        // En negrita.
                        fontWeight = FontWeight.Bold,
                        // Color principal.
                        color = AppTheme.colors.textPrimary,
                    )
                }
                // Espacio bajo la etiqueta.
                Spacer(Modifier.height(4.dp))
                // Texto del interés de adopción.
                Text(
                    // Con título si la publicación es visible; si no, un texto genérico.
                    text = if (postTitle != null) stringResource(R.string.chat_auto_interest, postTitle)
                    else stringResource(R.string.chat_auto_interest_no_title),
                    // Estilo de cuerpo mediano.
                    style = MaterialTheme.typography.bodyMedium,
                    // Color principal.
                    color = AppTheme.colors.textPrimary,
                )
            }
            // Foto: tocarla la abre a pantalla completa.
            if (message.type == MessageType.IMAGE && message.imageUrl != null) {
                // Estado que indica si el visor de foto está abierto (sobrevive a rotaciones).
                var showViewer by rememberSaveable { mutableStateOf(false) }
                // Imagen cargada de forma asíncrona con Coil.
                AsyncImage(
                    // URL de la foto.
                    model = message.imageUrl,
                    // Descripción accesible.
                    contentDescription = stringResource(R.string.chat_photo_description),
                    // Recorta la imagen para llenar el espacio.
                    contentScale = ContentScale.Crop,
                    // Color de relleno mientras carga.
                    placeholder = ColorPainter(AppTheme.colors.surfaceTag),
                    modifier = Modifier
                        // Tamaño fijo de la miniatura.
                        .size(width = 220.dp, height = 240.dp)
                        // Esquinas redondeadas.
                        .clip(RoundedCornerShape(12.dp))
                        // Tocarla abre el visor, con etiqueta accesible.
                        .clickable(onClickLabel = stringResource(R.string.photo_viewer_open), role = Role.Image) { showViewer = true },
                )
                // Si el visor está abierto, lo muestra.
                if (showViewer) {
                    // Visor de fotos a pantalla completa con una sola imagen.
                    PhotoViewerDialog(imageUrls = listOf(message.imageUrl), initialPage = 0, onDismiss = { showViewer = false })
                }
                // Si la foto lleva pie de texto, deja un espacio antes.
                if (message.text.isNotBlank()) Spacer(Modifier.height(6.dp))
            }
            // En el automático, [text] es el mensaje opcional que escribió quien solicitó; en una foto, su pie.
            if (message.text.isNotBlank()) {
                // Texto del mensaje.
                Text(text = message.text, style = MaterialTheme.typography.bodyMedium, color = AppTheme.colors.textPrimary)
            }
            // Hora abajo a la derecha, separada del texto; en los míos, un check (enviado) o doble (visto).
            Row(
                modifier = Modifier
                    // Alineada a la derecha dentro de la burbuja.
                    .align(Alignment.End)
                    // Margen superior.
                    .padding(top = 6.dp),
                // Centra verticalmente hora e ícono.
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Hora del mensaje.
                Text(
                    // Hora en formato "HH:mm".
                    text = chatClockText(message.sentAt),
                    // Letra pequeña.
                    fontSize = 11.sp,
                    // Peso medio.
                    fontWeight = FontWeight.Medium,
                    // Color principal semitransparente.
                    color = AppTheme.colors.textPrimary.copy(alpha = 0.65f),
                )
                // Solo en mis mensajes se muestra el check.
                if (message.isMine) {
                    // Espacio entre hora y check.
                    Spacer(Modifier.width(4.dp))
                    // Ícono de estado del mensaje.
                    Icon(
                        // Doble check si fue visto, check simple si solo se envió.
                        imageVector = if (isSeen) Icons.Filled.DoneAll else Icons.Filled.Done,
                        // Descripción accesible "Visto" o "Enviado".
                        contentDescription = stringResource(if (isSeen) R.string.chat_seen else R.string.chat_sent),
                        // Color de enlace si fue visto; si no, semitransparente.
                        tint = if (isSeen) AppTheme.colors.textLink else AppTheme.colors.textPrimary.copy(alpha = 0.65f),
                        // Tamaño del ícono.
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
    }
}

// Previsualización de la conversación con datos de ejemplo en Android Studio.
@Preview(showBackground = true, heightDp = 720)
@Composable
private fun ChatPreview() {
    // Momento actual para calcular fechas de ejemplo.
    val now = System.currentTimeMillis()
    // Duración de un día en milisegundos.
    val day = 24 * 60 * 60 * 1000L
    // Aplica el tema de la aplicación.
    PetAdoptionAppTheme {
        // Dibuja el contenido sin ViewModel.
        ChatContent(
            // Estado de ejemplo.
            uiState = ChatUiState(
                // Sin carga en curso.
                isLoading = false,
                // Alias de la otra persona.
                otherName = "luis_9",
                // Publicación de ejemplo.
                postId = "p1",
                // Título de la publicación.
                postTitle = "Perrito en adopción",
                // Mensajes de ejemplo: un interés automático y varios textos en distintos días.
                messages = listOf(
                    ChatMessageUi("1", false, MessageType.ADOPTION_INTEREST, "", now - 2 * day),
                    ChatMessageUi("2", true, MessageType.TEXT, "¡Hola Luis! Gracias por escribir. Es muy juguetón y sociable.", now - 2 * day + 600_000),
                    ChatMessageUi("3", false, MessageType.TEXT, "¿Podemos vernos mañana para conocerlo?", now - 1_200_000),
                    ChatMessageUi("4", true, MessageType.TEXT, "¡Claro! ¿A las 10 te sirve?", now - 600_000),
                ),
                // El último mensaje ya fue visto.
                seenMessageId = "4",
            ),
            // Acciones vacías para la previsualización.
            actions = ChatActions(),
        )
    }
}
