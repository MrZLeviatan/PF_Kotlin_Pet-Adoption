package co.edu.uniquindio.petadoptionapp.features.user.post.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.petadoptionapp.domain.error.AdoptionException
import co.edu.uniquindio.petadoptionapp.domain.model.adoption.AdoptionRequestStatus
import co.edu.uniquindio.petadoptionapp.domain.model.post.AnimalSize
import co.edu.uniquindio.petadoptionapp.domain.model.post.AnimalType
import co.edu.uniquindio.petadoptionapp.domain.model.post.Category
import co.edu.uniquindio.petadoptionapp.domain.model.post.Post
import co.edu.uniquindio.petadoptionapp.domain.model.post.PostStatus
import co.edu.uniquindio.petadoptionapp.domain.model.post.VerificationStatus
import co.edu.uniquindio.petadoptionapp.domain.model.users.User
import co.edu.uniquindio.petadoptionapp.domain.usecase.auth.ObserveSessionUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.catalog.LoadCatalogUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.catalog.ObserveAnimalTypesUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.catalog.ObserveCategoriesUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.chat.adoption.GetMyAdoptionRequestUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.chat.adoption.SendAdoptionInterestUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.comment.CreateCommentUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.comment.GetCommentsUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.comment.GetLikedCommentsUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.comment.ToggleCommentLikeUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.importantVote.GetMyVotedPostIdsUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.importantVote.ToggleImportantUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.DeletePostUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.GetPostDetailUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.GetUserProfileUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.ObserveCurrentUserUseCase
import co.edu.uniquindio.petadoptionapp.features.user.post.comments.CommentUi
import co.edu.uniquindio.petadoptionapp.features.user.post.comments.toUi
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Publicación lista para dibujar. Los textos fijos (tamaño, vacunas...) los pone la UI. */
data class PostDetailUi(
    val id: String,
    val photoUrls: List<String>,
    val categoryName: String,
    val isVerified: Boolean,
    val title: String,
    val city: String,
    val address: String,
    val country: String,
    // Distancia a quien mira; null si alguno no tiene coordenadas.
    val distanceKm: Double?,
    val ageHours: Int,
    val animalTypeName: String?,
    val breed: String,
    val size: AnimalSize?,
    val vaccinated: Boolean?,
    val description: String,
    val commentCount: Int,
    val importantCount: Int,
    // Solo las verificadas y en proceso aceptan comentarios, votos e interés.
    val isPublic: Boolean,
    val allowsAdoption: Boolean,
    // Finalizada: el caso se cerró; no se edita, no se elimina y no recibe interacciones.
    val isFinished: Boolean,
    // Rechazada por un moderador: se muestra su motivo; no se edita, pero sí se puede eliminar.
    val isRejected: Boolean = false,
    val rejectionReason: String? = null,
)

// Quien publicó: alias y foto siempre; el nombre real solo si su perfil es público.
data class PostAuthorUi(
    val id: String,
    val userName: String,
    val photoUrl: String?,
    val name: String?,
)

// Mensajes cortos (snackbar); el texto lo pone la UI con strings.xml.
enum class DetailMessage { ALREADY_REQUESTED, ACTION_ERROR, COMMENT_ERROR }

data class PostDetailUiState(
    val post: PostDetailUi? = null,
    val author: PostAuthorUi? = null,
    val isOwner: Boolean = false,
    val isLoading: Boolean = true,
    val notFound: Boolean = false,
    // Vista previa: los primeros comentarios principales (como en Facebook).
    val comments: List<CommentUi> = emptyList(),
    val commentText: String = "",
    val isSendingComment: Boolean = false,
    val viewerName: String = "",
    val viewerPhotoUrl: String? = null,
    val isVoted: Boolean = false,
    val isTogglingImportant: Boolean = false,
    val adoptionRequested: Boolean = false,
    // Chat de la solicitud ya enviada: el botón pasa a ser "Ir al chat".
    val adoptionChatId: String? = null,
    val isSendingInterest: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val isDeleting: Boolean = false,
    val message: DetailMessage? = null,
)

