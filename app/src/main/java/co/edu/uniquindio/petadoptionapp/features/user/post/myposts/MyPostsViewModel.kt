package co.edu.uniquindio.petadoptionapp.features.user.myposts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.petadoptionapp.domain.model.post.Category
import co.edu.uniquindio.petadoptionapp.domain.model.post.Post
import co.edu.uniquindio.petadoptionapp.domain.model.post.PostStatus
import co.edu.uniquindio.petadoptionapp.domain.model.post.VerificationStatus
import co.edu.uniquindio.petadoptionapp.domain.usecase.auth.ObserveSessionUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.catalog.LoadCatalogUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.catalog.ObserveCategoriesUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.DeletePostUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.FinishPostUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.GetMyPostsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Sufijo "Ui" para no chocar con modelos del dominio que tengan el mismo nombre.
// El orden define el de las secciones: Activas → Pendientes → Rechazadas → Finalizadas.
enum class PostStatusUi { ACTIVE, PENDING, REJECTED, FINISHED }
enum class PostFilter { ALL, ACTIVE, PENDING, FINISHED }

data class MyPostUi(
    val id: String,
    val categoryName: String,
    val status: PostStatusUi,
    val title: String,
    val photoUrl: String?,
    // Antigüedad en horas: la pantalla la convierte a "hace X horas/días".
    val ageHours: Int,
    val interestedCount: Int = 0,
    val rejectionReason: String? = null,
)

// Acción que espera confirmación en el diálogo.
sealed interface PendingPostAction {
    val postId: String
    data class Finish(override val postId: String) : PendingPostAction
    data class Delete(override val postId: String) : PendingPostAction
}

data class MyPostsUiState(
    val filter: PostFilter = PostFilter.ALL,
    val posts: List<MyPostUi> = emptyList(),
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
    // Diálogo abierto (null = cerrado) y si la acción confirmada está en curso.
    val pendingAction: PendingPostAction? = null,
    val isProcessing: Boolean = false,
    val actionError: Boolean = false,
) {
    /** Cantidad de publicaciones de cada pestaña. */
    fun countOf(filter: PostFilter): Int = posts.count { it.matches(filter) }

    /** Publicaciones de la pestaña actual, agrupadas por estado y en el orden del enum. */
    val visibleSections: Map<PostStatusUi, List<MyPostUi>>
        get() = posts
            .filter { it.matches(filter) }
            .groupBy { it.status }
            .toSortedMap()
}

// "Todas" incluye todas las publicaciones, también las finalizadas (van en su propia sección al final).
private fun MyPostUi.matches(filter: PostFilter): Boolean = when (filter) {
    PostFilter.ALL -> true
    PostFilter.ACTIVE -> status == PostStatusUi.ACTIVE
    PostFilter.PENDING -> status == PostStatusUi.PENDING
    PostFilter.FINISHED -> status == PostStatusUi.FINISHED
}

@HiltViewModel
class MyPostsViewModel @Inject constructor(
    private val observeSession: ObserveSessionUseCase,
    private val getMyPosts: GetMyPostsUseCase,
    private val finishPost: FinishPostUseCase,
    private val deletePost: DeletePostUseCase,
    private val observeCategories: ObserveCategoriesUseCase,
    private val loadCatalog: LoadCatalogUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyPostsUiState())
    val uiState: StateFlow<MyPostsUiState> = _uiState.asStateFlow()

    // Últimas publicaciones leídas: se vuelven a traducir si cambia el catálogo.
    private var lastPosts: List<Post> = emptyList()

    init {
        // El nombre de cada categoría sale del catálogo; si llega después, las etiquetas se actualizan.
        viewModelScope.launch {
            if (observeCategories().value.isEmpty()) loadCatalog()
            observeCategories().collect { categories ->
                _uiState.update { it.copy(posts = lastPosts.map { post -> post.toUi(categories) }) }
            }
        }
    }

    /** Recarga la lista; la pantalla lo llama al aparecer (por si se creó o cambió algo). */
    fun refresh() {
        val session = observeSession().value ?: return
        viewModelScope.launch {
            getMyPosts(session.id)
                .onSuccess { posts ->
                    lastPosts = posts
                    val categories = observeCategories().value
                    _uiState.update {
                        it.copy(posts = posts.map { post -> post.toUi(categories) }, isLoading = false, hasError = false)
                    }
                }
                .onFailure { _uiState.update { it.copy(isLoading = false, hasError = true) } }
        }
    }

    fun onFilterSelected(filter: PostFilter) = _uiState.update { it.copy(filter = filter) }

    // Abren el diálogo; la acción real ocurre en onConfirmAction.
    fun onFinishPost(postId: String) =
        _uiState.update { it.copy(pendingAction = PendingPostAction.Finish(postId), actionError = false) }

    fun onDeletePost(postId: String) =
        _uiState.update { it.copy(pendingAction = PendingPostAction.Delete(postId), actionError = false) }

    fun onDismissAction() = _uiState.update { if (it.isProcessing) it else it.copy(pendingAction = null) }

    fun onConfirmAction() {
        val state = _uiState.value
        val action = state.pendingAction ?: return
        if (state.isProcessing) return
        val session = observeSession().value ?: return

        _uiState.update { it.copy(isProcessing = true) }
        viewModelScope.launch {
            val result = when (action) {
                is PendingPostAction.Finish -> finishPost(session, action.postId)
                is PendingPostAction.Delete -> deletePost(session.id, action.postId)
            }
            _uiState.update {
                it.copy(isProcessing = false, pendingAction = null, actionError = result.isFailure)
            }
            if (result.isSuccess) refresh()
        }
    }

    private fun Post.toUi(categories: List<Category>): MyPostUi {
        val ageHours = ((System.currentTimeMillis() - createdAt) / MILLIS_PER_HOUR).toInt().coerceAtLeast(0)
        return MyPostUi(
            id = id,
            categoryName = categories.firstOrNull { it.id == categoryId }?.name.orEmpty(),
            status = statusUi(),
            title = title,
            photoUrl = photoUrls.firstOrNull(),
            ageHours = ageHours,
            interestedCount = importantCount,
            rejectionReason = verification.rejectionReason,
        )
    }

    // Une el ciclo de vida y la verificación en el estado que ve el autor.
    private fun Post.statusUi(): PostStatusUi = when {
        status == PostStatus.FINISHED -> PostStatusUi.FINISHED
        verification.status == VerificationStatus.VERIFIED -> PostStatusUi.ACTIVE
        verification.status == VerificationStatus.REJECTED -> PostStatusUi.REJECTED
        else -> PostStatusUi.PENDING
    }

    private companion object {
        const val MILLIS_PER_HOUR = 60 * 60 * 1000L
    }
}
