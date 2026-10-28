package co.edu.uniquindio.petadoptionapp.features.user.likes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.petadoptionapp.domain.model.post.Category
import co.edu.uniquindio.petadoptionapp.domain.model.post.Post
import co.edu.uniquindio.petadoptionapp.domain.usecase.catalog.LoadCatalogUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.catalog.ObserveCategoriesUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.importantVote.GetMyImportantPostsUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.importantVote.ToggleImportantUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Publicación marcada con "Es importante", lista para la tarjeta.
data class LikedPostUi(
    val id: String,
    val title: String,
    val categoryId: String,
    val categoryName: String,
    val photoUrl: String?,
    val ageHours: Int,
    val importantCount: Int,
)

// Pestaña de categoría con su contador ("Adopción (2)").
data class LikesTab(val categoryId: String, val name: String, val count: Int)

/**
 Estado de "Me gusta". [posts] llega en el orden de los votos: el más reciente primero.
 [selectedCategoryId] en `null` es la pestaña "Todas".
 */
data class MyLikesUiState(
    val posts: List<LikedPostUi> = emptyList(),
    val selectedCategoryId: String? = null,
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
    val actionError: Boolean = false,
) {
    // Una pestaña por categoría con al menos un "me gusta", en el orden del más reciente.
    val tabs: List<LikesTab>
        get() = posts.groupBy { it.categoryId }
            .map { (id, list) -> LikesTab(id, list.first().categoryName, list.size) }

    /**
     Secciones de la pestaña actual, agrupadas por categoría. Las categorías y las
     publicaciones dentro de cada una quedan ordenadas del "me gusta" más reciente al más antiguo.
     */
    val visibleSections: List<Pair<LikesTab, List<LikedPostUi>>>
        get() = posts
            .filter { selectedCategoryId == null || it.categoryId == selectedCategoryId }
            .groupBy { it.categoryId }
            .map { (id, list) -> LikesTab(id, list.first().categoryName, list.size) to list }
}

@HiltViewModel
class MyLikesViewModel @Inject constructor(
    private val getMyImportantPosts: GetMyImportantPostsUseCase,
    private val toggleImportant: ToggleImportantUseCase,
    private val observeCategories: ObserveCategoriesUseCase,
    private val loadCatalog: LoadCatalogUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyLikesUiState())
    val uiState: StateFlow<MyLikesUiState> = _uiState.asStateFlow()

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

    /** Recarga la lista; la pantalla lo llama al aparecer (por si se votó en otra pantalla). */
    fun refresh() {
        viewModelScope.launch {
            // El caso de uso pagina de a 20: se piden páginas hasta que no lleguen más.
            val all = mutableListOf<Post>()
            do {
                val page = getMyImportantPosts(afterPostId = all.lastOrNull()?.id).getOrElse {
                    _uiState.update { s -> s.copy(isLoading = false, hasError = true) }
                    return@launch
                }
                all += page
            } while (page.isNotEmpty())

            lastPosts = all
            val categories = observeCategories().value
            _uiState.update { state ->
                val posts = all.map { it.toUi(categories) }
                // Si la categoría elegida quedó vacía, se vuelve a "Todas".
                val selected = state.selectedCategoryId?.takeIf { id -> posts.any { it.categoryId == id } }
                state.copy(posts = posts, selectedCategoryId = selected, isLoading = false, hasError = false)
            }
        }
    }

    fun onTabSelected(categoryId: String?) = _uiState.update { it.copy(selectedCategoryId = categoryId) }

    // Quita el "me gusta": sale de la lista al instante y vuelve si no se pudo guardar.
    fun onRemoveLike(postId: String) {
        val previous = _uiState.value
        _uiState.update { state ->
            val posts = state.posts.filterNot { it.id == postId }
            state.copy(
                posts = posts,
                selectedCategoryId = state.selectedCategoryId?.takeIf { id -> posts.any { it.categoryId == id } },
                actionError = false,
            )
        }
        viewModelScope.launch {
            toggleImportant(postId)
                .onSuccess { voted ->
                    // Si quedó votada (el voto ya se había quitado en otra pantalla), se recarga para quedar al día.
                    if (voted) refresh() else lastPosts = lastPosts.filterNot { it.id == postId }
                }
                .onFailure { _uiState.value = previous.copy(actionError = true) }
        }
    }

    private fun Post.toUi(categories: List<Category>) = LikedPostUi(
        id = id,
        title = title,
        categoryId = categoryId,
        categoryName = categories.firstOrNull { it.id == categoryId }?.name.orEmpty(),
        photoUrl = photoUrls.firstOrNull(),
        ageHours = ((System.currentTimeMillis() - createdAt) / MILLIS_PER_HOUR).toInt().coerceAtLeast(0),
        importantCount = importantCount,
    )

    private companion object {
        const val MILLIS_PER_HOUR = 60 * 60 * 1000L
    }
}
