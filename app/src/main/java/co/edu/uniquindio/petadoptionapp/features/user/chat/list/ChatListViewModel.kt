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

/**
ViewModel de la lista de chats (pestaña Mensajes del usuario). Prepara el estado que dibuja
ChatListScreen: los chats ordenados del más reciente al más antiguo con la otra persona, la
publicación de la que tratan, la vista previa del último mensaje y el número de no leídos.
Estado (ChatListUiState): lista de chats, texto de búsqueda, carga/error y el flujo de eliminación
(chat seleccionado, eliminación en curso y error al eliminar).
Eventos: refresh (recargar), onQueryChange (buscar), onChatLongClick / onDeleteDismiss /
onDeleteConfirm (eliminar un chat al mantenerlo presionado).
Casos de uso: ObserveSessionUseCase (usuario actual), GetMyChatsUseCase (chats), GetUserProfileUseCase
(alias y foto de la otra persona), GetPostDetailUseCase (título de la publicación) y DeleteChatUseCase.
Navegación: la pantalla llama a refresh cada vez que vuelve a verse (por ejemplo, al regresar de una
conversación) y, al tocar un chat, navega a ChatScreen con su id.
**/

// Indica qué tipo de vista previa del último mensaje se muestra; el texto final lo pone la UI con strings.xml.
enum class ChatPreviewKind { TEXT, PHOTO, INTEREST_SENT, INTEREST_RECEIVED }

// Representa un chat de la lista ya preparado para dibujarse en la UI.
data class ChatSummaryUi(
    // Identificador del chat.
    val id: String,
    // Identificador de la otra persona del chat.
    val otherUserId: String,
    // Alias (userName) de la otra persona; vacío si su cuenta fue eliminada.
    val otherName: String,
    // URL de la foto de perfil de la otra persona, si tiene.
    val otherPhotoUrl: String?,
    // Indica si la cuenta de la otra persona fue eliminada.
    val isOtherDeleted: Boolean,
    // Publicación de la que trata el chat; null si no hay o ya no se puede ver.
    val postTitle: String?,
    // Tipo de vista previa del último mensaje (texto, foto o interés de adopción).
    val previewKind: ChatPreviewKind,
    // Contenido del último mensaje.
    val lastMessage: String,
    // El último mensaje es mío: la vista previa empieza con "Tú: ".
    val isLastFromMe: Boolean,
    // Momento (en milisegundos) del último mensaje.
    val lastMessageAt: Long,
    // Cantidad de mensajes no leídos por el usuario actual.
    val unreadCount: Int,
)

// Estado completo de la pantalla de lista de chats.
data class ChatListUiState(
    // Todos los chats del usuario, ya transformados para la UI.
    val chats: List<ChatSummaryUi> = emptyList(),
    // Texto escrito en la barra de búsqueda.
    val query: String = "",
    // Indica si la lista se está cargando.
    val isLoading: Boolean = true,
    // Indica si ocurrió un error al cargar la lista.
    val hasError: Boolean = false,
    // Chat elegido (mantener presionado) para eliminar; null = sin diálogo.
    val chatToDelete: String? = null,
    // Indica si la eliminación del chat está en curso.
    val isDeleting: Boolean = false,
    // Indica si la última eliminación falló.
    val deleteError: Boolean = false,
) {
    // Chats que coinciden con la búsqueda: por la persona (alias) o por la publicación, sin importar mayúsculas.
    val visibleChats: List<ChatSummaryUi>
        // Limpia espacios y una "@" inicial del texto buscado antes de filtrar.
        get() = query.trim().removePrefix("@").let { q ->
            // Si no hay búsqueda se devuelven todos los chats.
            if (q.isEmpty()) chats
            // Si hay búsqueda se filtran por alias de la otra persona o por título de la publicación.
            else chats.filter { it.otherName.contains(q, ignoreCase = true) || it.postTitle?.contains(q, ignoreCase = true) == true }
        }
}

