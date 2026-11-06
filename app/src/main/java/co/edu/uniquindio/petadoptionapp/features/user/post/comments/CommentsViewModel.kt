package co.edu.uniquindio.petadoptionapp.features.user.post.comments

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.petadoptionapp.domain.model.comment.Comment
import co.edu.uniquindio.petadoptionapp.domain.usecase.auth.ObserveSessionUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.comment.CreateCommentUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.comment.GetCommentsUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.comment.GetLikedCommentsUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.comment.ToggleCommentLikeUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.GetPostDetailUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.ObserveCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
ViewModel de la pantalla de comentarios de una publicación (CommentsScreen).
Recibe el `postId` (y opcionalmente `replyToCommentId`) desde la ruta de navegación
MainRoutes.PostComments mediante SavedStateHandle.
Su estado (CommentsUiState) contiene el título de la publicación, los comentarios principales
paginados, las respuestas de cada hilo abierto, el texto que se está escribiendo, a quién se
responde y las banderas de carga, envío y error.
Eventos que expone: cargar más, abrir/cerrar respuestas, responder, cancelar respuesta,
cambiar texto, enviar y dar "me gusta".
Casos de uso que utiliza: ObserveSessionUseCase, ObserveCurrentUserUseCase, GetPostDetailUseCase,
GetCommentsUseCase, GetLikedCommentsUseCase, ToggleCommentLikeUseCase y CreateCommentUseCase.
**/

/**
Estado inmutable de la pantalla de comentarios. La UI lo observa como StateFlow y se
redibuja cada vez que el ViewModel publica una copia nueva.
**/
data class CommentsUiState(
    // Título de la publicación que se muestra en la barra superior.
    val postTitle: String = "",
    // Total de comentarios de la publicación (según el contador de la publicación).
    val totalComments: Int = 0,
    // Comentarios principales (más recientes primero).
    val comments: List<CommentUi> = emptyList(),
    // Respuestas de cada hilo abierto, indexadas por el id del comentario padre.
    val replies: Map<String, List<CommentUi>> = emptyMap(),
    // Conjunto de ids de comentarios cuyo hilo de respuestas está desplegado.
    val expanded: Set<String> = emptySet(),
    // Indica si quedan más páginas de comentarios principales por cargar.
    val canLoadMore: Boolean = false,
    // Indica si la pantalla está cargando los datos iniciales.
    val isLoading: Boolean = true,
    // Indica si ocurrió un error al cargar la publicación o los comentarios.
    val hasError: Boolean = false,
    // Solo las publicaciones visibles al público aceptan comentarios (regla del caso de uso).
    val canComment: Boolean = false,
    // Texto actual del campo para escribir un comentario.
    val text: String = "",
    // Comentario al que se está respondiendo; null si es un comentario nuevo principal.
    val replyingTo: CommentUi? = null,
    // Indica si se está enviando un comentario en este momento.
    val isSending: Boolean = false,
    // Indica si falló el último envío de comentario.
    val sendError: Boolean = false,
    // Nombre de usuario de quien está viendo, para el avatar del campo de escritura.
    val viewerName: String = "",
    // URL de la foto de quien está viendo; null si no tiene.
    val viewerPhotoUrl: String? = null,
)