sealed interface PostDetailEvent {
    // La publicación se eliminó: la navegación vuelve atrás.
    data object Deleted : PostDetailEvent
    // Abrir el chat con el autor (después de "Me interesa adoptar" o con "Ir al chat").
    data class OpenChat(val chatId: String) : PostDetailEvent
}

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeSession: ObserveSessionUseCase,
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    private val getPostDetail: GetPostDetailUseCase,
    private val observeCategories: ObserveCategoriesUseCase,
    private val observeAnimalTypes: ObserveAnimalTypesUseCase,
    private val loadCatalog: LoadCatalogUseCase,
    private val getComments: GetCommentsUseCase,
    private val getLikedComments: GetLikedCommentsUseCase,
    private val toggleCommentLike: ToggleCommentLikeUseCase,
    private val createComment: CreateCommentUseCase,
    private val toggleImportant: ToggleImportantUseCase,
    private val getMyVotedPostIds: GetMyVotedPostIdsUseCase,
    private val sendAdoptionInterest: SendAdoptionInterestUseCase,
    private val getMyAdoptionRequest: GetMyAdoptionRequestUseCase,
    private val deletePost: DeletePostUseCase,
    private val getUserProfile: GetUserProfileUseCase,
) : ViewModel() {

    // Llega de la ruta MainRoutes.PostDetail(postId).
    private val postId: String = savedStateHandle["postId"] ?: ""

    private val _uiState = MutableStateFlow(PostDetailUiState())
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()

    private val _events = Channel<PostDetailEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // Datos de origen: se vuelven a traducir si cambia el catálogo o el perfil.
    private var lastPost: Post? = null
    private var viewer: User? = null

    init {
        viewModelScope.launch { if (observeCategories().value.isEmpty()) loadCatalog() }

        // Perfil de quien mira (avatar de la barra y distancia) + catálogos para los nombres.
        viewModelScope.launch {
            combine(observeCurrentUser(), observeCategories(), observeAnimalTypes()) { user, categories, types ->
                Triple(user, categories, types)
            }.collect { (user, categories, types) ->
                viewer = user
                _uiState.update { state ->
                    state.copy(
                        viewerName = user?.account?.userName.orEmpty(),
                        viewerPhotoUrl = user?.photoUrl,
                        post = lastPost?.toUi(categories, types),
                    )
                }
            }
        }
    }

    /** Carga (o recarga al volver de comentarios) la publicación y su vista previa. */
    fun refresh() {
        val session = observeSession().value ?: return
        viewModelScope.launch {
            val post = getPostDetail(session, postId).getOrElse {
                _uiState.update { it.copy(isLoading = false, notFound = true) }
                return@launch
            }
            lastPost = post
            val isOwner = post.authorId == session.id
            _uiState.update {
                it.copy(
                    post = post.toUi(observeCategories().value, observeAnimalTypes().value),
                    isOwner = isOwner,
                    isLoading = false,
                    notFound = false,
                )
            }
            loadCommentsPreview()

            // Autor: la privacidad la aplica el caso de uso (el nombre llega en null si es privado).
            getUserProfile(post.authorId, session.id).onSuccess { profile ->
                _uiState.update {
                    it.copy(author = PostAuthorUi(profile.id, profile.userName, profile.photoUrl, profile.name?.takeIf(String::isNotBlank)))
                }
            }

            // Estado de los botones de quien no es el dueño.
            if (!isOwner) {
                val voted = getMyVotedPostIds(listOf(postId)).getOrNull().orEmpty()
                val request = getMyAdoptionRequest(postId).getOrNull()
                    ?.takeIf { it.status != AdoptionRequestStatus.CANCELLED }
                _uiState.update {
                    it.copy(
                        isVoted = postId in voted,
                        adoptionRequested = request != null,
                        adoptionChatId = request?.chatId?.takeIf(String::isNotBlank),
                    )
                }
            }
        }
    }

    // Vista previa: solo los primeros comentarios principales (las respuestas se ven en la pantalla completa).
    private suspend fun loadCommentsPreview() {
        val liked = getLikedComments(postId).getOrNull().orEmpty()
        val viewerId = observeSession().value?.id
        getComments(postId).onSuccess { comments ->
            _uiState.update { state ->
                state.copy(comments = comments.take(PREVIEW_SIZE).map { it.toUi(liked, viewerId) })
            }
        }
    }

    // --- Comentarios ---

    fun onCommentTextChange(value: String) = _uiState.update { it.copy(commentText = value) }

    fun onSendComment() {
        val state = _uiState.value
        if (state.commentText.isBlank() || state.isSendingComment) return
        _uiState.update { it.copy(isSendingComment = true) }
        viewModelScope.launch {
            // Desde el detalle solo se crean comentarios principales; las respuestas, en la pantalla completa.
            createComment(postId, state.commentText)
                .onSuccess {
                    _uiState.update { it.copy(commentText = "", isSendingComment = false) }
                    // Recarga para que el contador y la vista previa queden al día.
                    refresh()
                }
                .onFailure { _uiState.update { it.copy(isSendingComment = false, message = DetailMessage.COMMENT_ERROR) } }
        }
    }

    fun onCommentLike(commentId: String) {
        viewModelScope.launch {
            toggleCommentLike(postId, commentId)
                .onSuccess { loadCommentsPreview() }
                .onFailure { _uiState.update { it.copy(message = DetailMessage.ACTION_ERROR) } }
        }
    }

    // --- Acciones de quien no es el dueño ---

    fun onImportantClick() {
        if (_uiState.value.isTogglingImportant) return
        _uiState.update { it.copy(isTogglingImportant = true) }
        viewModelScope.launch {
            toggleImportant(postId)
                .onSuccess { voted ->
                    _uiState.update { state ->
                        state.copy(
                            isVoted = voted,
                            isTogglingImportant = false,
                            post = state.post?.let { p ->
                                p.copy(importantCount = (p.importantCount + if (voted) 1 else -1).coerceAtLeast(0))
                            },
                        )
                    }
                }
                .onFailure { _uiState.update { it.copy(isTogglingImportant = false, message = DetailMessage.ACTION_ERROR) } }
        }
    }

    /**
     "Me interesa adoptar": el caso de uso crea la solicitud, abre el chat con el autor y deja el mensaje
     automático; luego se abre ese chat. Si ya se había enviado, el botón es "Ir al chat" y solo lo abre.
     */
    fun onAdoptionInterestClick() {
        val state = _uiState.value
        if (state.isSendingInterest) return
        state.adoptionChatId?.let { _events.trySend(PostDetailEvent.OpenChat(it)); return }
        if (state.adoptionRequested) return
        _uiState.update { it.copy(isSendingInterest = true) }
        viewModelScope.launch {
            sendAdoptionInterest(postId)
                .onSuccess { request -> openAdoptionChat(request.chatId) }
                .onFailure { error ->
                    if (error === AdoptionException.AlreadyRequested) {
                        // Ya existía (por ejemplo, desde otro dispositivo): se abre su chat.
                        val chatId = getMyAdoptionRequest(postId).getOrNull()?.chatId
                        if (!chatId.isNullOrBlank()) {
                            openAdoptionChat(chatId)
                            return@launch
                        }
                    }
                    _uiState.update {
                        it.copy(
                            isSendingInterest = false,
                            adoptionRequested = error === AdoptionException.AlreadyRequested || it.adoptionRequested,
                            message = if (error === AdoptionException.AlreadyRequested) DetailMessage.ALREADY_REQUESTED else DetailMessage.ACTION_ERROR,
                        )
                    }
                }
        }
    }

    private fun openAdoptionChat(chatId: String) {
        _uiState.update { it.copy(isSendingInterest = false, adoptionRequested = true, adoptionChatId = chatId) }
        _events.trySend(PostDetailEvent.OpenChat(chatId))
    }

    // --- Acciones del dueño ---

    fun onDeleteClick() = _uiState.update { it.copy(showDeleteDialog = true) }

    fun onDeleteDismiss() = _uiState.update { if (it.isDeleting) it else it.copy(showDeleteDialog = false) }

    fun onDeleteConfirm() {
        val session = observeSession().value ?: return
        if (_uiState.value.isDeleting) return
        _uiState.update { it.copy(isDeleting = true) }
        viewModelScope.launch {
            deletePost(session.id, postId)
                .onSuccess {
                    _uiState.update { it.copy(isDeleting = false, showDeleteDialog = false) }
                    _events.send(PostDetailEvent.Deleted)
                }
                .onFailure {
                    _uiState.update {
                        it.copy(isDeleting = false, showDeleteDialog = false, message = DetailMessage.ACTION_ERROR)
                    }
                }
        }
    }

    fun onMessageShown() = _uiState.update { it.copy(message = null) }

    private fun Post.toUi(categories: List<Category>, types: List<AnimalType>): PostDetailUi {
        val category = categories.firstOrNull { it.id == categoryId }
        return PostDetailUi(
            id = id,
            photoUrls = photoUrls,
            categoryName = category?.name.orEmpty(),
            isVerified = verification.status == VerificationStatus.VERIFIED,
            title = title,
            city = location.city,
            address = location.address,
            country = location.country,
            distanceKm = viewer?.location?.distanceKmTo(location),
            ageHours = ((System.currentTimeMillis() - createdAt) / MILLIS_PER_HOUR).toInt().coerceAtLeast(0),
            animalTypeName = animalTypeId?.let { id -> types.firstOrNull { it.id == id }?.name },
            breed = breed,
            size = size,
            vaccinated = vaccinated,
            description = description,
            commentCount = commentCount,
            importantCount = importantCount,
            isPublic = showsInPublicFeed(),
            allowsAdoption = category?.allowsAdoptionRequests == true,
            isFinished = status == PostStatus.FINISHED,
            isRejected = verification.status == VerificationStatus.REJECTED,
            rejectionReason = verification.rejectionReason?.takeIf { it.isNotBlank() },
        )
    }

    private companion object {
        // Comentarios que se ven en el detalle antes de "Ver todos".
        const val PREVIEW_SIZE = 3
        const val MILLIS_PER_HOUR = 60 * 60 * 1000L
    }
}
