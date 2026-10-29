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

/** Un mensaje listo para dibujar; la hora y los textos automáticos los pone la UI. */
data class ChatMessageUi(
    val id: String,
    val isMine: Boolean,
    // ADOPTION_INTEREST = mensaje automático de "Me interesa adoptar".
    val type: MessageType,
    val text: String,
    val sentAt: Long,
    // Foto del mensaje (tipo IMAGE).
    val imageUrl: String? = null,
)

data class ChatUiState(
    val isLoading: Boolean = true,
    val notFound: Boolean = false,
    val otherUserId: String = "",
    // Alias de la otra persona (como en la lista de chats).
    val otherName: String = "",
    val otherPhotoUrl: String? = null,
    // Su cuenta fue eliminada: el encabezado lo dice y no se abre su perfil.
    val isOtherDeleted: Boolean = false,
    // Publicación que originó el chat; el título es null si ya no se puede ver.
    val postId: String? = null,
    val postTitle: String? = null,
    // Del más antiguo al más nuevo.
    val messages: List<ChatMessageUi> = emptyList(),
    // Id de mi último mensaje si la otra persona ya lo vio ("Visto").
    val seenMessageId: String? = null,
    // Hasta cuándo leyó la otra persona: mis mensajes enviados antes llevan el doble check de "visto".
    val seenUntil: Long = 0L,
    val canLoadOlder: Boolean = false,
    val isLoadingOlder: Boolean = false,
    val text: String = "",
    val isSending: Boolean = false,
    val sendError: Boolean = false,
    // Eliminar chat (para mí o para ambos).
    val showDeleteDialog: Boolean = false,
    val isDeleting: Boolean = false,
    val deleteError: Boolean = false,
)

sealed interface ChatEvent {
    // El chat se eliminó: la navegación vuelve atrás.
    data object Deleted : ChatEvent
}

