package co.edu.uniquindio.petadoptionapp.features.user.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.petadoptionapp.domain.model.location.Location
import co.edu.uniquindio.petadoptionapp.domain.model.post.AnimalSize
import co.edu.uniquindio.petadoptionapp.domain.model.post.Post
import co.edu.uniquindio.petadoptionapp.domain.model.post.filter.PostFilter
import co.edu.uniquindio.petadoptionapp.domain.model.users.profile.UserSummary
import co.edu.uniquindio.petadoptionapp.domain.usecase.catalog.LoadCatalogUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.catalog.ObserveAnimalTypesUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.catalog.ObserveCategoriesUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.notification.CountUnreadNotificationsUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.GetFeedUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.ObserveCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.SearchUsersUseCase


// Publicación lista para dibujar. Solo guarda valores neutros: los textos los pone la UI con strings.xml.
data class FeedPost(
    val id: String,
    val imageUrl: String?,
    val categoryName: String,      // Viene del catálogo (lo crea el moderador), por eso es texto.
    val distanceKm: Double?,       // null si el usuario no tiene ubicación.
    val title: String,
    val animalName: String?,       // Viene del catálogo.
    val breed: String,             // Vacío si no se sabe.
    val size: AnimalSize?,
    val vaccinated: Boolean?,
    val importantCount: Int
)

// Estado completo que la pantalla necesita para dibujarse.
data class HomeUserUiState(
    val userName: String = "",
    val city: String = "",
    val notifications: Int = 0,
    val query: String = "",
    val posts: List<FeedPost> = emptyList(),
    val isLoading: Boolean = false,
    val hasError: Boolean = false,
    val users: List<UserSummary> = emptyList(),   // Resultados de personas (cuando se busca con @).
    val isSearchingUsers: Boolean = false         // true si la búsqueda actual es de personas.
)

@HiltViewModel
class HomeUserViewModel @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    private val getFeed: GetFeedUseCase,
    private val loadCatalog: LoadCatalogUseCase,
    private val observeCategories: ObserveCategoriesUseCase,
    private val observeAnimalTypes: ObserveAnimalTypesUseCase,
    private val countUnread: CountUnreadNotificationsUseCase,
    private val searchUsers: SearchUsersUseCase
) : ViewModel() {

    // Lista completa sin filtrar (el buscador filtra sobre esta).
    private var allPosts: List<FeedPost> = emptyList()

    // Ubicación del usuario: sirve para calcular la distancia a cada publicación.
    private var userLocation: Location? = null

    private var searchJob: Job? = null



    private val _uiState = MutableStateFlow(HomeUserUiState())
    val uiState: StateFlow<HomeUserUiState> = _uiState.asStateFlow()

    init {
        // Si el perfil cambia (por ejemplo, edita su ubicación), saludo y distancias se actualizan.
        viewModelScope.launch {
            observeCurrentUser().collect { user ->
                userLocation = user?.location
                _uiState.update {
                    it.copy(
                        userName = user?.account?.userName.orEmpty(),
                        city = user?.location?.toDisplayText().orEmpty()
                    )
                }
                loadFeed()
            }
        }
        loadUnreadCount()
    }

    // Pide la primera página del feed y la convierte a FeedPost.
    private fun loadFeed() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, hasError = false) }
            loadCatalog()
            getFeed(PostFilter())
                .onSuccess { posts ->
                    allPosts = posts.map { it.toFeedPost() }
                    applyFilter(_uiState.value.query)
                    _uiState.update { it.copy(isLoading = false) }
                }
                .onFailure { _uiState.update { s -> s.copy(isLoading = false, hasError = true) } }
        }
    }

    private fun loadUnreadCount() {
        viewModelScope.launch {
            countUnread().onSuccess { count -> _uiState.update { it.copy(notifications = count) } }
        }
    }

    fun onQueryChange(value: String) {
        val searchingUsers = value.trimStart().startsWith("@")
        _uiState.update { it.copy(query = value, isSearchingUsers = searchingUsers) }

        // Cancela la búsqueda anterior: solo vale la última letra escrita.
        searchJob?.cancel()

        if (searchingUsers) {
            searchJob = viewModelScope.launch {
                delay(300)   // Espera a que deje de escribir (cuida la cuota de lecturas).
                searchUsers(value).onSuccess { list -> _uiState.update { it.copy(users = list) } }
            }
        } else {
            // Búsqueda normal: filtra las publicaciones y limpia las personas.
            _uiState.update { it.copy(users = emptyList()) }
            applyFilter(value)
        }
    }

    // Filtra la lista completa por título, ignorando mayúsculas.
    private fun applyFilter(query: String) {
        _uiState.update { state ->
            state.copy(posts = allPosts.filter { it.title.contains(query.trim(), ignoreCase = true) })
        }
    }

    // Convierte una publicación del dominio en la versión que dibuja la tarjeta.
    private fun Post.toFeedPost() = FeedPost(
        id = id,
        imageUrl = photoUrls.firstOrNull(),
        categoryName = observeCategories().value.firstOrNull { it.id == categoryId }?.name.orEmpty(),
        distanceKm = userLocation?.let { location.distanceKmTo(it) }?.let { Math.round(it * 10) / 10.0 },
        title = title,
        animalName = observeAnimalTypes().value.firstOrNull { it.id == animalTypeId }?.name,
        breed = breed,
        size = size,
        vaccinated = vaccinated,
        importantCount = importantCount
    )

    // "Armenia, Colombia". Si falta alguna parte no deja comas colgando.
    private fun Location.toDisplayText(): String =
        listOf(city, country).filter { it.isNotBlank() }.joinToString(", ")



}