// ViewModel de la lista de chats, inyectado por Hilt con los casos de uso que necesita.
@HiltViewModel
class ChatListViewModel @Inject constructor(
    // Caso de uso que expone la sesión (cuenta) actual.
    private val observeSession: ObserveSessionUseCase,
    // Caso de uso que obtiene los chats del usuario actual.
    private val getMyChats: GetMyChatsUseCase,
    // Caso de uso que obtiene el perfil público de otra persona.
    private val getUserProfile: GetUserProfileUseCase,
    // Caso de uso que obtiene el detalle de una publicación (para su título).
    private val getPostDetail: GetPostDetailUseCase,
    // Caso de uso que elimina un chat (para ambos o solo para mí).
    private val deleteChat: DeleteChatUseCase,
) : ViewModel() {

    // Estado mutable interno de la pantalla.
    private val _uiState = MutableStateFlow(ChatListUiState())
    // Estado de solo lectura que observa la UI.
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()

    // Perfiles ya consultados: al recargar no se vuelven a pedir.
    private val profiles = mutableMapOf<String, OtherProfile>()
    // Títulos de publicaciones ya consultados (null si no se pudieron ver): al recargar no se vuelven a pedir.
    private val postTitles = mutableMapOf<String, String?>()

    // Datos mínimos de la otra persona que se guardan en caché: alias, foto y si la cuenta fue eliminada.
    private data class OtherProfile(val userName: String, val photoUrl: String?, val deleted: Boolean)

    // Recarga la lista de chats del usuario actual.
    fun refresh() {
        // Obtiene la sesión actual; si no hay sesión no hace nada.
        val session = observeSession().value ?: return
        // Lanza la carga en una corrutina ligada al ciclo de vida del ViewModel.
        viewModelScope.launch {
            // Pide los chats del usuario.
            getMyChats()
                // Si la consulta funcionó, transforma cada chat al modelo de UI.
                .onSuccess { chats ->
                    // Convierte los chats descartando los que no tienen otra persona válida.
                    val items = chats.mapNotNull { it.toUi(session) }
                    // Publica la lista y quita la carga y el error.
                    _uiState.update { it.copy(chats = items, isLoading = false, hasError = false) }
                }
                // Si falló, quita la carga y marca el error.
                .onFailure { _uiState.update { it.copy(isLoading = false, hasError = true) } }
        }
    }

    // Actualiza el texto de búsqueda; el filtrado lo hace visibleChats.
    fun onQueryChange(value: String) = _uiState.update { it.copy(query = value) }

    // --- Eliminar chat (mantener presionado) ---

    // Guarda el chat mantenido presionado para abrir el diálogo de eliminación y limpia el error previo.
    fun onChatLongClick(chatId: String) = _uiState.update { it.copy(chatToDelete = chatId, deleteError = false) }

    // Cierra el diálogo de eliminación, salvo que la eliminación esté en curso.
    fun onDeleteDismiss() = _uiState.update { if (it.isDeleting) it else it.copy(chatToDelete = null) }

    // Confirma la eliminación del chat seleccionado, para ambos (forEveryone = true) o solo para mí.
    fun onDeleteConfirm(forEveryone: Boolean) {
        // Toma el chat seleccionado; si no hay ninguno no hace nada.
        val chatId = _uiState.value.chatToDelete ?: return
        // Evita lanzar dos eliminaciones a la vez.
        if (_uiState.value.isDeleting) return
        // Marca que la eliminación está en curso.
        _uiState.update { it.copy(isDeleting = true) }
        // Ejecuta la eliminación en segundo plano.
        viewModelScope.launch {
            // Llama al caso de uso para eliminar el chat.
            deleteChat(chatId, forEveryone)
                // Si se eliminó correctamente:
                .onSuccess {
                    // Sale de la lista al instante; la recarga confirma el resto (no leídos, orden).
                    _uiState.update { s -> s.copy(isDeleting = false, chatToDelete = null, chats = s.chats.filterNot { it.id == chatId }) }
                    // Recarga la lista desde el servidor.
                    refresh()
                }
                // Si falló, cierra el diálogo y marca el error de eliminación.
                .onFailure { _uiState.update { it.copy(isDeleting = false, chatToDelete = null, deleteError = true) } }
        }
    }

    // Convierte un chat del dominio en un ChatSummaryUi; devuelve null si no se encuentra la otra persona.
    private suspend fun Chat.toUi(session: Account): ChatSummaryUi? {
        // Identificador del usuario actual.
        val myId = session.id
        // Identificador del otro participante; si no existe se descarta el chat.
        val otherId = otherParticipantId(myId) ?: return null
        // Si el perfil no se pudo leer, se muestra vacío y se reintenta en la próxima recarga.
        val other = profiles[otherId] ?: run {
            // Consulta el perfil de la otra persona.
            val result = getUserProfile(otherId, myId)
            // Decide qué perfil guardar según el resultado.
            when {
                // Si se obtuvo, guarda su alias y foto.
                result.isSuccess -> result.getOrThrow().let { OtherProfile(it.userName, it.photoUrl, deleted = false) }
                // Si la cuenta fue eliminada, guarda un perfil vacío marcado como eliminado.
                result.exceptionOrNull() === UserException.UserDeleted -> OtherProfile("", null, deleted = true)
                // Ante cualquier otro error no se guarda nada en caché.
                else -> null
            // Si hay perfil válido, lo almacena en la caché.
            }?.also { profiles[otherId] = it }
        // Si no hubo perfil, usa uno vacío temporal.
        } ?: OtherProfile("", null, deleted = false)
        // Obtiene el título de la publicación asociada, usando la caché si ya se consultó.
        val title = postId?.let { id ->
            // Si ya está en caché lo reutiliza; si no, lo consulta y lo guarda (aunque sea null).
            if (id in postTitles) postTitles[id] else getPostDetail(session, id).getOrNull()?.title.also { postTitles[id] = it }
        }
        // Indica si el último mensaje lo envió el usuario actual.
        val fromMe = lastSenderId == myId
        // Construye el modelo de UI del chat.
        return ChatSummaryUi(
            // Identificador del chat.
            id = id,
            // Identificador de la otra persona.
            otherUserId = otherId,
            // Alias de la otra persona.
            otherName = other.userName,
            // Foto de la otra persona.
            otherPhotoUrl = other.photoUrl,
            // Si la cuenta de la otra persona fue eliminada.
            isOtherDeleted = other.deleted,
            // Título de la publicación asociada.
            postTitle = title,
            // Determina el tipo de vista previa según el tipo y el autor del último mensaje.
            previewKind = when {
                // Si el último mensaje es una imagen, se muestra como foto.
                lastMessageType == MessageType.IMAGE -> ChatPreviewKind.PHOTO
                // Si no es un interés de adopción, se muestra como texto.
                lastMessageType != MessageType.ADOPTION_INTEREST -> ChatPreviewKind.TEXT
                // Si es un interés de adopción enviado por mí.
                fromMe -> ChatPreviewKind.INTEREST_SENT
                // Si es un interés de adopción recibido.
                else -> ChatPreviewKind.INTEREST_RECEIVED
            },
            // Contenido del último mensaje.
            lastMessage = lastMessage,
            // Si el último mensaje es mío.
            isLastFromMe = fromMe,
            // Momento del último mensaje.
            lastMessageAt = lastMessageAt,
            // Mensajes no leídos por el usuario actual.
            unreadCount = unreadFor(myId),
        )
    }
}