/**
 Una conversación (tipo WhatsApp). Recibe los mensajes en vivo, marca el chat como leído al llegar
 cada mensaje nuevo y permite cargar los anteriores. El chatId llega de la ruta MainRoutes.Chat.
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val requireChatMember: RequireChatMemberUseCase,
    private val observeMessages: ObserveMessagesUseCase,
    private val getOlderMessages: GetOlderMessagesUseCase,
    private val sendMessage: SendMessageUseCase,
    private val markChatRead: MarkChatReadUseCase,
    private val getUserProfile: GetUserProfileUseCase,
    private val getPostDetail: GetPostDetailUseCase,
    private val deleteChat: DeleteChatUseCase,
) : ViewModel() {

    private val chatId: String = savedStateHandle["chatId"] ?: ""

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _events = Channel<ChatEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // Mi id (de la sesión, vía la membresía del chat).
    private var myId: String = ""

    // Mensajes anteriores ya cargados con "Ver mensajes anteriores"; los en vivo van después.
    private var older: List<Message> = emptyList()
    private var live: List<Message> = emptyList()

    init { load() }

    private fun load() = viewModelScope.launch {
        val membership = requireChatMember(chatId).getOrElse {
            _uiState.update { it.copy(isLoading = false, notFound = true) }
            return@launch
        }
        myId = membership.account.id
        val chat = membership.chat
        val otherId = chat.otherParticipantId(myId).orEmpty()
        _uiState.update { it.copy(isLoading = false, otherUserId = otherId, postId = chat.postId) }

        // Encabezado: el alias de la otra persona (o que eliminó su cuenta) y la publicación.
        launch {
            getUserProfile(otherId, myId)
                .onSuccess { profile -> _uiState.update { it.copy(otherName = profile.userName, otherPhotoUrl = profile.photoUrl) } }
                .onFailure { error -> if (error === UserException.UserDeleted) _uiState.update { it.copy(isOtherDeleted = true) } }
        }
        chat.postId?.let { postId ->
            launch {
                getPostDetail(membership.account, postId).onSuccess { post -> _uiState.update { it.copy(postTitle = post.title) } }
            }
        }

        // Mensajes en vivo: cada vez que llega uno, se marca leído y se actualiza el "Visto".
        val flow = observeMessages(chatId).getOrElse {
            _uiState.update { it.copy(notFound = true) }
            return@launch
        }
        flow.collect { messages ->
            live = messages
            publishMessages()
            // Si el flujo trae el máximo, puede haber anteriores (mientras no se hayan pedido ya).
            if (older.isEmpty()) _uiState.update { it.copy(canLoadOlder = messages.size >= ObserveMessagesUseCase.LIVE_SIZE) }
            markChatRead(chatId)
            requireChatMember(chatId).onSuccess { updateSeen(it.chat) }
        }
    }

    fun onTextChange(value: String) = _uiState.update { it.copy(text = value, sendError = false) }

    fun onSend() {
        val state = _uiState.value
        if (state.text.isBlank() || state.isSending) return
        _uiState.update { it.copy(isSending = true, sendError = false) }
        viewModelScope.launch {
            // El mensaje aparece solo por el flujo en vivo; aquí solo se limpia el campo.
            sendMessage(chatId, state.text)
                .onSuccess { _uiState.update { it.copy(isSending = false, text = "") } }
                .onFailure { _uiState.update { it.copy(isSending = false, sendError = true) } }
        }
    }

    /** Envía una foto (ya guardada en la app). Como en el texto, aparece sola por el flujo en vivo. */
    fun onImagePicked(url: String) {
        if (_uiState.value.isSending) return
        _uiState.update { it.copy(isSending = true, sendError = false) }
        viewModelScope.launch {
            sendMessage(chatId, "", imageUrl = url)
                .onSuccess { _uiState.update { it.copy(isSending = false) } }
                .onFailure { _uiState.update { it.copy(isSending = false, sendError = true) } }
        }
    }

    fun onLoadOlder() {
        val oldest = (older.firstOrNull() ?: live.firstOrNull()) ?: return
        if (_uiState.value.isLoadingOlder) return
        _uiState.update { it.copy(isLoadingOlder = true) }
        viewModelScope.launch {
            getOlderMessages(chatId, oldest.sentAt)
                .onSuccess { page ->
                    older = page + older
                    publishMessages()
                    _uiState.update { it.copy(isLoadingOlder = false, canLoadOlder = page.size >= GetOlderMessagesUseCase.PAGE_SIZE) }
                }
                .onFailure { _uiState.update { it.copy(isLoadingOlder = false) } }
        }
    }

    fun onSendErrorShown() = _uiState.update { it.copy(sendError = false) }

    // --- Eliminar chat ---

    fun onDeleteClick() = _uiState.update { it.copy(showDeleteDialog = true, deleteError = false) }

    fun onDeleteDismiss() = _uiState.update { if (it.isDeleting) it else it.copy(showDeleteDialog = false) }

    fun onDeleteConfirm(forEveryone: Boolean) {
        if (_uiState.value.isDeleting) return
        _uiState.update { it.copy(isDeleting = true) }
        viewModelScope.launch {
            deleteChat(chatId, forEveryone)
                .onSuccess {
                    _uiState.update { it.copy(isDeleting = false, showDeleteDialog = false) }
                    _events.send(ChatEvent.Deleted)
                }
                .onFailure { _uiState.update { it.copy(isDeleting = false, showDeleteDialog = false, deleteError = true) } }
        }
    }

    // Une los anteriores con los en vivo (sin repetir) y los pasa a la UI.
    private fun publishMessages() {
        val all = (older + live).distinctBy { it.id }.sortedBy { it.sentAt }
        _uiState.update { state ->
            state.copy(messages = all.map { ChatMessageUi(it.id, it.senderId == myId, it.type, it.text, it.sentAt, it.imageUrl) })
        }
        lastChat?.let(::updateSeen)
    }

    private var lastChat: Chat? = null

    // "Visto" va bajo mi último mensaje si la otra persona abrió el chat después de enviarlo.
    private fun updateSeen(chat: Chat) {
        lastChat = chat
        val otherId = chat.otherParticipantId(myId) ?: return
        val lastMine = (older + live).lastOrNull { it.senderId == myId }
        val seen = lastMine?.takeIf { chat.isMessageSeenBy(otherId, it) }?.id
        _uiState.update { it.copy(seenMessageId = seen, seenUntil = chat.lastReadAt[otherId] ?: 0L) }
    }
}
