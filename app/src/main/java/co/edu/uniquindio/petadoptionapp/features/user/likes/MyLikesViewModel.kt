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

/**
ViewModel de la pantalla "Me gusta" (Mis likes), a la que se llega desde Mi Perfil.
Obtiene todas las publicaciones que la persona marcó como "Es importante" con
GetMyImportantPostsUseCase (paginando hasta el final) y traduce el id de categoría a su
nombre con el catálogo (ObserveCategoriesUseCase / LoadCatalogUseCase). Expone en
MyLikesUiState la lista, la pestaña de categoría seleccionada y las banderas de carga y error.
Permite quitar un "me gusta" con ToggleImportantUseCase de forma optimista (se revierte si
falla). No emite eventos de navegación: la pantalla recibe onBack y onPostClick desde
AppNavigation y llama a refresh() cada vez que vuelve a primer plano.
**/

// Publicación marcada con "Es importante", lista para la tarjeta.
data class LikedPostUi(
    // Identificador de la publicación.
    val id: String,
    // Título de la publicación.
    val title: String,
    // Id de la categoría (para agrupar y filtrar).
    val categoryId: String,
    // Nombre de la categoría resuelto desde el catálogo.
    val categoryName: String,
    // Primera foto, si tiene.
    val photoUrl: String?,
    // Horas desde que se publicó.
    val ageHours: Int,
    // Número de votos "Es importante".
    val importantCount: Int,
)

// Pestaña de categoría con su contador ("Adopción (2)").
data class LikesTab(val categoryId: String, val name: String, val count: Int)

/**
Estado de "Me gusta". [posts] llega en el orden de los votos: el más reciente primero.
[selectedCategoryId] en `null` es la pestaña "Todas".
Las pestañas y las secciones visibles se calculan a partir de la lista.
**/
data class MyLikesUiState(
    // Publicaciones con "me gusta", de la más reciente a la más antigua.
    val posts: List<LikedPostUi> = emptyList(),
    // Categoría de la pestaña elegida; null es "Todas".
    val selectedCategoryId: String? = null,
    // Indica que la lista se está cargando.
    val isLoading: Boolean = true,
    // Indica que falló la carga de la lista.
    val hasError: Boolean = false,
    // Indica que falló quitar un "me gusta".
    val actionError: Boolean = false,
) {
    // Una pestaña por categoría con al menos un "me gusta", en el orden del más reciente.
    val tabs: List<LikesTab>
        // Agrupa por categoría (groupBy conserva el orden de aparición) y cuenta cada grupo.
        get() = posts.groupBy { it.categoryId }
            .map { (id, list) -> LikesTab(id, list.first().categoryName, list.size) }

    /**
    Secciones de la pestaña actual, agrupadas por categoría. Las categorías y las
    publicaciones dentro de cada una quedan ordenadas del "me gusta" más reciente al más antiguo.
    **/
    val visibleSections: List<Pair<LikesTab, List<LikedPostUi>>>
        get() = posts
            // Deja solo las de la categoría elegida (o todas si es null).
            .filter { selectedCategoryId == null || it.categoryId == selectedCategoryId }
            // Agrupa por categoría.
            .groupBy { it.categoryId }
            // Empareja cada encabezado de categoría con sus publicaciones.
            .map { (id, list) -> LikesTab(id, list.first().categoryName, list.size) to list }
}

