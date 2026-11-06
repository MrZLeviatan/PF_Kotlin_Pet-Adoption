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

/**
ViewModel de la pantalla Explorar: muestra en el mapa las publicaciones cercanas a la persona
(ubicación actual del dispositivo; si no hay, la de su perfil; y si tampoco, la primera ciudad del
catálogo). Incluye un buscador de lugares o direcciones, una pantalla de filtros (categoría, tipo de
animal, tamaño, vacunas, ciudad y distancia) que trabaja sobre un borrador hasta pulsar "Aplicar",
y el resumen de la publicación tocada. Cada publicación se ubica en un punto aproximado para no
revelar la dirección exacta.
Estado (ExploreUiState): centro del mapa, ubicación del usuario, publicaciones, publicación
seleccionada, carga, búsqueda y sugerencias, filtros aplicados, borrador de filtros con su conteo y
catálogos de categorías y tipos de animal.
Eventos: onCurrentLocation, onRecenter, onMarkerClick, onMapClick, onClearFiltersClick, onQueryChange,
onSuggestionClick, onOpenFilters, onCloseFilters, onDraftChange, onClearDraft y onApplyFilters.
Casos de uso: GetFeedUseCase, ObserveCurrentUserUseCase, ObserveCategoriesUseCase,
ObserveAnimalTypesUseCase, LoadCatalogUseCase y SearchAddressUseCase.
Navegación: el ViewModel no navega; ExploreScreen avisa con onPostClick para abrir el detalle.
**/

// Filtros de Explorar (pantalla "Filtros"). [city] null = alrededor de la persona.
data class ExploreFilters(
    // Categoría elegida (adopción, perdido, etc.); null = todas.
    val categoryId: String? = null,
    // Tipo de animal elegido; null = todos.
    val animalTypeId: String? = null,
    // Tamaño del animal; null = cualquiera.
    val size: AnimalSize? = null,
    // Si es true, solo muestra animales vacunados.
    val vaccinatedOnly: Boolean = false,
    // Ciudad donde buscar; null = alrededor de la persona.
    val city: City? = null,
    // Radio de búsqueda en kilómetros.
    val radiusKm: Int = DEFAULT_RADIUS_KM,
) {
    // Cuántos filtros cambió la persona (para el contador del botón "Filtros").
    val activeCount: Int
        // Arma una lista de condiciones (una por filtro distinto del valor por defecto) y cuenta las verdaderas.
        get() = listOf(categoryId != null, animalTypeId != null, size != null, vaccinatedOnly, city != null, radiusKm != DEFAULT_RADIUS_KM)
            .count { it }

    // Constantes compartidas de los filtros.
    companion object {
        // Radio de búsqueda por defecto en kilómetros.
        const val DEFAULT_RADIUS_KM = 10
        // Radio máximo permitido en el control deslizante.
        const val MAX_RADIUS_KM = 50
    }
}

// Publicación en el mapa (en su punto aproximado) y en el resumen de abajo.
data class ExplorePostUi(
    // Identificador de la publicación.
    val id: String,
    // Título de la publicación.
    val title: String,
    // Primera foto de la publicación, si tiene.
    val imageUrl: String?,
    // Identificador de la categoría (define el color del marcador).
    val categoryId: String,
    // Nombre visible de la categoría.
    val categoryName: String,
    // Nombre del tipo de animal (perro, gato…), si se conoce.
    val animalName: String?,
    // Raza del animal.
    val breed: String,
    // Tamaño del animal.
    val size: AnimalSize?,
    // Cantidad de marcas de "importante".
    val importantCount: Int,
    // Distancia al centro de búsqueda en km (con un decimal), si se puede calcular.
    val distanceKm: Double?,
    // Punto aproximado donde se dibuja el marcador.
    val point: GeoPoint,
)

