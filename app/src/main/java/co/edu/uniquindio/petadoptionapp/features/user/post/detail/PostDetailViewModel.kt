package co.edu.uniquindio.petadoptionapp.features.user.post.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.petadoptionapp.domain.error.AdoptionException
import co.edu.uniquindio.petadoptionapp.domain.error.AiException
import co.edu.uniquindio.petadoptionapp.domain.model.post.AiReview
import co.edu.uniquindio.petadoptionapp.domain.usecase.ai.AnalyzePostUseCase
import co.edu.uniquindio.petadoptionapp.domain.error.PostException
import co.edu.uniquindio.petadoptionapp.domain.model.adoption.AdoptionRequestStatus
import co.edu.uniquindio.petadoptionapp.domain.model.post.AnimalSize
import co.edu.uniquindio.petadoptionapp.domain.model.post.AnimalType
import co.edu.uniquindio.petadoptionapp.domain.model.post.Category
import co.edu.uniquindio.petadoptionapp.domain.model.post.Post
import co.edu.uniquindio.petadoptionapp.domain.model.post.PostStatus
import co.edu.uniquindio.petadoptionapp.domain.model.post.VerificationStatus
import co.edu.uniquindio.petadoptionapp.domain.model.users.User
import co.edu.uniquindio.petadoptionapp.domain.model.users.UserRole
import co.edu.uniquindio.petadoptionapp.domain.usecase.auth.ObserveSessionUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.catalog.LoadCatalogUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.catalog.ObserveAnimalTypesUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.catalog.ObserveCategoriesUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.chat.adoption.GetMyAdoptionRequestUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.chat.StartChatUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.chat.adoption.SendAdoptionInterestUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.comment.CreateCommentUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.comment.GetCommentsUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.comment.GetLikedCommentsUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.comment.ToggleCommentLikeUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.importantVote.GetMyVotedPostIdsUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.importantVote.ToggleImportantUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.ApprovePostUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.DeletePostUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.FinishPostUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.StartReviewUseCase
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

/**
ViewModel del detalle de una publicación (PostDetailScreen).
Recibe el `postId` de la ruta MainRoutes.PostDetail mediante SavedStateHandle.
Estado (PostDetailUiState): la publicación traducida (PostDetailUi), su autor, el rol de quien
mira (dueño, moderador o usuario), la vista previa de comentarios, el voto de "importante", el
estado de la solicitud de adopción/chat, los diálogos (eliminar, aprobar, finalizar), el análisis
de IA y el mensaje corto (snackbar) a mostrar.
Eventos de una sola vez (PostDetailEvent por Channel): publicación eliminada (volver atrás) y
abrir un chat; la pantalla los recolecta y navega.
Funciones según el rol:
- Usuario: comentar, dar "me gusta", marcar "importante", "Me interesa adoptar" o "Empezar a chatear".
- Dueño: eliminar la publicación.
- Moderador: tomar para revisión, aprobar, finalizar y pedir el análisis con IA.
Casos de uso: ObserveSession, ObserveCurrentUser, GetPostDetail, ObserveCategories,
ObserveAnimalTypes, LoadCatalog, GetComments, GetLikedComments, ToggleCommentLike, CreateComment,
ToggleImportant, GetMyVotedPostIds, SendAdoptionInterest, StartChat, GetMyAdoptionRequest,
DeletePost, StartReview, ApprovePost, FinishPost, AnalyzePost y GetUserProfile.
**/

