package co.edu.uniquindio.petadoptionapp.features.user.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.petadoptionapp.core.component.map.GeoPoint
import co.edu.uniquindio.petadoptionapp.core.util.approximatePoint
import co.edu.uniquindio.petadoptionapp.domain.model.location.City
import co.edu.uniquindio.petadoptionapp.domain.model.location.CityCatalog
import co.edu.uniquindio.petadoptionapp.domain.model.location.Location
import co.edu.uniquindio.petadoptionapp.domain.model.post.AnimalSize
import co.edu.uniquindio.petadoptionapp.domain.model.post.AnimalType
import co.edu.uniquindio.petadoptionapp.domain.model.post.Category
import co.edu.uniquindio.petadoptionapp.domain.model.post.Post
import co.edu.uniquindio.petadoptionapp.domain.model.post.filter.PostFilter
import co.edu.uniquindio.petadoptionapp.domain.usecase.catalog.LoadCatalogUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.catalog.ObserveAnimalTypesUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.catalog.ObserveCategoriesUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.location.SearchAddressUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.GetFeedUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.ObserveCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Filtros de Explorar (pantalla "Filtros"). [city] null = alrededor de la persona. */
data class ExploreFilters(
    val categoryId: String? = null,
    val animalTypeId: String? = null,
    val size: AnimalSize? = null,
    val vaccinatedOnly: Boolean = false,
    val city: City? = null,
    val radiusKm: Int = DEFAULT_RADIUS_KM,
) {
    // Cuántos filtros cambió la persona (para el contador del botón "Filtros").
    val activeCount: Int
        get() = listOf(categoryId != null, animalTypeId != null, size != null, vaccinatedOnly, city != null, radiusKm != DEFAULT_RADIUS_KM)
            .count { it }

    companion object {
        const val DEFAULT_RADIUS_KM = 10
        const val MAX_RADIUS_KM = 50
    }
}

/** Publicación en el mapa (en su punto aproximado) y en el resumen de abajo. */
data class ExplorePostUi(
    val id: String,
    val title: String,
    val imageUrl: String?,
    val categoryId: String,
    val categoryName: String,
    val animalName: String?,
    val breed: String,
    val size: AnimalSize?,
    val importantCount: Int,
    val distanceKm: Double?,
    val point: GeoPoint,
)

data class ExploreUiState(
    // Centro de la búsqueda: la ubicación de la persona, una ciudad o una dirección buscada.
    val center: GeoPoint = CityCatalog.all.first().let { GeoPoint(it.latitude, it.longitude) },
    val userLocation: GeoPoint? = null,
    val isLocating: Boolean = true,
    val posts: List<ExplorePostUi> = emptyList(),
    val selectedPostId: String? = null,
    val isLoading: Boolean = false,
    // Buscador "Buscar ubicación o dirección".
    val query: String = "",
    val suggestions: List<Location> = emptyList(),
    // Filtros aplicados y, mientras está abierta la pantalla de filtros, el borrador y cuántas daría.
    val filters: ExploreFilters = ExploreFilters(),
    val showFilters: Boolean = false,
    val draft: ExploreFilters = ExploreFilters(),
    val draftCount: Int = 0,
    val categories: List<Category> = emptyList(),
    val animalTypes: List<AnimalType> = emptyList(),
) {
    val selectedPost: ExplorePostUi? get() = posts.firstOrNull { it.id == selectedPostId }
}

/**
 Explorar: publicaciones cercanas a la persona (ubicación actual; si no hay, la de su perfil) en el mapa,
 con buscador de lugares, filtros (categoría, tipo de animal, tamaño, vacunas, ciudad y distancia)
 y el resumen de la publicación tocada. Cada publicación se ubica en su punto aproximado.
 */
