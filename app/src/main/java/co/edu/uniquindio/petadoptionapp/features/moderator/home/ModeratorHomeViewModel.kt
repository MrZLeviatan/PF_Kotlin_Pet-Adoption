package co.edu.uniquindio.petadoptionapp.features.moderator.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.petadoptionapp.domain.model.post.ModerationStats
import co.edu.uniquindio.petadoptionapp.domain.model.post.Post
import co.edu.uniquindio.petadoptionapp.domain.model.post.PriorityLevel
import co.edu.uniquindio.petadoptionapp.domain.model.post.VerificationStatus
import co.edu.uniquindio.petadoptionapp.domain.model.post.filter.PostFilter
import co.edu.uniquindio.petadoptionapp.domain.model.users.profile.UserSummary
import co.edu.uniquindio.petadoptionapp.domain.usecase.catalog.LoadCatalogUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.catalog.ObserveCategoriesUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.notification.CountUnreadNotificationsUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.GetFeedUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.GetModerationQueueUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.GetModerationStatsUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.GetCurrentModeratorUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.SearchUsersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Publicación de la cola (o de la búsqueda) lista para dibujar; los textos fijos los pone la UI. */
data class ModerationPostUi(
    val id: String,
    val imageUrl: String?,
    val title: String,
    val categoryName: String,
    // Prioridad sugerida por la IA y su explicación; null si aún no la analizó.
    val priority: PriorityLevel?,
    val priorityReason: String,
    val ageHours: Int,
    val verificationStatus: VerificationStatus,
)

data class ModeratorHomeUiState(
    val name: String = "",
    val notifications: Int = 0,
    val stats: ModerationStats = ModerationStats(),
    // Sin moderador asignado, de mayor a menor prioridad.
    val queue: List<ModerationPostUi> = emptyList(),
    // Al tocar "Prioridad alta" la cola se filtra a las altas y urgentes; al tocarla de nuevo se quita.
    val highPriorityOnly: Boolean = false,
    val query: String = "",
    // Resultados de publicaciones al escribir (sin @).
    val postResults: List<ModerationPostUi> = emptyList(),
    // Resultados de personas al escribir con @ (solo usuarios; los moderadores no se buscan).
    val users: List<UserSummary> = emptyList(),
    val isSearchingUsers: Boolean = false,
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
) {
    val isSearching: Boolean get() = query.isNotBlank()

    /** Cola que se muestra, con el filtro de prioridad alta si está activo. */
    val visibleQueue: List<ModerationPostUi>
        get() = if (highPriorityOnly) queue.filter { it.priority in HIGH_PRIORITIES } else queue

    private companion object {
        val HIGH_PRIORITIES = setOf(PriorityLevel.HIGH, PriorityLevel.URGENT)
    }
}

/**
 Inicio del moderador: saludo y campana (las mismas notificaciones que el usuario), buscador,
 dashboard de moderación (general de la app; "revisadas hoy" es del moderador) y la cola de publicaciones
 que ningún moderador ha tomado, por prioridad. Se recarga cada vez que la pantalla vuelve a verse.
 */
@HiltViewModel
class ModeratorHomeViewModel @Inject constructor(
    private val getCurrentModerator: GetCurrentModeratorUseCase,
    private val getModerationStats: GetModerationStatsUseCase,
    private val getModerationQueue: GetModerationQueueUseCase,
    private val getFeed: GetFeedUseCase,
    private val loadCatalog: LoadCatalogUseCase,
    private val observeCategories: ObserveCategoriesUseCase,
    private val countUnread: CountUnreadNotificationsUseCase,
    private val searchUsers: SearchUsersUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ModeratorHomeUiState())
    val uiState: StateFlow<ModeratorHomeUiState> = _uiState.asStateFlow()

    // Publicaciones donde se busca por título: las del feed público y todas las pendientes.
    private var searchable: List<ModerationPostUi> = emptyList()
    private var searchJob: Job? = null

    fun refresh() {
        viewModelScope.launch {
            loadCatalog()   // Para mostrar el nombre de la categoría.
            getCurrentModerator().onSuccess { moderator ->
                // Saludo con el primer nombre ("Hola, Nicolas"); sin nombre, el alias.
                val first = moderator.name.trim().substringBefore(" ").ifBlank { moderator.account.userName }
                _uiState.update { it.copy(name = first) }
            }
            countUnread().onSuccess { count -> _uiState.update { it.copy(notifications = count) } }

            val stats = getModerationStats()
            val queue = getModerationQueue(unassignedOnly = true)
            if (stats.isFailure || queue.isFailure) {
                _uiState.update { it.copy(isLoading = false, hasError = true) }
                return@launch
            }
            val queueUi = queue.getOrThrow().map { it.toUi() }
            _uiState.update {
                it.copy(stats = stats.getOrThrow(), queue = queueUi, isLoading = false, hasError = false)
            }

            // Lo que se puede buscar: pendientes (también las en revisión) y el feed público.
            val pending = getModerationQueue().getOrDefault(emptyList())
            val feed = getFeed(PostFilter()).getOrDefault(emptyList())
            searchable = (pending + feed).distinctBy { it.id }.map { it.toUi() }
            if (!_uiState.value.isSearchingUsers) applyPostFilter(_uiState.value.query)
        }
    }

    fun onHighPriorityClick() = _uiState.update { it.copy(highPriorityOnly = !it.highPriorityOnly) }

    fun onQueryChange(value: String) {
        val searchingUsers = value.trimStart().startsWith("@")
        _uiState.update { it.copy(query = value, isSearchingUsers = searchingUsers) }
        searchJob?.cancel()
        if (searchingUsers) {
            searchJob = viewModelScope.launch {
                delay(300)   // Espera a que deje de escribir (cuida la cuota de lecturas).
                searchUsers(value).onSuccess { list -> _uiState.update { it.copy(users = list) } }
            }
        } else {
            _uiState.update { it.copy(users = emptyList()) }
            applyPostFilter(value)
        }
    }

    // Filtra por título, ignorando mayúsculas; sin texto no hay resultados (se ve el dashboard).
    private fun applyPostFilter(query: String) {
        val q = query.trim()
        _uiState.update {
            it.copy(postResults = if (q.isEmpty()) emptyList() else searchable.filter { p -> p.title.contains(q, ignoreCase = true) })
        }
    }

    private fun Post.toUi() = ModerationPostUi(
        id = id,
        imageUrl = photoUrls.firstOrNull(),
        title = title,
        categoryName = observeCategories().value.firstOrNull { it.id == categoryId }?.name.orEmpty(),
        priority = verification.priorityLevel,
        priorityReason = verification.priorityReason,
        ageHours = ((System.currentTimeMillis() - createdAt) / 3_600_000L).toInt().coerceAtLeast(0),
        verificationStatus = verification.status,
    )
}