/**
Publicación lista para dibujar. Los textos fijos (tamaño, vacunas...) los pone la UI.
**/
data class PostDetailUi(
    // Id de la publicación.
    val id: String,
    // URLs de las fotos.
    val photoUrls: List<String>,
    // Nombre de la categoría.
    val categoryName: String,
    // Indica si un moderador la verificó.
    val isVerified: Boolean,
    // Título.
    val title: String,
    // Ciudad.
    val city: String,
    // Dirección.
    val address: String,
    // Coordenadas guardadas (el mapa las muestra corridas, como zona aproximada): latitud.
    val latitude: Double? = null,
    // Longitud guardada.
    val longitude: Double? = null,
    // País.
    val country: String,
    // Distancia a quien mira; null si alguno no tiene coordenadas.
    val distanceKm: Double?,
    // Antigüedad en horas.
    val ageHours: Int,
    // Nombre del tipo de animal (null si no se definió).
    val animalTypeName: String?,
    // Raza.
    val breed: String,
    // Tamaño.
    val size: AnimalSize?,
    // Vacunado (null = no se sabe).
    val vaccinated: Boolean?,
    // Descripción.
    val description: String,
    // Cantidad de comentarios.
    val commentCount: Int,
    // Cantidad de votos de "importante".
    val importantCount: Int,
    // Solo las verificadas y en proceso aceptan comentarios, votos e interés.
    val isPublic: Boolean,
    // Indica si la categoría permite solicitudes de adopción.
    val allowsAdoption: Boolean,
    // Finalizada: el caso se cerró; no se edita, no se elimina y no recibe interacciones.
    val isFinished: Boolean,
    // Rechazada por un moderador: se muestra su motivo; no se edita, pero sí se puede eliminar.
    val isRejected: Boolean = false,
    // Motivo del rechazo (null si no lo hay).
    val rejectionReason: String? = null,
)

// Quien publicó: alias y foto siempre; el nombre real solo si su perfil es público.
data class PostAuthorUi(
    // Id del autor.
    val id: String,
    // Alias del autor.
    val userName: String,
    // Foto del autor.
    val photoUrl: String?,
    // Nombre real (null si su perfil es privado).
    val name: String?,
)

// Mensajes cortos (snackbar); el texto lo pone la UI con strings.xml.
enum class DetailMessage {
    ALREADY_REQUESTED, ACTION_ERROR, COMMENT_ERROR, REVIEW_TAKEN, REVIEW_TAKEN_BY_OTHER, APPROVED, FINISHED,
    AI_DONE, AI_NOT_CONFIGURED, AI_FAILED,
}

/**
Qué puede hacer el moderador que mira la publicación:
AVAILABLE = nadie la revisa (puede tomarla), MINE = la revisa él (aprobar o rechazar),
OTHER = la revisa otro moderador (solo un aviso), VERIFIED = aprobada y en proceso (se puede finalizar),
CLOSED = rechazada o ya cerrada.
**/
enum class ModeratorReview { AVAILABLE, MINE, OTHER, VERIFIED, CLOSED }

/**
Estado inmutable de la pantalla de detalle.
**/
data class PostDetailUiState(
    // Publicación traducida (null mientras carga).
    val post: PostDetailUi? = null,
    // Autor de la publicación.
    val author: PostAuthorUi? = null,
    // Indica si quien mira es el dueño.
    val isOwner: Boolean = false,
    // Un moderador la está viendo (desde su inicio): no adopta, no vota, no chatea ni comenta.
    val isModerator: Boolean = false,
    // Solo para moderadores (null para los demás).
    val moderatorReview: ModeratorReview? = null,
    // Análisis de la IA (solo se muestra a moderadores).
    val aiReview: AiReview? = null,
    // Indica si se está pidiendo de nuevo el análisis de IA.
    val isAnalyzing: Boolean = false,
    // Indica si se está tomando la publicación para revisión.
    val isTakingReview: Boolean = false,
    // Diálogo de aprobar visible.
    val showApproveDialog: Boolean = false,
    // Aprobación en curso.
    val isApproving: Boolean = false,
    // Diálogo de finalizar visible.
    val showFinishDialog: Boolean = false,
    // Finalización en curso.
    val isFinishing: Boolean = false,
    // Carga inicial en curso.
    val isLoading: Boolean = true,
    // La publicación no existe o no se puede ver.
    val notFound: Boolean = false,
    // Vista previa: los primeros comentarios principales (como en Facebook).
    val comments: List<CommentUi> = emptyList(),
    // Texto del comentario que se está escribiendo.
    val commentText: String = "",
    // Envío de comentario en curso.
    val isSendingComment: Boolean = false,
    // Alias de quien mira.
    val viewerName: String = "",
    // Foto de quien mira.
    val viewerPhotoUrl: String? = null,
    // Indica si quien mira ya la marcó como "importante".
    val isVoted: Boolean = false,
    // Cambio de voto en curso.
    val isTogglingImportant: Boolean = false,
    // Indica si ya envió una solicitud de adopción.
    val adoptionRequested: Boolean = false,
    // Chat de la solicitud ya enviada: el botón pasa a ser "Ir al chat".
    val adoptionChatId: String? = null,
    // Envío del interés de adopción en curso.
    val isSendingInterest: Boolean = false,
    // "Empezar a chatear" (publicaciones que no son de adopción) mientras se abre el chat.
    val isStartingChat: Boolean = false,
    // Diálogo de eliminar visible.
    val showDeleteDialog: Boolean = false,
    // Eliminación en curso.
    val isDeleting: Boolean = false,
    // Mensaje corto pendiente de mostrar.
    val message: DetailMessage? = null,
)

