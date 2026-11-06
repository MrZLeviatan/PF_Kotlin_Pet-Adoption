package co.edu.uniquindio.petadoptionapp.features.user.chat.conversation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.petadoptionapp.domain.error.UserException
import co.edu.uniquindio.petadoptionapp.domain.model.messaging.Chat
import co.edu.uniquindio.petadoptionapp.domain.model.messaging.Message
import co.edu.uniquindio.petadoptionapp.domain.model.messaging.MessageType
import co.edu.uniquindio.petadoptionapp.domain.usecase.chat.DeleteChatUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.chat.GetOlderMessagesUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.chat.MarkChatReadUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.chat.ObserveMessagesUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.chat.RequireChatMemberUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.chat.SendMessageUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.GetPostDetailUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
ViewModel de una conversación entre dos usuarios (tipo WhatsApp). Recibe los mensajes en vivo,
marca el chat como leído cada vez que llega un mensaje nuevo, calcula el "Visto" de mis mensajes
y permite cargar mensajes anteriores por páginas, enviar texto o fotos y eliminar el chat.
Estado (ChatUiState): carga / no encontrado, datos de la otra persona y de la publicación, lista de
mensajes, información de "Visto", paginación, texto del campo, envío y flujo de eliminación.
Eventos de UI: onTextChange, onSend, onImagePicked, onLoadOlder, onSendErrorShown, onDeleteClick,
onDeleteDismiss y onDeleteConfirm. Evento de una sola vez hacia la UI: ChatEvent.Deleted.
Casos de uso: RequireChatMemberUseCase, ObserveMessagesUseCase, GetOlderMessagesUseCase,
SendMessageUseCase, MarkChatReadUseCase, GetUserProfileUseCase, GetPostDetailUseCase y DeleteChatUseCase.
Navegación: el chatId llega por argumento de la ruta MainRoutes.Chat (SavedStateHandle); al emitir
ChatEvent.Deleted la pantalla vuelve atrás.
**/

// Un mensaje listo para dibujar; la hora y los textos automáticos los pone la UI.
data class ChatMessageUi(
    // Identificador del mensaje.
    val id: String,
    // Indica si el mensaje lo envió el usuario actual (se dibuja a la derecha).
    val isMine: Boolean,
    // ADOPTION_INTEREST = mensaje automático de "Me interesa adoptar".
    val type: MessageType,
    // Texto del mensaje.
    val text: String,
    // Momento de envío en milisegundos.
    val sentAt: Long,
    // Foto del mensaje (tipo IMAGE).
    val imageUrl: String? = null,
)

// Estado completo de la pantalla de conversación.
data class ChatUiState(
    // Indica si se está validando el acceso al chat.
    val isLoading: Boolean = true,
    // Indica que el chat no existe o el usuario no pertenece a él.
    val notFound: Boolean = false,
    // Identificador de la otra persona.
    val otherUserId: String = "",
    // Alias de la otra persona (como en la lista de chats).
    val otherName: String = "",
    // Foto de perfil de la otra persona.
    val otherPhotoUrl: String? = null,
    // Su cuenta fue eliminada: el encabezado lo dice y no se abre su perfil.
    val isOtherDeleted: Boolean = false,
    // Publicación que originó el chat.
    val postId: String? = null,
    // Título de la publicación; es null si ya no se puede ver.
    val postTitle: String? = null,
    // Mensajes ordenados del más antiguo al más nuevo.
    val messages: List<ChatMessageUi> = emptyList(),
    // Id de mi último mensaje si la otra persona ya lo vio ("Visto").
    val seenMessageId: String? = null,
    // Hasta cuándo leyó la otra persona: mis mensajes enviados antes llevan el doble check de "visto".
    val seenUntil: Long = 0L,
    // Indica si puede haber mensajes anteriores por cargar.
    val canLoadOlder: Boolean = false,
    // Indica si se están cargando mensajes anteriores.
    val isLoadingOlder: Boolean = false,
    // Texto escrito en el campo de mensaje.
    val text: String = "",
    // Indica si se está enviando un mensaje.
    val isSending: Boolean = false,
    // Indica si falló el último envío.
    val sendError: Boolean = false,
    // Indica si se muestra el diálogo para eliminar el chat (para mí o para ambos).
    val showDeleteDialog: Boolean = false,
    // Indica si la eliminación está en curso.
    val isDeleting: Boolean = false,
    // Indica si falló la eliminación.
    val deleteError: Boolean = false,
)

