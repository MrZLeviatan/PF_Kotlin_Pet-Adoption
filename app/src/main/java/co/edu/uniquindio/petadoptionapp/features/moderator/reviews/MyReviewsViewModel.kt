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

/**
ViewModel de la pestaña "Publicaciones" del moderador (MyReviewsScreen).
Estado de UI (MyReviewsUiState): publicaciones que el moderador tomó y aún no decide (ya ordenadas por
prioridad), la pestaña elegida y las banderas de carga y error; de ahí se derivan las pestañas por
prioridad (tabs) y las secciones visibles (visibleSections).
Eventos: refresh() cada vez que la pantalla vuelve a primer plano y onTabSelected() al cambiar de pestaña.
Casos de uso: GetMyReviewsUseCase (publicaciones en revisión del moderador), ObserveCategoriesUseCase y
LoadCatalogUseCase (para mostrar el nombre de la categoría).
Navegación: el ViewModel no navega; la pantalla usa el id de cada publicación para abrir su detalle,
donde el moderador la aprueba o la rechaza.
**/

// Publicación en revisión por el moderador, lista para la tarjeta.
data class ReviewPostUi(
    // Identificador de la publicación.
    val id: String,
    // Título de la publicación.
    val title: String,
    // Nombre legible de la categoría.
    val categoryName: String,
    // URL de la primera foto (null si no tiene).
    val photoUrl: String?,
    // Horas desde que se publicó.
    val ageHours: Int,
    // Prioridad de la IA (null = sin analizar) y su explicación.
    val priority: PriorityLevel?,
    // Explicación de la prioridad dada por la IA.
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
    // Publicaciones en revisión, ordenadas por prioridad.
    val posts: List<ReviewPostUi> = emptyList(),
    // Clave de la pestaña elegida (null = "Todas").
    val selectedKey: String? = null,
    // Indica si la lista aún se está cargando.
    val isLoading: Boolean = true,
    // Indica si falló la carga.
    val hasError: Boolean = false,
) {
    // Una pestaña por prioridad presente, de la más urgente a la más baja.
    val tabs: List<ReviewsTab>
        // Agrupa por prioridad (respetando el orden de posts) y cuenta cada grupo.
        get() = posts.groupBy { it.priority }.map { (priority, list) -> ReviewsTab(priority, list.size) }

    /** Secciones de la pestaña actual, una por prioridad (en el mismo orden que las pestañas). */
    val visibleSections: List<Pair<ReviewsTab, List<ReviewPostUi>>>
        // Calcula las secciones a partir de las publicaciones.
        get() = posts
            // Deja todas si la pestaña es "Todas", o solo las de la prioridad elegida.
            .filter { selectedKey == null || (it.priority?.name ?: "NONE") == selectedKey }
            // Agrupa por prioridad.
            .groupBy { it.priority }
            // Convierte cada grupo en un par (pestaña con contador, publicaciones).
            .map { (priority, list) -> ReviewsTab(priority, list.size) to list }
}

/**
 Publicaciones que el moderador tomó para revisar y aún no decide, divididas por prioridad.
 Se recarga al volver a la pantalla (por ejemplo, después de aprobar o rechazar una).
 */
// Hilt crea e inyecta este ViewModel.
@HiltViewModel
class MyReviewsViewModel @Inject constructor(
    // Obtiene las publicaciones en revisión del moderador.
    private val getMyReviews: GetMyReviewsUseCase,
    // Expone las categorías cargadas.
    private val observeCategories: ObserveCategoriesUseCase,
    // Carga el catálogo de categorías.
    private val loadCatalog: LoadCatalogUseCase,
) : ViewModel() {

    // Estado mutable interno.
    private val _uiState = MutableStateFlow(MyReviewsUiState())
    // Estado público de solo lectura que observa la UI.
    val uiState: StateFlow<MyReviewsUiState> = _uiState.asStateFlow()

    // Recarga la lista de publicaciones en revisión.
    fun refresh() {
        // Lanza la carga en el ámbito del ViewModel.
        viewModelScope.launch {
            // Carga el catálogo solo si aún no hay categorías en memoria.
            if (observeCategories().value.isEmpty()) loadCatalog()
            // Pide las publicaciones en revisión del moderador.
            getMyReviews()
                // Si responde, las convierte y actualiza el estado.
                .onSuccess { list ->
                    // Convierte cada publicación al modelo de UI.
                    val posts = list.map { it.toUi() }
                    // Actualiza el estado conservando la pestaña si sigue existiendo.
                    _uiState.update { state ->
                        // Estado con la lista nueva y sin carga ni error.
                        val updated = state.copy(posts = posts, isLoading = false, hasError = false)
                        // Si la pestaña elegida quedó vacía (se decidieron todas), se vuelve a "Todas".
                        updated.copy(selectedKey = state.selectedKey?.takeIf { key -> updated.tabs.any { it.key == key } })
                    }
                }
                // Si falla, quita la carga y marca el error.
                .onFailure { _uiState.update { it.copy(isLoading = false, hasError = true) } }
        }
    }

    // Guarda la pestaña elegida (null = "Todas").
    fun onTabSelected(key: String?) = _uiState.update { it.copy(selectedKey = key) }

    // Convierte una publicación del dominio al modelo de la tarjeta.
    private fun Post.toUi() = ReviewPostUi(
        // Copia el id.
        id = id,
        // Copia el título.
        title = title,
        // Busca el nombre de la categoría (vacío si no se encuentra).
        categoryName = observeCategories().value.firstOrNull { it.id == categoryId }?.name.orEmpty(),
        // Usa la primera foto.
        photoUrl = photoUrls.firstOrNull(),
        // Calcula las horas desde la creación (nunca negativas).
        ageHours = ((System.currentTimeMillis() - createdAt) / 3_600_000L).toInt().coerceAtLeast(0),
        // Toma la prioridad de la IA.
        priority = verification.priorityLevel,
        // Toma la explicación de la prioridad.
        priorityReason = verification.priorityReason,
    )
}