// Eventos de una sola vez que la pantalla convierte en navegación.
sealed interface PostDetailEvent {
    // La publicación se eliminó: la navegación vuelve atrás.
    data object Deleted : PostDetailEvent
    // Abrir el chat con el autor (después de "Me interesa adoptar" o con "Ir al chat").
    data class OpenChat(val chatId: String) : PostDetailEvent
}

// Marca la clase como ViewModel inyectable por Hilt.
@HiltViewModel
// Declara el ViewModel con todos sus casos de uso inyectados.
class PostDetailViewModel @Inject constructor(
    // Argumentos de navegación (postId).
    savedStateHandle: SavedStateHandle,
    // Sesión actual.
    private val observeSession: ObserveSessionUseCase,
    // Perfil del usuario actual.
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    // Detalle de la publicación.
    private val getPostDetail: GetPostDetailUseCase,
    // Categorías del catálogo.
    private val observeCategories: ObserveCategoriesUseCase,
    // Tipos de animal del catálogo.
    private val observeAnimalTypes: ObserveAnimalTypesUseCase,
    // Carga del catálogo.
    private val loadCatalog: LoadCatalogUseCase,
    // Lectura de comentarios.
    private val getComments: GetCommentsUseCase,
    // Comentarios con "me gusta" del usuario.
    private val getLikedComments: GetLikedCommentsUseCase,
    // Alternar "me gusta" de un comentario.
    private val toggleCommentLike: ToggleCommentLikeUseCase,
    // Crear comentario.
    private val createComment: CreateCommentUseCase,
    // Alternar voto de "importante".
    private val toggleImportant: ToggleImportantUseCase,
    // Publicaciones votadas por el usuario.
    private val getMyVotedPostIds: GetMyVotedPostIdsUseCase,
    // Enviar interés de adopción.
    private val sendAdoptionInterest: SendAdoptionInterestUseCase,
    // Iniciar chat con el autor.
    private val startChat: StartChatUseCase,
    // Solicitud de adopción propia sobre la publicación.
    private val getMyAdoptionRequest: GetMyAdoptionRequestUseCase,
    // Eliminar publicación.
    private val deletePost: DeletePostUseCase,
    // Tomar publicación para revisión (moderador).
    private val startReview: StartReviewUseCase,
    // Aprobar publicación (moderador).
    private val approvePost: ApprovePostUseCase,
    // Finalizar publicación.
    private val finishPost: FinishPostUseCase,
    // Análisis con IA.
    private val analyzePost: AnalyzePostUseCase,
    // Perfil público del autor.
    private val getUserProfile: GetUserProfileUseCase,
) : ViewModel() {

    // Llega de la ruta MainRoutes.PostDetail(postId).
    private val postId: String = savedStateHandle["postId"] ?: ""

    // Estado mutable interno.
    private val _uiState = MutableStateFlow(PostDetailUiState())
    // Estado de solo lectura que observa la pantalla.
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()

    // Canal de eventos de una sola vez (con búfer para no perderlos).
    private val _events = Channel<PostDetailEvent>(Channel.BUFFERED)
    // Flujo de eventos que recolecta la pantalla.
    val events = _events.receiveAsFlow()

    // Datos de origen: se vuelven a traducir si cambia el catálogo o el perfil. Última publicación leída.
    private var lastPost: Post? = null
    // Perfil de quien mira (para la distancia).
    private var viewer: User? = null

    // Inicialización del ViewModel.
    init {
        // Carga el catálogo si aún está vacío.
        viewModelScope.launch { if (observeCategories().value.isEmpty()) loadCatalog() }

        // Perfil de quien mira (avatar de la barra y distancia) + catálogos para los nombres.
        viewModelScope.launch {
            // Combina usuario, categorías y tipos de animal.
            combine(observeCurrentUser(), observeCategories(), observeAnimalTypes()) { user, categories, types ->
                // Agrupa los tres valores.
                Triple(user, categories, types)
            // Recolecta cada combinación.
            }.collect { (user, categories, types) ->
                // Guarda el perfil de quien mira.
                viewer = user
                // Actualiza avatar y vuelve a traducir la publicación.
                _uiState.update { state ->
                    // Copia del estado.
                    state.copy(
                        // Alias de quien mira.
                        viewerName = user?.account?.userName.orEmpty(),
                        // Foto de quien mira.
                        viewerPhotoUrl = user?.photoUrl,
                        // Publicación traducida con los catálogos actuales.
                        post = lastPost?.toUi(categories, types),
                    )
                }
            }
        }
    }

    /**
    Carga (o recarga al volver de comentarios) la publicación y su vista previa.
    También carga el autor y, si quien mira no es el dueño, su voto y su solicitud de adopción.
    **/
    fun refresh() {
        // Obtiene la sesión; sin sesión no hace nada.
        val session = observeSession().value ?: return
        // Lanza una corrutina para cargar.
        viewModelScope.launch {
            // Obtiene la publicación o marca "no encontrada".
            val post = getPostDetail(session, postId).getOrElse {
                // Marca que no se encontró.
                _uiState.update { it.copy(isLoading = false, notFound = true) }
                // Sale de la corrutina.
                return@launch
            }
            // Guarda la publicación cruda.
            lastPost = post
            // Calcula si quien mira es el dueño.
            val isOwner = post.authorId == session.id
            // Publica la publicación y los datos de rol.
            _uiState.update {
                // Copia del estado.
                it.copy(
                    // Publicación traducida.
                    post = post.toUi(observeCategories().value, observeAnimalTypes().value),
                    // Es dueño.
                    isOwner = isOwner,
                    // Es moderador.
                    isModerator = session.role == UserRole.MODERATOR,
                    // Estado de revisión solo para moderadores.
                    moderatorReview = if (session.role == UserRole.MODERATOR) post.reviewFor(session.id) else null,
                    // Análisis de IA guardado.
                    aiReview = post.aiReview,
                    // Termina la carga.
                    isLoading = false,
                    // Sí se encontró.
                    notFound = false,
                )
            }
            // Carga la vista previa de comentarios.
            loadCommentsPreview()

            // Autor: la privacidad la aplica el caso de uso (el nombre llega en null si es privado).
            getUserProfile(post.authorId, session.id).onSuccess { profile ->
                // Guarda los datos del autor.
                _uiState.update {
                    // Copia con el autor (nombre vacío se trata como null).
                    it.copy(author = PostAuthorUi(profile.id, profile.userName, profile.photoUrl, profile.name?.takeIf(String::isNotBlank)))
                }
            }

            // Estado de los botones de quien no es el dueño.
            if (!isOwner) {
                // Ids de publicaciones votadas (vacío si falla).
                val voted = getMyVotedPostIds(listOf(postId)).getOrNull().orEmpty()
                // Solicitud de adopción propia, ignorando las canceladas.
                val request = getMyAdoptionRequest(postId).getOrNull()
                    ?.takeIf { it.status != AdoptionRequestStatus.CANCELLED }
                // Actualiza voto y estado de adopción.
                _uiState.update {
                    // Copia del estado.
                    it.copy(
                        // Ya votó si el id está en la lista.
                        isVoted = postId in voted,
                        // Ya solicitó si hay solicitud vigente.
                        adoptionRequested = request != null,
                        // Chat de la solicitud, si existe.
                        adoptionChatId = request?.chatId?.takeIf(String::isNotBlank),
                    )
                }
            }
        }
    }

    // Vista previa: solo los primeros comentarios principales (las respuestas se ven en la pantalla completa).
    private suspend fun loadCommentsPreview() {
        // Comentarios con "me gusta" del usuario.
        val liked = getLikedComments(postId).getOrNull().orEmpty()
        // Id de quien mira.
        val viewerId = observeSession().value?.id
        // Lee la primera página de comentarios.
        getComments(postId).onSuccess { comments ->
            // Guarda solo los primeros PREVIEW_SIZE traducidos.
            _uiState.update { state ->
                // Copia con la vista previa.
                state.copy(comments = comments.take(PREVIEW_SIZE).map { it.toUi(liked, viewerId) })
            }
        }
    }

    // --- Comentarios ---

    // Actualiza el texto del comentario.
    fun onCommentTextChange(value: String) = _uiState.update { it.copy(commentText = value) }

    // Envía un comentario principal desde el detalle.
    fun onSendComment() {
        // Toma el estado actual.
        val state = _uiState.value
        // No envía si está vacío o ya se está enviando.
        if (state.commentText.isBlank() || state.isSendingComment) return
        // Marca el envío en curso.
        _uiState.update { it.copy(isSendingComment = true) }
        // Lanza una corrutina para crear el comentario.
        viewModelScope.launch {
            // Desde el detalle solo se crean comentarios principales; las respuestas, en la pantalla completa.
            createComment(postId, state.commentText)
                // Si salió bien.
                .onSuccess {
                    // Limpia el campo y quita la bandera.
                    _uiState.update { it.copy(commentText = "", isSendingComment = false) }
                    // Recarga para que el contador y la vista previa queden al día.
                    refresh()
                }
                // Si falló, muestra el mensaje de error.
                .onFailure { _uiState.update { it.copy(isSendingComment = false, message = DetailMessage.COMMENT_ERROR) } }
        }
    }

    // Alterna el "me gusta" de un comentario de la vista previa.
    fun onCommentLike(commentId: String) {
        // Lanza una corrutina.
        viewModelScope.launch {
            // Alterna el "me gusta".
            toggleCommentLike(postId, commentId)
                // Si salió bien, recarga la vista previa.
                .onSuccess { loadCommentsPreview() }
                // Si falló, muestra error.
                .onFailure { _uiState.update { it.copy(message = DetailMessage.ACTION_ERROR) } }
        }
    }

    // --- Acciones de quien no es el dueño ---

    // Marca o desmarca la publicación como "importante".
    fun onImportantClick() {
        // Evita dobles toques.
        if (_uiState.value.isTogglingImportant) return
        // Marca la acción en curso.
        _uiState.update { it.copy(isTogglingImportant = true) }
        // Lanza una corrutina.
        viewModelScope.launch {
            // Alterna el voto.
            toggleImportant(postId)
                // Si salió bien, actualiza voto y contador localmente.
                .onSuccess { voted ->
                    // Actualiza el estado.
                    _uiState.update { state ->
                        // Copia del estado.
                        state.copy(
                            // Nuevo estado del voto.
                            isVoted = voted,
                            // Termina la acción.
                            isTogglingImportant = false,
                            // Ajusta el contador (+1 o -1, nunca negativo).
                            post = state.post?.let { p ->
                                p.copy(importantCount = (p.importantCount + if (voted) 1 else -1).coerceAtLeast(0))
                            },
                        )
                    }
                }
                // Si falló, muestra error.
                .onFailure { _uiState.update { it.copy(isTogglingImportant = false, message = DetailMessage.ACTION_ERROR) } }
        }
    }

    /**
    "Me interesa adoptar": el caso de uso crea la solicitud, abre el chat con el autor y deja el mensaje
    automático; luego se abre ese chat. Si ya se había enviado, el botón es "Ir al chat" y solo lo abre.
    **/
    fun onAdoptionInterestClick() {
        // Toma el estado actual.
        val state = _uiState.value
        // Evita dobles toques.
        if (state.isSendingInterest) return
        // Si ya hay chat, emite el evento para abrirlo y termina.
        state.adoptionChatId?.let { _events.trySend(PostDetailEvent.OpenChat(it)); return }
        // Si ya solicitó (sin chat), no hace nada.
        if (state.adoptionRequested) return
        // Marca el envío en curso.
        _uiState.update { it.copy(isSendingInterest = true) }
        // Lanza una corrutina.
        viewModelScope.launch {
            // Envía el interés de adopción.
            sendAdoptionInterest(postId)
                // Si salió bien, abre el chat de la solicitud.
                .onSuccess { request -> openAdoptionChat(request.chatId) }
                // Si falló.
                .onFailure { error ->
                    // Si ya existía la solicitud.
                    if (error === AdoptionException.AlreadyRequested) {
                        // Ya existía (por ejemplo, desde otro dispositivo): se busca su chat.
                        val chatId = getMyAdoptionRequest(postId).getOrNull()?.chatId
                        // Si tiene chat, lo abre y termina.
                        if (!chatId.isNullOrBlank()) {
                            // Abre el chat existente.
                            openAdoptionChat(chatId)
                            // Sale de la corrutina.
                            return@launch
                        }
                    }
                    // Actualiza el estado con el error correspondiente.
                    _uiState.update {
                        // Copia del estado.
                        it.copy(
                            // Termina el envío.
                            isSendingInterest = false,
                            // Marca como solicitada si ya existía.
                            adoptionRequested = error === AdoptionException.AlreadyRequested || it.adoptionRequested,
                            // Mensaje: ya solicitada o error genérico.
                            message = if (error === AdoptionException.AlreadyRequested) DetailMessage.ALREADY_REQUESTED else DetailMessage.ACTION_ERROR,
                        )
                    }
                }
        }
    }

    /**
    "Empezar a chatear" (perdidos, encontrados, veterinaria...): abre el chat de esta publicación con su autor.
    Si ya existía, StartChatUseCase devuelve el mismo (id fijo por persona y publicación), así no se duplica.
    **/
    fun onStartChatClick() {
        // Evita dobles toques.
        if (_uiState.value.isStartingChat) return
        // Marca la apertura en curso.
        _uiState.update { it.copy(isStartingChat = true) }
        // Lanza una corrutina.
        viewModelScope.launch {
            // Inicia (o recupera) el chat.
            startChat(postId)
                // Si salió bien.
                .onSuccess { chat ->
                    // Quita la bandera.
                    _uiState.update { it.copy(isStartingChat = false) }
                    // Emite el evento para abrir el chat.
                    _events.send(PostDetailEvent.OpenChat(chat.id))
                }
                // Si falló, muestra error.
                .onFailure { _uiState.update { it.copy(isStartingChat = false, message = DetailMessage.ACTION_ERROR) } }
        }
    }

    // Guarda el chat de la solicitud y emite el evento para abrirlo.
    private fun openAdoptionChat(chatId: String) {
        // Marca la solicitud como enviada con su chat.
        _uiState.update { it.copy(isSendingInterest = false, adoptionRequested = true, adoptionChatId = chatId) }
        // Emite el evento de abrir chat.
        _events.trySend(PostDetailEvent.OpenChat(chatId))
    }

    // --- Acciones del dueño ---

    // Abre el diálogo de eliminar.
    fun onDeleteClick() = _uiState.update { it.copy(showDeleteDialog = true) }

    // Cierra el diálogo de eliminar, salvo que ya se esté eliminando.
    fun onDeleteDismiss() = _uiState.update { if (it.isDeleting) it else it.copy(showDeleteDialog = false) }

    // Elimina la publicación tras confirmar.
    fun onDeleteConfirm() {
        // Obtiene la sesión; sin sesión sale.
        val session = observeSession().value ?: return
        // Evita ejecutar dos veces.
        if (_uiState.value.isDeleting) return
        // Marca la eliminación en curso.
        _uiState.update { it.copy(isDeleting = true) }
        // Lanza una corrutina.
        viewModelScope.launch {
            // Elimina la publicación.
            deletePost(session.id, postId)
                // Si salió bien.
                .onSuccess {
                    // Cierra el diálogo.
                    _uiState.update { it.copy(isDeleting = false, showDeleteDialog = false) }
                    // Emite el evento para volver atrás.
                    _events.send(PostDetailEvent.Deleted)
                }
                // Si falló.
                .onFailure {
                    // Cierra el diálogo y muestra error.
                    _uiState.update {
                        // Copia del estado.
                        it.copy(isDeleting = false, showDeleteDialog = false, message = DetailMessage.ACTION_ERROR)
                    }
                }
        }
    }

    // La pantalla ya mostró el mensaje: se limpia.
    fun onMessageShown() = _uiState.update { it.copy(message = null) }

    // --- Acciones del moderador ---

    /**
    "Seleccionar para revisión": queda a su nombre ("en revisión") y nadie más puede decidirla.
    **/
    fun onTakeReviewClick() {
        // Evita dobles toques.
        if (_uiState.value.isTakingReview) return
        // Marca la acción en curso.
        _uiState.update { it.copy(isTakingReview = true) }
        // Lanza una corrutina.
        viewModelScope.launch {
            // Toma la publicación para revisión.
            startReview(postId)
                // Si salió bien, avisa.
                .onSuccess { _uiState.update { it.copy(isTakingReview = false, message = DetailMessage.REVIEW_TAKEN) } }
                // Si falló.
                .onFailure { error ->
                    // Otro moderador la tomó un instante antes: se avisa y se muestra su estado real.
                    val message = if (error === PostException.ReviewedByAnotherModerator) DetailMessage.REVIEW_TAKEN_BY_OTHER else DetailMessage.ACTION_ERROR
                    // Quita la bandera y muestra el mensaje.
                    _uiState.update { it.copy(isTakingReview = false, message = message) }
                }
            // Recarga para mostrar el estado actualizado.
            refresh()
        }
    }

    // Abre el diálogo de aprobar.
    fun onApproveClick() = _uiState.update { it.copy(showApproveDialog = true) }

    // Cierra el diálogo de aprobar, salvo que ya se esté aprobando.
    fun onApproveDismiss() = _uiState.update { if (it.isApproving) it else it.copy(showApproveDialog = false) }

    /**
    Aprueba: aparece en el feed y el autor recibe la notificación (lo hace el caso de uso).
    **/
    fun onApproveConfirm() {
        // Evita ejecutar dos veces.
        if (_uiState.value.isApproving) return
        // Marca la aprobación en curso.
        _uiState.update { it.copy(isApproving = true) }
        // Lanza una corrutina.
        viewModelScope.launch {
            // Aprueba la publicación.
            approvePost(postId)
                // Si salió bien, cierra el diálogo y avisa.
                .onSuccess { _uiState.update { it.copy(isApproving = false, showApproveDialog = false, message = DetailMessage.APPROVED) } }
                // Si falló, cierra el diálogo y muestra error.
                .onFailure { _uiState.update { it.copy(isApproving = false, showApproveDialog = false, message = DetailMessage.ACTION_ERROR) } }
            // Recarga el detalle.
            refresh()
        }
    }

    /**
    Moderador: pide (o repite) el análisis con IA de esta publicación.
    **/
    fun onAnalyzeClick() {
        // Evita dobles toques.
        if (_uiState.value.isAnalyzing) return
        // Marca el análisis en curso.
        _uiState.update { it.copy(isAnalyzing = true) }
        // Lanza una corrutina.
        viewModelScope.launch {
            // Ejecuta el análisis y toma el error, si lo hubo.
            val error = analyzePost(postId).exceptionOrNull()
            // Elige el mensaje según el resultado.
            val message = when {
                // Sin error: análisis listo.
                error == null -> DetailMessage.AI_DONE
                // IA sin configurar.
                error === AiException.NotConfigured -> DetailMessage.AI_NOT_CONFIGURED
                // Cualquier otro fallo.
                else -> DetailMessage.AI_FAILED
            }
            // Quita la bandera y muestra el mensaje.
            _uiState.update { it.copy(isAnalyzing = false, message = message) }
            // Recarga para mostrar el nuevo análisis.
            refresh()
        }
    }

    // Abre el diálogo de finalizar.
    fun onFinishClick() = _uiState.update { it.copy(showFinishDialog = true) }

    // Cierra el diálogo de finalizar, salvo que ya se esté finalizando.
    fun onFinishDismiss() = _uiState.update { if (it.isFinishing) it else it.copy(showFinishDialog = false) }

    /**
    Finaliza una verificada: sale del feed y el caso de uso le avisa al autor con una notificación.
    **/
    fun onFinishConfirm() {
        // Obtiene la sesión; sin sesión sale.
        val session = observeSession().value ?: return
        // Evita ejecutar dos veces.
        if (_uiState.value.isFinishing) return
        // Marca la finalización en curso.
        _uiState.update { it.copy(isFinishing = true) }
        // Lanza una corrutina.
        viewModelScope.launch {
            // Finaliza la publicación.
            finishPost(session, postId)
                // Si salió bien, cierra el diálogo y avisa.
                .onSuccess { _uiState.update { it.copy(isFinishing = false, showFinishDialog = false, message = DetailMessage.FINISHED) } }
                // Si falló, cierra el diálogo y muestra error.
                .onFailure { _uiState.update { it.copy(isFinishing = false, showFinishDialog = false, message = DetailMessage.ACTION_ERROR) } }
            // Recarga el detalle.
            refresh()
        }
    }

    // Calcula qué puede hacer el moderador según el estado de la publicación.
    private fun Post.reviewFor(moderatorId: String): ModeratorReview = when {
        // No está en proceso: cerrada.
        status != PostStatus.IN_PROGRESS -> ModeratorReview.CLOSED
        // Sin verificar: disponible para tomar.
        verification.status == VerificationStatus.UNVERIFIED -> ModeratorReview.AVAILABLE
        // En revisión: suya u de otro moderador.
        verification.status == VerificationStatus.IN_PROCESS ->
            if (verification.reviewedBy == moderatorId) ModeratorReview.MINE else ModeratorReview.OTHER
        // Verificada: se puede finalizar.
        verification.status == VerificationStatus.VERIFIED -> ModeratorReview.VERIFIED
        // Cualquier otro caso (rechazada): cerrada.
        else -> ModeratorReview.CLOSED
    }

    // Traduce la publicación del dominio al modelo de la pantalla.
    private fun Post.toUi(categories: List<Category>, types: List<AnimalType>): PostDetailUi {
        // Busca la categoría en el catálogo.
        val category = categories.firstOrNull { it.id == categoryId }
        // Construye el modelo de presentación.
        return PostDetailUi(
            // Id.
            id = id,
            // Fotos.
            photoUrls = photoUrls,
            // Nombre de la categoría.
            categoryName = category?.name.orEmpty(),
            // Verificada.
            isVerified = verification.status == VerificationStatus.VERIFIED,
            // Título.
            title = title,
            // Ciudad.
            city = location.city,
            // Latitud.
            latitude = location.latitude,
            // Longitud.
            longitude = location.longitude,
            // Dirección.
            address = location.address,
            // País.
            country = location.country,
            // Distancia desde la ubicación de quien mira.
            distanceKm = viewer?.location?.distanceKmTo(location),
            // Horas desde la creación (sin negativos).
            ageHours = ((System.currentTimeMillis() - createdAt) / MILLIS_PER_HOUR).toInt().coerceAtLeast(0),
            // Nombre del tipo de animal buscado en el catálogo.
            animalTypeName = animalTypeId?.let { id -> types.firstOrNull { it.id == id }?.name },
            // Raza.
            breed = breed,
            // Tamaño.
            size = size,
            // Vacunado.
            vaccinated = vaccinated,
            // Descripción.
            description = description,
            // Comentarios.
            commentCount = commentCount,
            // Votos de "importante".
            importantCount = importantCount,
            // Visible en el feed público.
            isPublic = showsInPublicFeed(),
            // La categoría acepta solicitudes de adopción.
            allowsAdoption = category?.allowsAdoptionRequests == true,
            // Finalizada.
            isFinished = status == PostStatus.FINISHED,
            // Rechazada.
            isRejected = verification.status == VerificationStatus.REJECTED,
            // Motivo del rechazo (null si está vacío).
            rejectionReason = verification.rejectionReason?.takeIf { it.isNotBlank() },
        )
    }

    // Constantes privadas.
    private companion object {
        // Comentarios que se ven en el detalle antes de "Ver todos".
        const val PREVIEW_SIZE = 3
        // Milisegundos de una hora.
        const val MILLIS_PER_HOUR = 60 * 60 * 1000L
    }
}