// Estado completo de la pantalla Explorar.
data class ExploreUiState(
    // Centro de la búsqueda: la ubicación de la persona, una ciudad o una dirección buscada (por defecto la primera ciudad).
    val center: GeoPoint = CityCatalog.all.first().let { GeoPoint(it.latitude, it.longitude) },
    // Ubicación actual del dispositivo, si se obtuvo.
    val userLocation: GeoPoint? = null,
    // Indica que todavía se está pidiendo la ubicación.
    val isLocating: Boolean = true,
    // Publicaciones a dibujar en el mapa.
    val posts: List<ExplorePostUi> = emptyList(),
    // Publicación tocada en el mapa; null = sin resumen.
    val selectedPostId: String? = null,
    // Indica si se están cargando publicaciones.
    val isLoading: Boolean = false,
    // Texto del buscador "Buscar ubicación o dirección".
    val query: String = "",
    // Sugerencias de lugares para el texto buscado.
    val suggestions: List<Location> = emptyList(),
    // Filtros aplicados actualmente.
    val filters: ExploreFilters = ExploreFilters(),
    // Indica si la pantalla de filtros está abierta.
    val showFilters: Boolean = false,
    // Borrador de filtros mientras la pantalla de filtros está abierta.
    val draft: ExploreFilters = ExploreFilters(),
    // Cuántas publicaciones daría el borrador.
    val draftCount: Int = 0,
    // Categorías activas del catálogo.
    val categories: List<Category> = emptyList(),
    // Tipos de animal activos del catálogo.
    val animalTypes: List<AnimalType> = emptyList(),
) {
    // Publicación seleccionada (o null si no hay o ya no está en la lista).
    val selectedPost: ExplorePostUi? get() = posts.firstOrNull { it.id == selectedPostId }
}