// Marca la clase como ViewModel inyectable por Hilt.
@HiltViewModel
// Declara el ViewModel e inyecta sus dependencias por constructor.
class CommentsViewModel @Inject constructor(
    // Recibe los argumentos de navegación de la ruta (postId y replyToCommentId).
    savedStateHandle: SavedStateHandle,
    // Caso de uso que expone la sesión actual (cuenta autenticada).
    private val observeSession: ObserveSessionUseCase,
    // Caso de uso que emite el perfil del usuario actual.
    observeCurrentUser: ObserveCurrentUserUseCase,
    // Caso de uso que obtiene el detalle de la publicación.
    private val getPostDetail: GetPostDetailUseCase,
    // Caso de uso que obtiene una página de comentarios o respuestas.
    private val getComments: GetCommentsUseCase,
    // Caso de uso que obtiene los ids de comentarios a los que el usuario dio "me gusta".
    private val getLikedComments: GetLikedCommentsUseCase,
    // Caso de uso que alterna el "me gusta" de un comentario.
    private val toggleCommentLike: ToggleCommentLikeUseCase,
    // Caso de uso que crea un comentario o una respuesta.
    private val createComment: CreateCommentUseCase,
) : ViewModel() {

    // Llega de la ruta MainRoutes.PostComments(postId, replyToCommentId).
    private val postId: String = savedStateHandle["postId"] ?: ""

    // Si se llegó desde "Responder" en el detalle: el comentario a responder (se usa una sola vez).
    private var pendingReplyToId: String? = savedStateHandle["replyToCommentId"]

    // Estado mutable interno de la pantalla.
    private val _uiState = MutableStateFlow(CommentsUiState())
    // Estado de solo lectura que observa la UI.
    val uiState: StateFlow<CommentsUiState> = _uiState.asStateFlow()

    // Comentarios principales crudos ya leídos: base para recargar sin perder lo que se estaba viendo.
    private var topLevel: List<Comment> = emptyList()
    // Respuestas crudas ya leídas, agrupadas por el id del comentario padre.
    private val repliesByParent = mutableMapOf<String, List<Comment>>()
    // Ids de los comentarios a los que el usuario actual dio "me gusta".
    private var likedIds: Set<String> = emptySet()

    // Bloque de inicialización que se ejecuta al crear el ViewModel.
    init {
        // Lanza una corrutina para observar el perfil del usuario actual.
        viewModelScope.launch {
            // Recolecta cada cambio del usuario actual.
            observeCurrentUser().collect { user ->
                // Actualiza el nombre y la foto de quien está viendo.
                _uiState.update { it.copy(viewerName = user?.account?.userName.orEmpty(), viewerPhotoUrl = user?.photoUrl) }
            }
        }
        // Lanza una corrutina para cargar la publicación y los comentarios por primera vez.
        viewModelScope.launch { reloadAll() }
    }

    // Carga la siguiente página de comentarios principales.
    fun onLoadMore() {
        // Sale si ya no quedan más páginas por cargar.
        if (!_uiState.value.canLoadMore) return
        // Lanza una corrutina para pedir la página.
        viewModelScope.launch {
            // Pide la página siguiente tomando como cursor el último comentario cargado.
            getComments(postId, afterCommentId = topLevel.lastOrNull()?.id).onSuccess { page ->
                // Agrega la nueva página a la lista de comentarios principales.
                topLevel = topLevel + page
                // Publica el estado; si la página vino completa, puede haber más.
                publish(canLoadMore = page.size >= PAGE_SIZE)
            }
        }
    }

    // Abre o cierra el hilo de respuestas de un comentario (se cargan al abrirlo).
    fun onToggleReplies(commentId: String) {
        // Si el hilo ya está abierto, lo cierra.
        if (commentId in _uiState.value.expanded) {
            // Quita el comentario del conjunto de hilos abiertos.
            _uiState.update { it.copy(expanded = it.expanded - commentId) }
            // Termina sin cargar nada.
            return
        }
        // Lanza una corrutina para cargar y abrir el hilo.
        viewModelScope.launch {
            // Carga todas las respuestas del comentario.
            loadReplies(commentId)
            // Marca el hilo como abierto.
            _uiState.update { it.copy(expanded = it.expanded + commentId) }
            // Publica las respuestas cargadas en el estado.
            publish()
        }
    }

    // Marca el comentario al que se va a responder y limpia el error de envío.
    fun onReplyClick(comment: CommentUi) = _uiState.update { it.copy(replyingTo = comment, sendError = false) }

    // Cancela la respuesta en curso para volver a escribir un comentario principal.
    fun onCancelReply() = _uiState.update { it.copy(replyingTo = null) }

    // Actualiza el texto escrito y limpia el error de envío.
    fun onTextChange(value: String) = _uiState.update { it.copy(text = value, sendError = false) }

    // Envía el comentario o la respuesta escrita.
    fun onSend() {
        // Toma una foto del estado actual.
        val state = _uiState.value
        // No envía si el texto está vacío, ya se está enviando o no se permite comentar.
        if (state.text.isBlank() || state.isSending || !state.canComment) return
        // Marca que el envío está en curso.
        _uiState.update { it.copy(isSending = true) }
        // Lanza una corrutina para crear el comentario.
        viewModelScope.launch {
            // Crea el comentario, pasando el id del comentario respondido si lo hay.
            createComment(postId, state.text, state.replyingTo?.id)
                // Si se guardó correctamente.
                .onSuccess { saved ->
                    // El dominio puede "aplanar" la respuesta bajo otro padre: se abre ese hilo.
                    saved.parentId?.let { parent -> _uiState.update { it.copy(expanded = it.expanded + parent) } }
                    // Limpia el campo, la respuesta en curso y la bandera de envío.
                    _uiState.update { it.copy(text = "", replyingTo = null, isSending = false) }
                    // Recarga todo para mostrar el comentario nuevo y los contadores actualizados.
                    reloadAll()
                }
                // Si falla, quita la bandera de envío y marca el error.
                .onFailure { _uiState.update { it.copy(isSending = false, sendError = true) } }
        }
    }

    // Alterna el "me gusta" de un comentario.
    fun onLikeClick(commentId: String) {
        // Lanza una corrutina para ejecutar el caso de uso.
        viewModelScope.launch {
            // Alterna el "me gusta"; el resultado indica si quedó marcado o no.
            toggleCommentLike(postId, commentId).onSuccess { liked ->
                // Agrega o quita el id del conjunto local de "me gusta".
                likedIds = if (liked) likedIds + commentId else likedIds - commentId
                // Ajusta el contador sin volver a leer todo: suma o resta uno.
                val delta = if (liked) 1 else -1
                // Actualiza el contador en los comentarios principales.
                topLevel = topLevel.map { if (it.id == commentId) it.copy(likeCount = it.likeCount + delta) else it }
                // Actualiza el contador en las respuestas de cada hilo.
                repliesByParent.replaceAll { _, list ->
                    // Reemplaza solo el comentario afectado dentro de la lista.
                    list.map { if (it.id == commentId) it.copy(likeCount = it.likeCount + delta) else it }
                }
                // Publica el estado actualizado.
                publish()
            }
        }
    }

    // Vuelve a leer la publicación, los principales ya vistos y los hilos abiertos.
    private suspend fun reloadAll() {
        // Obtiene la sesión actual; si no hay sesión no hace nada.
        val session = observeSession().value ?: return
        // Obtiene el detalle de la publicación o marca error si falla.
        val post = getPostDetail(session, postId).getOrElse {
            // Marca el error y quita la carga.
            _uiState.update { it.copy(isLoading = false, hasError = true) }
            // Termina la recarga.
            return
        }
        // Obtiene los ids de comentarios con "me gusta" del usuario (vacío si falla).
        likedIds = getLikedComments(postId).getOrNull().orEmpty()

        // Pide páginas hasta cubrir lo que ya se mostraba (al menos una página).
        val target = topLevel.size.coerceAtLeast(PAGE_SIZE)
        // Lista donde se acumulan los comentarios leídos.
        val loaded = mutableListOf<Comment>()
        // Tamaño de la última página leída, para saber si quedan más.
        var lastPageSize: Int
        // Repite la lectura de páginas.
        do {
            // Pide la siguiente página o marca error si falla.
            val page = getComments(postId, afterCommentId = loaded.lastOrNull()?.id).getOrElse {
                // Marca el error y quita la carga.
                _uiState.update { it.copy(isLoading = false, hasError = true) }
                // Termina la recarga.
                return
            }
            // Agrega la página a lo cargado.
            loaded += page
            // Guarda el tamaño de la página leída.
            lastPageSize = page.size
        // Continúa mientras la página venga llena y no se alcance el objetivo.
        } while (lastPageSize >= PAGE_SIZE && loaded.size < target)
        // Reemplaza los comentarios principales por los recién leídos.
        topLevel = loaded

        // Recarga las respuestas de cada hilo que estaba abierto.
        _uiState.value.expanded.forEach { loadReplies(it) }
        // Actualiza título, total de comentarios y si se permite comentar.
        _uiState.update {
            // Solo se puede comentar si la publicación aparece en el feed público.
            it.copy(postTitle = post.title, totalComments = post.commentCount, canComment = post.showsInPublicFeed())
        }
        // Publica los comentarios; hay más si la última página vino llena.
        publish(canLoadMore = lastPageSize >= PAGE_SIZE)

        // Llegó desde "Responder": deja lista la respuesta a ese comentario (la pantalla lo resalta y baja hasta él).
        pendingReplyToId?.let { id ->
            // Consume el id para que solo se aplique una vez.
            pendingReplyToId = null
            // Busca el comentario entre los principales cargados.
            _uiState.value.comments.firstOrNull { it.id == id }?.let { target ->
                // Lo marca como el comentario al que se responde.
                _uiState.update { it.copy(replyingTo = target) }
            }
        }
    }

    // Todas las respuestas del hilo (pide páginas hasta terminar), así "Ver respuestas" las muestra completas.
    private suspend fun loadReplies(parentId: String) {
        // Lista donde se acumulan las respuestas.
        val all = mutableListOf<Comment>()
        // Repite la lectura de páginas de respuestas.
        do {
            // Pide la siguiente página de respuestas del padre indicado.
            val page = getComments(postId, parentCommentId = parentId, afterCommentId = all.lastOrNull()?.id)
                // Si falla, deja el hilo como estaba y termina.
                .getOrElse { return }
            // Agrega la página a la lista.
            all += page
        // Continúa mientras la página venga llena.
        } while (page.size >= PAGE_SIZE)
        // Guarda las respuestas del hilo.
        repliesByParent[parentId] = all
    }

    // Traduce lo leído (modelos del dominio) al estado de la pantalla.
    private fun publish(canLoadMore: Boolean = _uiState.value.canLoadMore) {
        // Obtiene el id de quien está viendo para marcar comentarios propios.
        val viewerId = observeSession().value?.id
        // Actualiza el estado con los modelos de presentación.
        _uiState.update { state ->
            // Crea una copia nueva del estado.
            state.copy(
                // Convierte los comentarios principales a CommentUi.
                comments = topLevel.map { it.toUi(likedIds, viewerId) },
                // Convierte las respuestas de cada hilo a CommentUi.
                replies = repliesByParent.mapValues { (_, list) -> list.map { it.toUi(likedIds, viewerId) } },
                // Indica si se pueden cargar más páginas.
                canLoadMore = canLoadMore,
                // Termina el estado de carga.
                isLoading = false,
                // Limpia cualquier error previo.
                hasError = false,
            )
        }
    }

    // Constantes privadas del ViewModel.
    private companion object {
        // Mismo tamaño de página que usa GetCommentsUseCase.
        const val PAGE_SIZE = GetCommentsUseCase.PAGE_SIZE
    }
}
