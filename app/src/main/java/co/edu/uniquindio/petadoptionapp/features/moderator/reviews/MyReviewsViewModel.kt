package co.edu.uniquindio.petadoptionapp.features.moderator.reviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.petadoptionapp.domain.model.post.Post
import co.edu.uniquindio.petadoptionapp.domain.model.post.PriorityLevel
import co.edu.uniquindio.petadoptionapp.domain.usecase.catalog.LoadCatalogUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.catalog.ObserveCategoriesUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.GetMyReviewsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Publicación en revisión por el moderador, lista para la tarjeta.
data class ReviewPostUi(
    val id: String,
    val title: String,
    val categoryName: String,
    val photoUrl: String?,
    val ageHours: Int,
    // Prioridad de la IA (null = sin analizar) y su explicación.
    val priority: PriorityLevel?,
    val priorityReason: String,
)

// Pestaña de prioridad con su contador ("Urgente (1)"). [priority] null = "Sin prioridad".
data class ReviewsTab(val priority: PriorityLevel?, val count: Int) {
    // Identifica la pestaña aunque cambie el contador; "NONE" para las que no tienen prioridad.
    val key: String get() = priority?.name ?: "NONE"
}

/**
 Estado de "Publicaciones" del moderador. [posts] ya viene ordenado: urgentes primero.
 [selectedKey] (el [ReviewsTab.key]) en `null` es la pestaña "Todas".
 */
data class MyReviewsUiState(
    val posts: List<ReviewPostUi> = emptyList(),
    val selectedKey: String? = null,
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
) {
    // Una pestaña por prioridad presente, de la más urgente a la más baja.
    val tabs: List<ReviewsTab>
        get() = posts.groupBy { it.priority }.map { (priority, list) -> ReviewsTab(priority, list.size) }

    /** Secciones de la pestaña actual, una por prioridad (en el mismo orden que las pestañas). */
    val visibleSections: List<Pair<ReviewsTab, List<ReviewPostUi>>>
        get() = posts
            .filter { selectedKey == null || (it.priority?.name ?: "NONE") == selectedKey }
            .groupBy { it.priority }
            .map { (priority, list) -> ReviewsTab(priority, list.size) to list }
}

/**
 Publicaciones que el moderador tomó para revisar y aún no decide, divididas por prioridad.
 Se recarga al volver a la pantalla (por ejemplo, después de aprobar o rechazar una).
 */
@HiltViewModel
class MyReviewsViewModel @Inject constructor(
    private val getMyReviews: GetMyReviewsUseCase,
    private val observeCategories: ObserveCategoriesUseCase,
    private val loadCatalog: LoadCatalogUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyReviewsUiState())
    val uiState: StateFlow<MyReviewsUiState> = _uiState.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            if (observeCategories().value.isEmpty()) loadCatalog()
            getMyReviews()
                .onSuccess { list ->
                    val posts = list.map { it.toUi() }
                    _uiState.update { state ->
                        val updated = state.copy(posts = posts, isLoading = false, hasError = false)
                        // Si la pestaña elegida quedó vacía (se decidieron todas), se vuelve a "Todas".
                        updated.copy(selectedKey = state.selectedKey?.takeIf { key -> updated.tabs.any { it.key == key } })
                    }
                }
                .onFailure { _uiState.update { it.copy(isLoading = false, hasError = true) } }
        }
    }

    fun onTabSelected(key: String?) = _uiState.update { it.copy(selectedKey = key) }

    private fun Post.toUi() = ReviewPostUi(
        id = id,
        title = title,
        categoryName = observeCategories().value.firstOrNull { it.id == categoryId }?.name.orEmpty(),
        photoUrl = photoUrls.firstOrNull(),
        ageHours = ((System.currentTimeMillis() - createdAt) / 3_600_000L).toInt().coerceAtLeast(0),
        priority = verification.priorityLevel,
        priorityReason = verification.priorityReason,
    )
}
