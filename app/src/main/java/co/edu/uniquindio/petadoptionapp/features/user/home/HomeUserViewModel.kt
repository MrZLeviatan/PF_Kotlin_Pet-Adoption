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
import co.edu.uniquindio.petadoptionapp.domain.usecase.importantVote.GetMyVotedPostIdsUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.importantVote.ToggleImportantUseCase
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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow


/**
ViewModel de la pantalla de inicio del usuario (feed). Prepara el saludo con el alias y la ciudad,
el contador de notificaciones sin leer, la lista de publicaciones recientes y el buscador, que
filtra publicaciones por título o, si el texto empieza con "@", busca personas por su alias.
También gestiona el voto "Es importante" de cada publicación de forma optimista: cambia la
pantalla al instante y revierte el cambio si el servidor falla.
Estado (HomeUserUiState): alias, ciudad, notificaciones, búsqueda, publicaciones, carga, error y
resultados de personas. Evento de una sola vez: HomeUserEvent.ImportantError.
Eventos de UI: refresh, refreshUnreadCount, onQueryChange y onImportantClick.
Casos de uso: ObserveCurrentUserUseCase, GetFeedUseCase, LoadCatalogUseCase, ObserveCategoriesUseCase,
ObserveAnimalTypesUseCase, CountUnreadNotificationsUseCase, SearchUsersUseCase, ToggleImportantUseCase
y GetMyVotedPostIdsUseCase.
Navegación: el ViewModel no navega; HomeUserScreen avisa a AppNavigation al tocar una publicación,
una persona o la campana de notificaciones.
**/

// Publicación lista para dibujar. Solo guarda valores neutros: los textos los pone la UI con strings.xml.
data class FeedPost(
    // Identificador de la publicación.
    val id: String,
    // Primera foto de la publicación, si tiene.
    val imageUrl: String?,
    // Viene del catálogo (lo crea el moderador), por eso es texto.
    val categoryName: String,
    // Distancia al usuario en km; null si el usuario no tiene ubicación.
    val distanceKm: Double?,
    // Título de la publicación.
    val title: String,
    // Nombre del tipo de animal; viene del catálogo.
    val animalName: String?,
    // Raza del animal; vacío si no se sabe.
    val breed: String,
    // Tamaño del animal, si se conoce.
    val size: AnimalSize?,
    // Si está vacunado; null si no se indicó.
    val vaccinated: Boolean?,
    // Cantidad de votos "Es importante".
    val importantCount: Int,
    // Indica si el usuario actual ya votó "Es importante".
    val voted: Boolean = false,
    // Indica si la publicación es del usuario actual (no puede votarla).
    val isOwn: Boolean = false
)

// Estado completo que la pantalla necesita para dibujarse.
data class HomeUserUiState(
    // Alias del usuario para el saludo.
    val userName: String = "",
    // Ciudad y país del usuario ("Armenia, Colombia").
    val city: String = "",
    // Cantidad de notificaciones sin leer (campana).
    val notifications: Int = 0,
    // Texto del buscador.
    val query: String = "",
    // Publicaciones visibles (ya filtradas por la búsqueda).
    val posts: List<FeedPost> = emptyList(),
    // Indica si se está cargando el feed.
    val isLoading: Boolean = false,
    // Indica si falló la carga del feed.
    val hasError: Boolean = false,
    // Resultados de personas (cuando se busca con @).
    val users: List<UserSummary> = emptyList(),
    // true si la búsqueda actual es de personas.
    val isSearchingUsers: Boolean = false
)


// Eventos de una sola vez que el ViewModel envía a la pantalla.
sealed interface HomeUserEvent {
    // No se pudo guardar el voto "Es importante" (la UI muestra un aviso).
    data object ImportantError : HomeUserEvent
}