// ViewModel de Explorar, inyectado por Hilt con sus casos de uso.
@HiltViewModel
class ExploreViewModel @Inject constructor(
    // Obtiene páginas del feed de publicaciones según un filtro.
    private val getFeed: GetFeedUseCase,
    // Observa el usuario actual (para la ubicación de su perfil).
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    // Observa el catálogo de categorías.
    private val observeCategories: ObserveCategoriesUseCase,
    // Observa el catálogo de tipos de animal.
    private val observeAnimalTypes: ObserveAnimalTypesUseCase,
    // Carga los catálogos desde el servidor.
    private val loadCatalog: LoadCatalogUseCase,
    // Busca direcciones o lugares por texto.
    private val searchAddress: SearchAddressUseCase,
) : ViewModel() {

    // Estado mutable interno de la pantalla.
    private val _uiState = MutableStateFlow(ExploreUiState())
    // Estado de solo lectura que observa la UI.
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    // Tarea de búsqueda de lugares en curso (se cancela al escribir de nuevo).
    private var searchJob: Job? = null
    // Tarea de conteo del borrador de filtros en curso (se cancela al cambiar el borrador).
    private var countJob: Job? = null

    // Al crearse, carga y observa los catálogos de categorías y tipos de animal.
    init {
        // Corrutina ligada al ciclo de vida del ViewModel.
        viewModelScope.launch {
            // Si el catálogo aún no se ha cargado, lo pide.
            if (observeCategories().value.isEmpty()) loadCatalog()
            // Combina ambos catálogos y se queda solo con los elementos activos.
            combine(observeCategories(), observeAnimalTypes()) { c, t -> c.filter { it.active } to t.filter { it.active } }
                // Publica los catálogos en el estado cada vez que cambian.
                .collect { (c, t) -> _uiState.update { it.copy(categories = c, animalTypes = t) } }
        }
    }

    // Resultado de pedir la ubicación actual; si no hay, se usa la del perfil (o la primera ciudad).
    fun onCurrentLocation(point: GeoPoint?) {
        // Ejecuta en segundo plano porque necesita leer el usuario actual.
        viewModelScope.launch {
            // Ubicación del perfil, solo si tiene coordenadas.
            val fallback = observeCurrentUser().first()?.location?.takeIf { it.hasCoordinates() }
                // Convierte la ubicación del perfil a punto geográfico.
                ?.let { GeoPoint(it.latitude!!, it.longitude!!) }
            // Centro: ubicación actual, si no la del perfil, si no el centro actual.
            val center = point ?: fallback ?: _uiState.value.center
            // Guarda la ubicación, el centro y termina la localización.
            _uiState.update { it.copy(userLocation = point, center = center, isLocating = false) }
            // Carga las publicaciones alrededor del centro.
            load()
        }
    }

    // Recentra el mapa en la ubicación actual (botón "mi ubicación") y quita el filtro de ciudad.
    fun onRecenter(point: GeoPoint?) {
        // Si no se obtuvo ubicación, no hace nada.
        if (point == null) return
        // Usa la ubicación como centro y elimina la ciudad elegida.
        _uiState.update { it.copy(userLocation = point, center = point, filters = it.filters.copy(city = null)) }
        // Recarga las publicaciones.
        load()
    }

    // --- Mapa y resumen ---

    // Selecciona la publicación del marcador tocado para mostrar su resumen.
    fun onMarkerClick(postId: String) = _uiState.update { it.copy(selectedPostId = postId) }

    // Tocar el mapa fuera de un marcador oculta el resumen.
    fun onMapClick() = _uiState.update { it.copy(selectedPostId = null) }

    // "Todos": quita los filtros y vuelve a mostrar todo alrededor.
    fun onClearFiltersClick() {
        // Restablece los filtros y quita la selección.
        _uiState.update { it.copy(filters = ExploreFilters(), selectedPostId = null) }
        // Recarga las publicaciones.
        load()
    }

    // --- Buscador de lugares ---

    // Actualiza el texto del buscador y busca sugerencias con una espera (debounce).
    fun onQueryChange(value: String) {
        // Guarda el texto escrito.
        _uiState.update { it.copy(query = value) }
        // Cancela la búsqueda anterior si aún no terminó.
        searchJob?.cancel()
        // Lanza una nueva búsqueda.
        searchJob = viewModelScope.launch {
            // Espera 400 ms para no buscar en cada tecla.
            delay(400)
            // Si hay ciudad filtrada, la usa para priorizar resultados cercanos.
            val city = _uiState.value.filters.city?.toLocation()
            // Busca la dirección y publica las sugerencias (vacías si falla).
            _uiState.update { it.copy(suggestions = searchAddress(value, city).getOrDefault(emptyList())) }
        }
    }

    // Ir a un lugar buscado: el mapa y la búsqueda de cercanía se centran ahí.
    fun onSuggestionClick(location: Location) {
        // Latitud del lugar; si no tiene, no hace nada.
        val lat = location.latitude ?: return
        // Longitud del lugar; si no tiene, no hace nada.
        val lng = location.longitude ?: return
        // Centra en el lugar, muestra su dirección en el buscador y limpia sugerencias y selección.
        _uiState.update { it.copy(center = GeoPoint(lat, lng), query = location.address, suggestions = emptyList(), selectedPostId = null) }
        // Recarga las publicaciones alrededor del nuevo centro.
        load()
    }

    // --- Pantalla de filtros (borrador hasta "Aplicar") ---

    // Abre la pantalla de filtros copiando los filtros actuales al borrador.
    fun onOpenFilters() {
        // Muestra la pantalla y prepara el borrador.
        _uiState.update { it.copy(showFilters = true, draft = it.filters) }
        // Calcula cuántas publicaciones daría el borrador.
        countDraft()
    }

    // Cierra la pantalla de filtros sin aplicar el borrador.
    fun onCloseFilters() = _uiState.update { it.copy(showFilters = false) }

    // Modifica el borrador con la función recibida y recalcula el conteo.
    fun onDraftChange(change: (ExploreFilters) -> ExploreFilters) {
        // Aplica el cambio sobre el borrador actual.
        _uiState.update { it.copy(draft = change(it.draft)) }
        // Recalcula el número de resultados.
        countDraft()
    }

    // Restablece el borrador a los filtros por defecto.
    fun onClearDraft() = onDraftChange { ExploreFilters() }

    // Aplica el borrador como filtros activos y recarga el mapa.
    fun onApplyFilters() {
        // Toma el borrador actual.
        val draft = _uiState.value.draft
        // Actualiza el estado con los nuevos filtros.
        _uiState.update { state ->
            // Copia del estado con los cambios.
            state.copy(
                // El borrador pasa a ser el filtro aplicado.
                filters = draft,
                // Cierra la pantalla de filtros.
                showFilters = false,
                // Quita la publicación seleccionada.
                selectedPostId = null,
                // Con una ciudad elegida, el mapa va a ella; sin ciudad, vuelve a la persona.
                center = draft.city?.let { GeoPoint(it.latitude, it.longitude) } ?: state.userLocation ?: state.center,
            )
        }
        // Recarga las publicaciones con los nuevos filtros.
        load()
    }

    // --- Carga ---

    // Carga las publicaciones según los filtros y el centro actuales.
    private fun load() {
        // Ejecuta en segundo plano.
        viewModelScope.launch {
            // Marca la carga en curso.
            _uiState.update { it.copy(isLoading = true) }
            // Toma una copia del estado para usar filtros y centro coherentes.
            val state = _uiState.value
            // Pide todas las publicaciones que cumplen el filtro.
            val posts = fetch(state.filters, state.center)
            // Publica el resultado.
            _uiState.update { s ->
                // Copia del estado con los cambios.
                s.copy(
                    // Termina la carga.
                    isLoading = false,
                    // Convierte las publicaciones al modelo de UI relativo al centro.
                    posts = posts.map { it.toUi(s.center) },
                    // Mantiene la selección solo si la publicación sigue en los resultados.
                    selectedPostId = s.selectedPostId?.takeIf { id -> posts.any { it.id == id } },
                )
            }
        }
    }

    // "Hay N mascotas encontradas": se recalcula con el borrador (espera a que deje de tocar).
    private fun countDraft() {
        // Cancela el conteo anterior si aún no terminó.
        countJob?.cancel()
        // Lanza un nuevo conteo.
        countJob = viewModelScope.launch {
            // Espera 250 ms para no consultar en cada cambio.
            delay(250)
            // Toma una copia del estado.
            val state = _uiState.value
            // Centro del conteo: la ciudad del borrador, si no la ubicación del usuario, si no el centro actual.
            val center = state.draft.city?.let { GeoPoint(it.latitude, it.longitude) } ?: state.userLocation ?: state.center
            // Consulta las publicaciones del borrador y guarda cuántas son.
            _uiState.update { it.copy(draftCount = fetch(state.draft, center).size) }
        }
    }

    // Pide páginas del feed hasta que no lleguen más (con un tope, por la cuota de lecturas).
    private suspend fun fetch(filters: ExploreFilters, center: GeoPoint): List<Post> {
        // Construye el filtro del dominio a partir de los filtros de la pantalla.
        val filter = PostFilter(
            // Categoría.
            categoryId = filters.categoryId,
            // Tipo de animal.
            animalTypeId = filters.animalTypeId,
            // Centro de la búsqueda por cercanía.
            center = Location(center.latitude, center.longitude),
            // Radio en kilómetros.
            radiusKm = filters.radiusKm.toDouble(),
            // Tamaño.
            size = filters.size,
            // Solo vacunados.
            vaccinatedOnly = filters.vaccinatedOnly,
        )
        // Acumulador de todas las publicaciones obtenidas.
        val all = mutableListOf<Post>()
        // Pide como máximo MAX_PAGES páginas.
        repeat(MAX_PAGES) {
            // Pide la página siguiente a la última publicación recibida (vacía si falla).
            val page = getFeed(filter, afterPostId = all.lastOrNull()?.id).getOrDefault(emptyList())
            // Agrega la página al acumulador.
            all += page
            // Si la página vino incompleta, ya no hay más: devuelve lo acumulado.
            if (page.size < GetFeedUseCase.PAGE_SIZE) return all
        }
        // Devuelve lo acumulado al alcanzar el tope de páginas.
        return all
    }

    // Convierte una publicación del dominio en el modelo del mapa, relativo al centro dado.
    private fun Post.toUi(center: GeoPoint): ExplorePostUi {
        // Punto aproximado (desplazado de forma estable según el id) para no revelar la ubicación exacta.
        val point = approximatePoint(location.latitude ?: center.latitude, location.longitude ?: center.longitude, id)
        // Construye el modelo de UI.
        return ExplorePostUi(
            // Identificador.
            id = id,
            // Título.
            title = title,
            // Primera foto.
            imageUrl = photoUrls.firstOrNull(),
            // Categoría.
            categoryId = categoryId,
            // Nombre de la categoría buscado en el catálogo (vacío si no se encuentra).
            categoryName = observeCategories().value.firstOrNull { it.id == categoryId }?.name.orEmpty(),
            // Nombre del tipo de animal buscado en el catálogo.
            animalName = observeAnimalTypes().value.firstOrNull { it.id == animalTypeId }?.name,
            // Raza.
            breed = breed,
            // Tamaño.
            size = size,
            // Marcas de importante.
            importantCount = importantCount,
            // Distancia al centro redondeada a un decimal.
            distanceKm = location.distanceKmTo(Location(center.latitude, center.longitude))?.let { Math.round(it * 10) / 10.0 },
            // Punto aproximado del marcador.
            point = point,
        )
    }

    // Constantes privadas del ViewModel.
    private companion object {
        // Número máximo de páginas del feed que se piden por carga.
        const val MAX_PAGES = 5
    }
}