// Marca la clase para que Hilt la construya e inyecte sus dependencias.
@HiltViewModel
class MyLikesViewModel @Inject constructor(
    // Obtiene, paginadas, las publicaciones votadas por la persona.
    private val getMyImportantPosts: GetMyImportantPostsUseCase,
    // Pone o quita el voto "Es importante".
    private val toggleImportant: ToggleImportantUseCase,
    // Catálogo de categorías en memoria.
    private val observeCategories: ObserveCategoriesUseCase,
    // Carga el catálogo de categorías.
    private val loadCatalog: LoadCatalogUseCase,
) : ViewModel() {

    // Estado mutable interno.
    private val _uiState = MutableStateFlow(MyLikesUiState())
    // Estado público de solo lectura.
    val uiState: StateFlow<MyLikesUiState> = _uiState.asStateFlow()

    // Últimas publicaciones leídas: se vuelven a traducir si cambia el catálogo.
    private var lastPosts: List<Post> = emptyList()

    // Bloque de inicialización: observa el catálogo de categorías.
    init {
        // El nombre de cada categoría sale del catálogo; si llega después, las etiquetas se actualizan.
        viewModelScope.launch {
            // Carga el catálogo solo si aún no está en memoria.
            if (observeCategories().value.isEmpty()) loadCatalog()
            // Cada vez que cambia el catálogo, vuelve a traducir las publicaciones.
            observeCategories().collect { categories ->
                _uiState.update { it.copy(posts = lastPosts.map { post -> post.toUi(categories) }) }
            }
        }
    }

    /** Recarga la lista; la pantalla lo llama al aparecer (por si se votó en otra pantalla). **/
    fun refresh() {
        // Ejecuta la carga en una corrutina del ViewModel.
        viewModelScope.launch {
            // El caso de uso pagina de a 20: se piden páginas hasta que no lleguen más.
            val all = mutableListOf<Post>()
            // Repite mientras lleguen páginas con datos.
            do {
                // Pide la página siguiente a la última publicación ya leída; si falla, marca el error y termina.
                val page = getMyImportantPosts(afterPostId = all.lastOrNull()?.id).getOrElse {
                    _uiState.update { s -> s.copy(isLoading = false, hasError = true) }
                    return@launch
                }
                // Acumula la página.
                all += page
            } while (page.isNotEmpty())

            // Guarda la lista cruda para futuras traducciones del catálogo.
            lastPosts = all
            // Toma el catálogo actual de categorías.
            val categories = observeCategories().value
            // Actualiza el estado con la lista traducida.
            _uiState.update { state ->
                // Convierte cada publicación al modelo de UI.
                val posts = all.map { it.toUi(categories) }
                // Si la categoría elegida quedó vacía, se vuelve a "Todas".
                val selected = state.selectedCategoryId?.takeIf { id -> posts.any { it.categoryId == id } }
                // Publica la lista y termina la carga.
                state.copy(posts = posts, selectedCategoryId = selected, isLoading = false, hasError = false)
            }
        }
    }

    // Cambia la pestaña seleccionada (null es "Todas").
    fun onTabSelected(categoryId: String?) = _uiState.update { it.copy(selectedCategoryId = categoryId) }

    // Quita el "me gusta": sale de la lista al instante y vuelve si no se pudo guardar.
    fun onRemoveLike(postId: String) {
        // Guarda el estado previo para poder revertir.
        val previous = _uiState.value
        // Quita la publicación de la lista de inmediato.
        _uiState.update { state ->
            // Lista sin la publicación quitada.
            val posts = state.posts.filterNot { it.id == postId }
            state.copy(
                posts = posts,
                // Si la pestaña elegida quedó vacía, vuelve a "Todas".
                selectedCategoryId = state.selectedCategoryId?.takeIf { id -> posts.any { it.categoryId == id } },
                // Limpia el error de una acción anterior.
                actionError = false,
            )
        }
        // Persiste el cambio del voto.
        viewModelScope.launch {
            toggleImportant(postId)
                // voted indica si, tras alternar, la publicación quedó votada.
                .onSuccess { voted ->
                    // Si quedó votada (el voto ya se había quitado en otra pantalla), se recarga para quedar al día.
                    if (voted) refresh() else lastPosts = lastPosts.filterNot { it.id == postId }
                }
                // Si falla, restaura el estado previo y muestra el error.
                .onFailure { _uiState.value = previous.copy(actionError = true) }
        }
    }

    // Convierte una publicación del dominio en el modelo de la tarjeta.
    private fun Post.toUi(categories: List<Category>) = LikedPostUi(
        id = id,
        title = title,
        categoryId = categoryId,
        // Nombre de la categoría desde el catálogo (vacío si aún no está).
        categoryName = categories.firstOrNull { it.id == categoryId }?.name.orEmpty(),
        // Primera foto como portada.
        photoUrl = photoUrls.firstOrNull(),
        // Horas desde la creación, nunca negativas.
        ageHours = ((System.currentTimeMillis() - createdAt) / MILLIS_PER_HOUR).toInt().coerceAtLeast(0),
        importantCount = importantCount,
    )

    // Constantes privadas del ViewModel.
    private companion object {
        // Milisegundos que tiene una hora.
        const val MILLIS_PER_HOUR = 60 * 60 * 1000L
    }
}