// ViewModel del inicio, inyectado por Hilt con sus casos de uso.
@HiltViewModel
class HomeUserViewModel @Inject constructor(
    // Observa el perfil del usuario actual (alias, ubicación, id).
    private val observeCurrentUser: ObserveCurrentUserUseCase,
    // Obtiene páginas del feed de publicaciones.
    private val getFeed: GetFeedUseCase,
    // Carga los catálogos (categorías y tipos de animal).
    private val loadCatalog: LoadCatalogUseCase,
    // Observa el catálogo de categorías.
    private val observeCategories: ObserveCategoriesUseCase,
    // Observa el catálogo de tipos de animal.
    private val observeAnimalTypes: ObserveAnimalTypesUseCase,
    // Cuenta las notificaciones sin leer.
    private val countUnread: CountUnreadNotificationsUseCase,
    // Busca personas por alias.
    private val searchUsers: SearchUsersUseCase,
    // Alterna el voto "Es importante" de una publicación.
    private val toggleImportant: ToggleImportantUseCase,
    // Obtiene cuáles publicaciones (de una lista) ya votó el usuario.
    private val getMyVotedPostIds: GetMyVotedPostIdsUseCase
) : ViewModel() {

    // Id de quien tiene la sesión (para saber cuáles publicaciones son mías).
    private var currentUserId: String = ""

    // Publicaciones con un voto en curso: evita el doble clic.
    private val pendingVotes = mutableSetOf<String>()

    // Canal con búfer para eventos de una sola vez.
    private val _events = Channel<HomeUserEvent>(Channel.BUFFERED)
    // Flujo de eventos que consume la pantalla.
    val events = _events.receiveAsFlow()

    // Lista completa sin filtrar (el buscador filtra sobre esta).
    private var allPosts: List<FeedPost> = emptyList()

    // Ubicación del usuario: sirve para calcular la distancia a cada publicación.
    private var userLocation: Location? = null

    // Tarea de búsqueda de personas en curso (se cancela al escribir de nuevo).
    private var searchJob: Job? = null



    // Estado mutable interno de la pantalla.
    private val _uiState = MutableStateFlow(HomeUserUiState())
    // Estado de solo lectura que observa la UI.
    val uiState: StateFlow<HomeUserUiState> = _uiState.asStateFlow()

    // Al crearse, observa el perfil y carga el contador de notificaciones.
    init {
        // Si el perfil cambia (por ejemplo, edita su ubicación), saludo y distancias se actualizan.
        viewModelScope.launch {
            // Recoge cada versión del usuario actual.
            observeCurrentUser().collect { user ->
                // Guarda la ubicación para calcular distancias.
                userLocation = user?.location
                // Guarda el id del usuario para reconocer sus publicaciones.
                currentUserId = user?.account?.id.orEmpty()
                // Actualiza el saludo.
                _uiState.update {
                    // Copia del estado con alias y ciudad.
                    it.copy(
                        // Alias del usuario.
                        userName = user?.account?.userName.orEmpty(),
                        // Ciudad y país formateados.
                        city = user?.location?.toDisplayText().orEmpty()
                    )
                }
                // Recarga el feed con la nueva información.
                loadFeed()
            }
        }
        // Carga el número de notificaciones sin leer.
        loadUnreadCount()

    }

    // Pide la primera página del feed y la convierte a FeedPost.
    private fun loadFeed() {
        // Ejecuta en segundo plano.
        viewModelScope.launch {
            // Marca la carga y limpia el error.
            _uiState.update { it.copy(isLoading = true, hasError = false) }
            // Asegura que los catálogos estén cargados para mostrar nombres.
            loadCatalog()
            // Pide el feed sin filtros.
            getFeed(PostFilter())
                // Si funcionó:
                .onSuccess { posts ->
                    // Una sola consulta pinta todas las estrellas de la página.
                    val voted = getMyVotedPostIds(posts.map { it.id }).getOrDefault(emptySet())
                    // Convierte las publicaciones al modelo de la tarjeta.
                    allPosts = posts.map { it.toFeedPost(voted) }
                    // Aplica la búsqueda escrita actualmente.
                    applyFilter(_uiState.value.query)
                    // Termina la carga.
                    _uiState.update { it.copy(isLoading = false) }
                }
                // Si falló, termina la carga y marca el error.
                .onFailure { _uiState.update { s -> s.copy(isLoading = false, hasError = true) } }
        }
    }

    // Consulta cuántas notificaciones sin leer tiene el usuario.
    private fun loadUnreadCount() {
        // Ejecuta en segundo plano.
        viewModelScope.launch {
            // Si la consulta funciona, publica el contador.
            countUnread().onSuccess { count -> _uiState.update { it.copy(notifications = count) } }
        }
    }

    // Recarga el contador de la campana; se llama cada vez que la pantalla vuelve a primer plano.
    fun refreshUnreadCount() = loadUnreadCount()

    /**
    Se llama al volver a la pantalla: recarga el feed (votos "Es importante" y contadores
    que pudieron cambiar en el detalle) y la campana. La búsqueda escrita se conserva.
    **/
    fun refresh() {
        // Recarga las publicaciones.
        loadFeed()
        // Recarga el contador de notificaciones.
        loadUnreadCount()
    }

    // Actualiza la búsqueda: si empieza con "@" busca personas; si no, filtra publicaciones.
    fun onQueryChange(value: String) {
        // Determina si la búsqueda es de personas.
        val searchingUsers = value.trimStart().startsWith("@")
        // Guarda el texto y el tipo de búsqueda.
        _uiState.update { it.copy(query = value, isSearchingUsers = searchingUsers) }

        // Cancela la búsqueda anterior: solo vale la última letra escrita.
        searchJob?.cancel()

        // Búsqueda de personas.
        if (searchingUsers) {
            // Lanza una nueva búsqueda.
            searchJob = viewModelScope.launch {
                // Espera a que deje de escribir (cuida la cuota de lecturas).
                delay(300)
                // Busca personas y publica los resultados si funciona.
                searchUsers(value).onSuccess { list -> _uiState.update { it.copy(users = list) } }
            }
        } else {
            // Búsqueda normal: filtra las publicaciones y limpia las personas.
            _uiState.update { it.copy(users = emptyList()) }
            // Filtra las publicaciones por título.
            applyFilter(value)
        }
    }

    // Filtra la lista completa por título, ignorando mayúsculas.
    private fun applyFilter(query: String) {
        // Publica las publicaciones cuyo título contiene el texto buscado.
        _uiState.update { state ->
            // Copia del estado con la lista filtrada.
            state.copy(posts = allPosts.filter { it.title.contains(query.trim(), ignoreCase = true) })
        }
    }

    // Convierte una publicación del dominio en la versión que dibuja la tarjeta.
    private fun Post.toFeedPost(voted: Set<String>) = FeedPost(
        // Identificador.
        id = id,
        // Primera foto.
        imageUrl = photoUrls.firstOrNull(),
        // Nombre de la categoría buscado en el catálogo.
        categoryName = observeCategories().value.firstOrNull { it.id == categoryId }?.name.orEmpty(),
        // Distancia al usuario redondeada a un decimal (null si no hay ubicación).
        distanceKm = userLocation?.let { location.distanceKmTo(it) }?.let { Math.round(it * 10) / 10.0 },
        // Título.
        title = title,
        // Nombre del tipo de animal buscado en el catálogo.
        animalName = observeAnimalTypes().value.firstOrNull { it.id == animalTypeId }?.name,
        // Raza.
        breed = breed,
        // Tamaño.
        size = size,
        // Vacunado.
        vaccinated = vaccinated,
        // Votos "Es importante".
        importantCount = importantCount,
        // Si el usuario ya votó esta publicación.
        voted = id in voted,
        // Si la publicación es del usuario actual.
        isOwn = authorId == currentUserId
    )

    // "Armenia, Colombia". Si falta alguna parte no deja comas colgando.
    private fun Location.toDisplayText(): String =
        // Une ciudad y país omitiendo los vacíos.
        listOf(city, country).filter { it.isNotBlank() }.joinToString(", ")


    // Alterna "Es importante". Cambia la pantalla al instante y revierte si falla.
    fun onImportantClick(postId: String) {
        // Busca la publicación; si no existe no hace nada.
        val post = allPosts.firstOrNull { it.id == postId } ?: return
        // Propia o con un voto ya en curso: no hace nada (add devuelve false si ya estaba pendiente).
        if (post.isOwn || !pendingVotes.add(postId)) return

        // Estado al que se quiere llegar: lo contrario del actual.
        val willVote = !post.voted
        // Actualización optimista: cambia la pantalla antes de la respuesta del servidor.
        applyVote(postId, willVote)

        // Envía el voto en segundo plano.
        viewModelScope.launch {
            // Llama al caso de uso para alternar el voto.
            toggleImportant(postId)
                // Si funcionó:
                .onSuccess { voted ->
                    // Si el servidor terminó en otro estado, se corrige.
                    if (voted != willVote) applyVote(postId, voted)
                }
                // Si falló:
                .onFailure {
                    // Revierte el cambio optimista.
                    applyVote(postId, !willVote)
                    // Avisa a la pantalla para mostrar el error.
                    _events.trySend(HomeUserEvent.ImportantError)
                }
            // Libera la publicación para permitir nuevos votos.
            pendingVotes.remove(postId)
        }
    }

    // Pone el voto (o lo quita) en la lista y ajusta el contador.
    private fun applyVote(postId: String, voted: Boolean) {
        // Recorre la lista completa modificando solo la publicación indicada.
        allPosts = allPosts.map { p ->
            // Si no es la publicación o ya tiene ese estado, la deja igual.
            if (p.id != postId || p.voted == voted) p
            // Si no, cambia el voto y ajusta el contador sin bajar de cero.
            else p.copy(
                // Nuevo estado del voto.
                voted = voted,
                // Suma o resta un voto, mínimo cero.
                importantCount = (p.importantCount + if (voted) 1 else -1).coerceAtLeast(0)
            )
        }
        // Vuelve a aplicar la búsqueda para refrescar la lista visible.
        applyFilter(_uiState.value.query)
    }


}
