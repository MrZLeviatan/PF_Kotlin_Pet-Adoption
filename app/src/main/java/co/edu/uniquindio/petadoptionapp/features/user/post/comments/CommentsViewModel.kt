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

data class CommentsUiState(
    val postTitle: String = "",
    val totalComments: Int = 0,
    // Comentarios principales (más recientes primero) y las respuestas de cada hilo abierto.
    val comments: List<CommentUi> = emptyList(),
    val replies: Map<String, List<CommentUi>> = emptyMap(),
    val expanded: Set<String> = emptySet(),
    val canLoadMore: Boolean = false,
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
    // Solo las publicaciones visibles al público aceptan comentarios (regla del caso de uso).
    val canComment: Boolean = false,
    val text: String = "",
    val replyingTo: CommentUi? = null,
    val isSending: Boolean = false,
    val sendError: Boolean = false,
    val viewerName: String = "",
    val viewerPhotoUrl: String? = null,
)

@HiltViewModel
class CommentsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeSession: ObserveSessionUseCase,
    observeCurrentUser: ObserveCurrentUserUseCase,
    private val getPostDetail: GetPostDetailUseCase,
    private val getComments: GetCommentsUseCase,
    private val getLikedComments: GetLikedCommentsUseCase,
    private val toggleCommentLike: ToggleCommentLikeUseCase,
    private val createComment: CreateCommentUseCase,
) : ViewModel() {

    // Llega de la ruta MainRoutes.PostComments(postId, replyToCommentId).
    private val postId: String = savedStateHandle["postId"] ?: ""

    // Si se llegó desde "Responder" en el detalle: el comentario a responder (se usa una sola vez).
    private var pendingReplyToId: String? = savedStateHandle["replyToCommentId"]

    private val _uiState = MutableStateFlow(CommentsUiState())
    val uiState: StateFlow<CommentsUiState> = _uiState.asStateFlow()

    // Comentarios crudos ya leídos: base para recargar sin perder lo que se estaba viendo.
    private var topLevel: List<Comment> = emptyList()
    private val repliesByParent = mutableMapOf<String, List<Comment>>()
    private var likedIds: Set<String> = emptySet()

    init {
        viewModelScope.launch {
            observeCurrentUser().collect { user ->
                _uiState.update { it.copy(viewerName = user?.account?.userName.orEmpty(), viewerPhotoUrl = user?.photoUrl) }
            }
        }
        viewModelScope.launch { reloadAll() }
    }

    fun onLoadMore() {
        if (!_uiState.value.canLoadMore) return
        viewModelScope.launch {
            getComments(postId, afterCommentId = topLevel.lastOrNull()?.id).onSuccess { page ->
                topLevel = topLevel + page
                publish(canLoadMore = page.size >= PAGE_SIZE)
            }
        }
    }

    // Abre o cierra el hilo de respuestas de un comentario (se cargan al abrirlo).
    fun onToggleReplies(commentId: String) {
        if (commentId in _uiState.value.expanded) {
            _uiState.update { it.copy(expanded = it.expanded - commentId) }
            return
        }
        viewModelScope.launch {
            loadReplies(commentId)
            _uiState.update { it.copy(expanded = it.expanded + commentId) }
            publish()
        }
    }

    fun onReplyClick(comment: CommentUi) = _uiState.update { it.copy(replyingTo = comment, sendError = false) }

    fun onCancelReply() = _uiState.update { it.copy(replyingTo = null) }

    fun onTextChange(value: String) = _uiState.update { it.copy(text = value, sendError = false) }

    fun onSend() {
        val state = _uiState.value
        if (state.text.isBlank() || state.isSending || !state.canComment) return
        _uiState.update { it.copy(isSending = true) }
        viewModelScope.launch {
            createComment(postId, state.text, state.replyingTo?.id)
                .onSuccess { saved ->
                    // El dominio puede "aplanar" la respuesta bajo otro padre: se abre ese hilo.
                    saved.parentId?.let { parent -> _uiState.update { it.copy(expanded = it.expanded + parent) } }
                    _uiState.update { it.copy(text = "", replyingTo = null, isSending = false) }
                    reloadAll()
                }
                .onFailure { _uiState.update { it.copy(isSending = false, sendError = true) } }
        }
    }

    fun onLikeClick(commentId: String) {
        viewModelScope.launch {
            toggleCommentLike(postId, commentId).onSuccess { liked ->
                likedIds = if (liked) likedIds + commentId else likedIds - commentId
                // Ajusta el contador sin volver a leer todo.
                val delta = if (liked) 1 else -1
                topLevel = topLevel.map { if (it.id == commentId) it.copy(likeCount = it.likeCount + delta) else it }
                repliesByParent.replaceAll { _, list ->
                    list.map { if (it.id == commentId) it.copy(likeCount = it.likeCount + delta) else it }
                }
                publish()
            }
        }
    }

    // Vuelve a leer la publicación, los principales ya vistos y los hilos abiertos.
    private suspend fun reloadAll() {
        val session = observeSession().value ?: return
        val post = getPostDetail(session, postId).getOrElse {
            _uiState.update { it.copy(isLoading = false, hasError = true) }
            return
        }
        likedIds = getLikedComments(postId).getOrNull().orEmpty()

        // Pide páginas hasta cubrir lo que ya se mostraba (al menos una página).
        val target = topLevel.size.coerceAtLeast(PAGE_SIZE)
        val loaded = mutableListOf<Comment>()
        var lastPageSize: Int
        do {
            val page = getComments(postId, afterCommentId = loaded.lastOrNull()?.id).getOrElse {
                _uiState.update { it.copy(isLoading = false, hasError = true) }
                return
            }
            loaded += page
            lastPageSize = page.size
        } while (lastPageSize >= PAGE_SIZE && loaded.size < target)
        topLevel = loaded

        _uiState.value.expanded.forEach { loadReplies(it) }
        _uiState.update {
            it.copy(postTitle = post.title, totalComments = post.commentCount, canComment = post.showsInPublicFeed())
        }
        publish(canLoadMore = lastPageSize >= PAGE_SIZE)

        // Llegó desde "Responder": deja lista la respuesta a ese comentario (la pantalla lo resalta y baja hasta él).
        pendingReplyToId?.let { id ->
            pendingReplyToId = null
            _uiState.value.comments.firstOrNull { it.id == id }?.let { target ->
                _uiState.update { it.copy(replyingTo = target) }
            }
        }
    }

    // Todas las respuestas del hilo (pide páginas hasta terminar), así "Ver respuestas" las muestra completas.
    private suspend fun loadReplies(parentId: String) {
        val all = mutableListOf<Comment>()
        do {
            val page = getComments(postId, parentCommentId = parentId, afterCommentId = all.lastOrNull()?.id)
                .getOrElse { return }
            all += page
        } while (page.size >= PAGE_SIZE)
        repliesByParent[parentId] = all
    }

    // Traduce lo leído al estado de la pantalla.
    private fun publish(canLoadMore: Boolean = _uiState.value.canLoadMore) {
        val viewerId = observeSession().value?.id
        _uiState.update { state ->
            state.copy(
                comments = topLevel.map { it.toUi(likedIds, viewerId) },
                replies = repliesByParent.mapValues { (_, list) -> list.map { it.toUi(likedIds, viewerId) } },
                canLoadMore = canLoadMore,
                isLoading = false,
                hasError = false,
            )
        }
    }

    private companion object {
        // Mismo tamaño de página que usa GetCommentsUseCase.
        const val PAGE_SIZE = GetCommentsUseCase.PAGE_SIZE
    }
}