@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val getFeed: GetFeedUseCase,
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    private val observeCategories: ObserveCategoriesUseCase,
    private val observeAnimalTypes: ObserveAnimalTypesUseCase,
    private val loadCatalog: LoadCatalogUseCase,
    private val searchAddress: SearchAddressUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var countJob: Job? = null

    init {
        viewModelScope.launch {
            if (observeCategories().value.isEmpty()) loadCatalog()
            combine(observeCategories(), observeAnimalTypes()) { c, t -> c.filter { it.active } to t.filter { it.active } }
                .collect { (c, t) -> _uiState.update { it.copy(categories = c, animalTypes = t) } }
        }
    }

    /** Resultado de pedir la ubicación actual; si no hay, se usa la del perfil (o la primera ciudad). */
    fun onCurrentLocation(point: GeoPoint?) {
        viewModelScope.launch {
            val fallback = observeCurrentUser().first()?.location?.takeIf { it.hasCoordinates() }
                ?.let { GeoPoint(it.latitude!!, it.longitude!!) }
            val center = point ?: fallback ?: _uiState.value.center
            _uiState.update { it.copy(userLocation = point, center = center, isLocating = false) }
            load()
        }
    }

    fun onRecenter(point: GeoPoint?) {
        if (point == null) return
        _uiState.update { it.copy(userLocation = point, center = point, filters = it.filters.copy(city = null)) }
        load()
    }

    // --- Mapa y resumen ---

    fun onMarkerClick(postId: String) = _uiState.update { it.copy(selectedPostId = postId) }

    fun onMapClick() = _uiState.update { it.copy(selectedPostId = null) }

    // "Todos": quita los filtros y vuelve a mostrar todo alrededor.
    fun onClearFiltersClick() {
        _uiState.update { it.copy(filters = ExploreFilters(), selectedPostId = null) }
        load()
    }

    // --- Buscador de lugares ---

    fun onQueryChange(value: String) {
        _uiState.update { it.copy(query = value) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            val city = _uiState.value.filters.city?.toLocation()
            _uiState.update { it.copy(suggestions = searchAddress(value, city).getOrDefault(emptyList())) }
        }
    }

    // Ir a un lugar buscado: el mapa y la búsqueda de cercanía se centran ahí.
    fun onSuggestionClick(location: Location) {
        val lat = location.latitude ?: return
        val lng = location.longitude ?: return
        _uiState.update { it.copy(center = GeoPoint(lat, lng), query = location.address, suggestions = emptyList(), selectedPostId = null) }
        load()
    }

    // --- Pantalla de filtros (borrador hasta "Aplicar") ---

    fun onOpenFilters() {
        _uiState.update { it.copy(showFilters = true, draft = it.filters) }
        countDraft()
    }

    fun onCloseFilters() = _uiState.update { it.copy(showFilters = false) }

    fun onDraftChange(change: (ExploreFilters) -> ExploreFilters) {
        _uiState.update { it.copy(draft = change(it.draft)) }
        countDraft()
    }

    fun onClearDraft() = onDraftChange { ExploreFilters() }

    fun onApplyFilters() {
        val draft = _uiState.value.draft
        _uiState.update { state ->
            state.copy(
                filters = draft,
                showFilters = false,
                selectedPostId = null,
                // Con una ciudad elegida, el mapa va a ella; sin ciudad, vuelve a la persona.
                center = draft.city?.let { GeoPoint(it.latitude, it.longitude) } ?: state.userLocation ?: state.center,
            )
        }
        load()
    }

    // --- Carga ---

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val state = _uiState.value
            val posts = fetch(state.filters, state.center)
            _uiState.update { s ->
                s.copy(
                    isLoading = false,
                    posts = posts.map { it.toUi(s.center) },
                    selectedPostId = s.selectedPostId?.takeIf { id -> posts.any { it.id == id } },
                )
            }
        }
    }

    // "Hay N mascotas encontradas": se recalcula con el borrador (espera a que deje de tocar).
    private fun countDraft() {
        countJob?.cancel()
        countJob = viewModelScope.launch {
            delay(250)
            val state = _uiState.value
            val center = state.draft.city?.let { GeoPoint(it.latitude, it.longitude) } ?: state.userLocation ?: state.center
            _uiState.update { it.copy(draftCount = fetch(state.draft, center).size) }
        }
    }

    // Pide páginas del feed hasta que no lleguen más (con un tope, por la cuota de lecturas).
    private suspend fun fetch(filters: ExploreFilters, center: GeoPoint): List<Post> {
        val filter = PostFilter(
            categoryId = filters.categoryId,
            animalTypeId = filters.animalTypeId,
            center = Location(center.latitude, center.longitude),
            radiusKm = filters.radiusKm.toDouble(),
            size = filters.size,
            vaccinatedOnly = filters.vaccinatedOnly,
        )
        val all = mutableListOf<Post>()
        repeat(MAX_PAGES) {
            val page = getFeed(filter, afterPostId = all.lastOrNull()?.id).getOrDefault(emptyList())
            all += page
            if (page.size < GetFeedUseCase.PAGE_SIZE) return all
        }
        return all
    }

    private fun Post.toUi(center: GeoPoint): ExplorePostUi {
        val point = approximatePoint(location.latitude ?: center.latitude, location.longitude ?: center.longitude, id)
        return ExplorePostUi(
            id = id,
            title = title,
            imageUrl = photoUrls.firstOrNull(),
            categoryId = categoryId,
            categoryName = observeCategories().value.firstOrNull { it.id == categoryId }?.name.orEmpty(),
            animalName = observeAnimalTypes().value.firstOrNull { it.id == animalTypeId }?.name,
            breed = breed,
            size = size,
            importantCount = importantCount,
            distanceKm = location.distanceKmTo(Location(center.latitude, center.longitude))?.let { Math.round(it * 10) / 10.0 },
            point = point,
        )
    }

    private companion object {
        const val MAX_PAGES = 5
    }
}
