package co.edu.uniquindio.petadoptionapp.features.user.chat.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.petadoptionapp.domain.error.UserException
import co.edu.uniquindio.petadoptionapp.domain.model.messaging.Chat
import co.edu.uniquindio.petadoptionapp.domain.model.users.Account
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.GetPostDetailUseCase
import co.edu.uniquindio.petadoptionapp.domain.model.messaging.MessageType
import co.edu.uniquindio.petadoptionapp.domain.usecase.auth.ObserveSessionUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.chat.DeleteChatUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.chat.GetMyChatsUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.GetUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Qué muestra la vista previa del último mensaje; el texto lo pone la UI con strings.xml.
enum class ChatPreviewKind { TEXT, INTEREST_SENT, INTEREST_RECEIVED }

/** Un chat de la lista, listo para dibujar. */
data class ChatSummaryUi(
    val id: String,
    val otherUserId: String,
    // Alias (userName) de la otra persona; vacío si su cuenta fue eliminada.
    val otherName: String,
    val otherPhotoUrl: String?,
    val isOtherDeleted: Boolean,
    // Publicación de la que trata el chat; null si no hay o ya no se puede ver.
    val postTitle: String?,
    val previewKind: ChatPreviewKind,
    val lastMessage: String,
    // El último mensaje es mío: la vista previa empieza con "Tú: ".
    val isLastFromMe: Boolean,
    val lastMessageAt: Long,
    val unreadCount: Int,
)

data class ChatListUiState(
    val chats: List<ChatSummaryUi> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
    // Chat elegido (mantener presionado) para eliminar; null = sin diálogo.
    val chatToDelete: String? = null,
    val isDeleting: Boolean = false,
    val deleteError: Boolean = false,
) {
    /** Chats que coinciden con la búsqueda: por la persona (alias) o por la publicación, sin importar mayúsculas. */
    val visibleChats: List<ChatSummaryUi>
        get() = query.trim().removePrefix("@").let { q ->
            if (q.isEmpty()) chats
            else chats.filter { it.otherName.contains(q, ignoreCase = true) || it.postTitle?.contains(q, ignoreCase = true) == true }
        }
}

/**
 Lista de chats (pestaña Mensajes): del más reciente al más antiguo, con el último mensaje y los no leídos.
 Se recarga cada vez que la pantalla vuelve a verse (por ejemplo, al salir de una conversación).
 */
@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val observeSession: ObserveSessionUseCase,
    private val getMyChats: GetMyChatsUseCase,
    private val getUserProfile: GetUserProfileUseCase,
    private val getPostDetail: GetPostDetailUseCase,
    private val deleteChat: DeleteChatUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatListUiState())
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()

    // Perfiles y títulos ya consultados: al recargar no se vuelven a pedir.
    private val profiles = mutableMapOf<String, OtherProfile>()
    private val postTitles = mutableMapOf<String, String?>()

    private data class OtherProfile(val userName: String, val photoUrl: String?, val deleted: Boolean)

    fun refresh() {
        val session = observeSession().value ?: return
        viewModelScope.launch {
            getMyChats()
                .onSuccess { chats ->
                    val items = chats.mapNotNull { it.toUi(session) }
                    _uiState.update { it.copy(chats = items, isLoading = false, hasError = false) }
                }
                .onFailure { _uiState.update { it.copy(isLoading = false, hasError = true) } }
        }
    }

    fun onQueryChange(value: String) = _uiState.update { it.copy(query = value) }

    // --- Eliminar chat (mantener presionado) ---

    fun onChatLongClick(chatId: String) = _uiState.update { it.copy(chatToDelete = chatId, deleteError = false) }

    fun onDeleteDismiss() = _uiState.update { if (it.isDeleting) it else it.copy(chatToDelete = null) }

    fun onDeleteConfirm(forEveryone: Boolean) {
        val chatId = _uiState.value.chatToDelete ?: return
        if (_uiState.value.isDeleting) return
        _uiState.update { it.copy(isDeleting = true) }
        viewModelScope.launch {
            deleteChat(chatId, forEveryone)
                .onSuccess {
                    // Sale de la lista al instante; la recarga confirma el resto (no leídos, orden).
                    _uiState.update { s -> s.copy(isDeleting = false, chatToDelete = null, chats = s.chats.filterNot { it.id == chatId }) }
                    refresh()
                }
                .onFailure { _uiState.update { it.copy(isDeleting = false, chatToDelete = null, deleteError = true) } }
        }
    }

    private suspend fun Chat.toUi(session: Account): ChatSummaryUi? {
        val myId = session.id
        val otherId = otherParticipantId(myId) ?: return null
        // Si el perfil no se pudo leer, se muestra vacío y se reintenta en la próxima recarga.
        val other = profiles[otherId] ?: run {
            val result = getUserProfile(otherId, myId)
            when {
                result.isSuccess -> result.getOrThrow().let { OtherProfile(it.userName, it.photoUrl, deleted = false) }
                result.exceptionOrNull() === UserException.UserDeleted -> OtherProfile("", null, deleted = true)
                else -> null
            }?.also { profiles[otherId] = it }
        } ?: OtherProfile("", null, deleted = false)
        val title = postId?.let { id ->
            if (id in postTitles) postTitles[id] else getPostDetail(session, id).getOrNull()?.title.also { postTitles[id] = it }
        }
        val fromMe = lastSenderId == myId
        return ChatSummaryUi(
            id = id,
            otherUserId = otherId,
            otherName = other.userName,
            otherPhotoUrl = other.photoUrl,
            isOtherDeleted = other.deleted,
            postTitle = title,
            previewKind = when {
                lastMessageType != MessageType.ADOPTION_INTEREST -> ChatPreviewKind.TEXT
                fromMe -> ChatPreviewKind.INTEREST_SENT
                else -> ChatPreviewKind.INTEREST_RECEIVED
            },
            lastMessage = lastMessage,
            isLastFromMe = fromMe,
            lastMessageAt = lastMessageAt,
            unreadCount = unreadFor(myId),
        )
    }
}