// Eventos de una sola vez que el ViewModel envía a la pantalla.
sealed interface ChatEvent {
    // El chat se eliminó: la navegación vuelve atrás.
    data object Deleted : ChatEvent
}

// ViewModel de la conversación, inyectado por Hilt con sus casos de uso.
@HiltViewModel
class ChatViewModel @Inject constructor(
    // Argumentos de navegación de la ruta (contiene el chatId).
    savedStateHandle: SavedStateHandle,
    // Verifica que el usuario actual pertenezca al chat y devuelve chat y cuenta.
    private val requireChatMember: RequireChatMemberUseCase,
    // Expone el flujo en vivo de los mensajes más recientes.
    private val observeMessages: ObserveMessagesUseCase,
    // Obtiene una página de mensajes anteriores a una fecha.
    private val getOlderMessages: GetOlderMessagesUseCase,
    // Envía un mensaje de texto o de imagen.
    private val sendMessage: SendMessageUseCase,
    // Marca el chat como leído por el usuario actual.
    private val markChatRead: MarkChatReadUseCase,
    // Obtiene el perfil público de la otra persona.
    private val getUserProfile: GetUserProfileUseCase,
    // Obtiene el detalle de la publicación asociada.
    private val getPostDetail: GetPostDetailUseCase,
    // Elimina el chat para mí o para ambos.
    private val deleteChat: DeleteChatUseCase,
) : ViewModel() {

    // Identificador del chat leído de los argumentos de la ruta (vacío si no llegó).
    private val chatId: String = savedStateHandle["chatId"] ?: ""

    // Estado mutable interno de la pantalla.
    private val _uiState = MutableStateFlow(ChatUiState())
    // Estado de solo lectura que observa la UI.
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    // Canal con búfer para eventos de una sola vez (no se pierden si la UI aún no escucha).
    private val _events = Channel<ChatEvent>(Channel.BUFFERED)
    // Flujo de eventos que consume la pantalla.
    val events = _events.receiveAsFlow()

    // Mi id (de la sesión, vía la membresía del chat).
    private var myId: String = ""

    // Mensajes anteriores ya cargados con "Ver mensajes anteriores".
    private var older: List<Message> = emptyList()
    // Mensajes recibidos por el flujo en vivo; se muestran después de los anteriores.
    private var live: List<Message> = emptyList()

    // Al crearse el ViewModel comienza la carga del chat.
    init { load() }

    // Carga el chat: valida membresía, pide encabezado y empieza a escuchar mensajes en vivo.
    private fun load() = viewModelScope.launch {
        // Verifica que el usuario pertenezca al chat; si no, marca "no encontrado" y termina.
        val membership = requireChatMember(chatId).getOrElse {
            // Quita la carga y marca el chat como no encontrado.
            _uiState.update { it.copy(isLoading = false, notFound = true) }
            // Sale de la corrutina.
            return@launch
        }
        // Guarda el id del usuario actual.
        myId = membership.account.id
        // Datos del chat.
        val chat = membership.chat
        // Id de la otra persona (vacío si no se encuentra).
        val otherId = chat.otherParticipantId(myId).orEmpty()
        // Quita la carga y publica el id de la otra persona y de la publicación.
        _uiState.update { it.copy(isLoading = false, otherUserId = otherId, postId = chat.postId) }

        // Encabezado: el alias de la otra persona (o que eliminó su cuenta), en una corrutina hija.
        launch {
            // Consulta el perfil de la otra persona.
            getUserProfile(otherId, myId)
                // Si existe, publica su alias y su foto.
                .onSuccess { profile -> _uiState.update { it.copy(otherName = profile.userName, otherPhotoUrl = profile.photoUrl) } }
                // Si su cuenta fue eliminada, lo marca en el estado.
                .onFailure { error -> if (error === UserException.UserDeleted) _uiState.update { it.copy(isOtherDeleted = true) } }
        }
        // Si el chat viene de una publicación, carga su título.
        chat.postId?.let { postId ->
            // Corrutina hija para no bloquear la escucha de mensajes.
            launch {
                // Consulta la publicación y publica su título si se puede ver.
                getPostDetail(membership.account, postId).onSuccess { post -> _uiState.update { it.copy(postTitle = post.title) } }
            }
        }

        // Mensajes en vivo: obtiene el flujo; si falla, marca "no encontrado" y termina.
        val flow = observeMessages(chatId).getOrElse {
            // Marca el chat como no encontrado.
            _uiState.update { it.copy(notFound = true) }
            // Sale de la corrutina.
            return@launch
        }
        // Cada vez que llega una nueva lista, se marca leído y se actualiza el "Visto".
        flow.collect { messages ->
            // Guarda los mensajes en vivo.
            live = messages
            // Une y publica todos los mensajes.
            publishMessages()
            // Si el flujo trae el máximo, puede haber anteriores (mientras no se hayan pedido ya).
            if (older.isEmpty()) _uiState.update { it.copy(canLoadOlder = messages.size >= ObserveMessagesUseCase.LIVE_SIZE) }
            // Marca el chat como leído por el usuario actual.
            markChatRead(chatId)
            // Vuelve a leer el chat para conocer hasta dónde leyó la otra persona y actualizar el "Visto".
            requireChatMember(chatId).onSuccess { updateSeen(it.chat) }
        }
    }

    // Actualiza el texto del campo de mensaje y limpia el error de envío.
    fun onTextChange(value: String) = _uiState.update { it.copy(text = value, sendError = false) }

    // Envía el texto escrito como mensaje.
    fun onSend() {
        // Toma una copia del estado actual.
        val state = _uiState.value
        // No envía si el texto está vacío o ya hay un envío en curso.
        if (state.text.isBlank() || state.isSending) return
        // Marca el envío en curso y limpia el error.
        _uiState.update { it.copy(isSending = true, sendError = false) }
        // Ejecuta el envío en segundo plano.
        viewModelScope.launch {
            // El mensaje aparece solo por el flujo en vivo; aquí solo se limpia el campo.
            sendMessage(chatId, state.text)
                // Si se envió, termina el envío y vacía el campo.
                .onSuccess { _uiState.update { it.copy(isSending = false, text = "") } }
                // Si falló, termina el envío y marca el error (el texto se conserva).
                .onFailure { _uiState.update { it.copy(isSending = false, sendError = true) } }
        }
    }

    // Envía una foto (ya guardada en la app). Como en el texto, aparece sola por el flujo en vivo.
    fun onImagePicked(url: String) {
        // No envía si ya hay un envío en curso.
        if (_uiState.value.isSending) return
        // Marca el envío en curso y limpia el error.
        _uiState.update { it.copy(isSending = true, sendError = false) }
        // Ejecuta el envío en segundo plano.
        viewModelScope.launch {
            // Envía un mensaje sin texto con la URL de la imagen.
            sendMessage(chatId, "", imageUrl = url)
                // Si se envió, termina el envío.
                .onSuccess { _uiState.update { it.copy(isSending = false) } }
                // Si falló, termina el envío y marca el error.
                .onFailure { _uiState.update { it.copy(isSending = false, sendError = true) } }
        }
    }

    // Carga la página de mensajes anteriores al más antiguo que ya se tiene.
    fun onLoadOlder() {
        // Toma el mensaje más antiguo conocido; si no hay mensajes no hace nada.
        val oldest = (older.firstOrNull() ?: live.firstOrNull()) ?: return
        // Evita cargas simultáneas.
        if (_uiState.value.isLoadingOlder) return
        // Marca la carga de anteriores en curso.
        _uiState.update { it.copy(isLoadingOlder = true) }
        // Ejecuta la consulta en segundo plano.
        viewModelScope.launch {
            // Pide los mensajes enviados antes del más antiguo.
            getOlderMessages(chatId, oldest.sentAt)
                // Si funcionó:
                .onSuccess { page ->
                    // Antepone la nueva página a los anteriores ya cargados.
                    older = page + older
                    // Publica la lista combinada.
                    publishMessages()
                    // Termina la carga; si la página vino llena puede haber más anteriores.
                    _uiState.update { it.copy(isLoadingOlder = false, canLoadOlder = page.size >= GetOlderMessagesUseCase.PAGE_SIZE) }
                }
                // Si falló, solo termina la carga.
                .onFailure { _uiState.update { it.copy(isLoadingOlder = false) } }
        }
    }

    // Limpia el error de envío una vez que la UI ya lo mostró.
    fun onSendErrorShown() = _uiState.update { it.copy(sendError = false) }

    // --- Eliminar chat ---

    // Abre el diálogo de eliminación y limpia el error previo.
    fun onDeleteClick() = _uiState.update { it.copy(showDeleteDialog = true, deleteError = false) }

    // Cierra el diálogo de eliminación, salvo que la eliminación esté en curso.
    fun onDeleteDismiss() = _uiState.update { if (it.isDeleting) it else it.copy(showDeleteDialog = false) }

    // Confirma la eliminación del chat, para ambos (forEveryone = true) o solo para mí.
    fun onDeleteConfirm(forEveryone: Boolean) {
        // Evita lanzar dos eliminaciones a la vez.
        if (_uiState.value.isDeleting) return
        // Marca la eliminación en curso.
        _uiState.update { it.copy(isDeleting = true) }
        // Ejecuta la eliminación en segundo plano.
        viewModelScope.launch {
            // Llama al caso de uso para eliminar el chat.
            deleteChat(chatId, forEveryone)
                // Si se eliminó:
                .onSuccess {
                    // Termina la eliminación y cierra el diálogo.
                    _uiState.update { it.copy(isDeleting = false, showDeleteDialog = false) }
                    // Avisa a la pantalla para que vuelva atrás.
                    _events.send(ChatEvent.Deleted)
                }
                // Si falló, cierra el diálogo y marca el error.
                .onFailure { _uiState.update { it.copy(isDeleting = false, showDeleteDialog = false, deleteError = true) } }
        }
    }

    // Une los anteriores con los en vivo (sin repetir) y los pasa a la UI.
    private fun publishMessages() {
        // Combina ambas listas, quita duplicados por id y ordena por fecha de envío.
        val all = (older + live).distinctBy { it.id }.sortedBy { it.sentAt }
        // Publica la lista convertida al modelo de UI.
        _uiState.update { state ->
            // Convierte cada mensaje marcando si es mío.
            state.copy(messages = all.map { ChatMessageUi(it.id, it.senderId == myId, it.type, it.text, it.sentAt, it.imageUrl) })
        }
        // Si ya se conoce el chat, recalcula el "Visto" con los mensajes nuevos.
        lastChat?.let(::updateSeen)
    }

    // Último estado conocido del chat (para recalcular el "Visto" al publicar mensajes).
    private var lastChat: Chat? = null

    // "Visto" va bajo mi último mensaje si la otra persona abrió el chat después de enviarlo.
    private fun updateSeen(chat: Chat) {
        // Guarda el chat como último estado conocido.
        lastChat = chat
        // Id de la otra persona; si no existe no hace nada.
        val otherId = chat.otherParticipantId(myId) ?: return
        // Busca mi último mensaje enviado.
        val lastMine = (older + live).lastOrNull { it.senderId == myId }
        // Si la otra persona ya lo vio, toma su id; si no, null.
        val seen = lastMine?.takeIf { chat.isMessageSeenBy(otherId, it) }?.id
        // Publica el mensaje visto y hasta cuándo leyó la otra persona.
        _uiState.update { it.copy(seenMessageId = seen, seenUntil = chat.lastReadAt[otherId] ?: 0L) }
    }
}
