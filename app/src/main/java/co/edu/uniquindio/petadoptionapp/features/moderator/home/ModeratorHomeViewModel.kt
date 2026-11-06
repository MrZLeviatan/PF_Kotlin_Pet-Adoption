package co.edu.uniquindio.petadoptionapp.features.moderator.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.petadoptionapp.domain.error.AiException
import co.edu.uniquindio.petadoptionapp.domain.model.post.AiReview
import co.edu.uniquindio.petadoptionapp.domain.model.post.ModerationStats
import co.edu.uniquindio.petadoptionapp.domain.usecase.ai.AnalyzePostUseCase
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

/**
ViewModel de la pantalla de inicio del moderador (ModeratorHomeScreen).
Estado de UI (ModeratorHomeUiState): nombre para el saludo, contador de notificaciones sin leer,
estadísticas de moderación, cola de publicaciones sin moderador asignado (ordenada por prioridad),
filtro de prioridad elegido, texto del buscador con sus resultados (publicaciones o usuarios con "@"),
banderas de carga/error y el progreso y resultado del análisis por lotes con IA.
Eventos: refresh() al volver a la pantalla, onFilterClick() al tocar una tarjeta del dashboard,
onQueryChange() al escribir en el buscador, onAnalyzePendingClick() al pulsar "Analizar con IA"
y onAiMessageShown() cuando la UI ya mostró el mensaje del resultado.
Casos de uso: GetCurrentModeratorUseCase, GetModerationStatsUseCase, GetModerationQueueUseCase,
GetFeedUseCase, LoadCatalogUseCase, ObserveCategoriesUseCase, CountUnreadNotificationsUseCase,
SearchUsersUseCase y AnalyzePostUseCase.
Navegación: el ViewModel no navega; la pantalla usa los ids de las publicaciones y usuarios del estado
para abrir el detalle de la publicación, el perfil del usuario o las notificaciones.
**/

/** Filtro de la cola al tocar una tarjeta: ALL = "Pendientes" (todas sin moderador). */
enum class QueueFilter { ALL, HIGH, MEDIUM, LOW }

/** Publicación de la cola (o de la búsqueda) lista para dibujar; los textos fijos los pone la UI. */
data class ModerationPostUi(
    // Identificador de la publicación, usado para navegar a su detalle.
    val id: String,
    // URL de la primera foto de la publicación (null si no tiene fotos).
    val imageUrl: String?,
    // Título de la publicación.
    val title: String,
    // Nombre legible de la categoría de la mascota.
    val categoryName: String,
    // Prioridad sugerida por la IA y su explicación; null si aún no la analizó.
    val priority: PriorityLevel?,
    // Explicación textual de por qué la IA asignó esa prioridad.
    val priorityReason: String,
    // Horas transcurridas desde que se creó la publicación.
    val ageHours: Int,
    // Estado actual de verificación de la publicación.
    val verificationStatus: VerificationStatus,
    // Análisis de la IA (null = aún sin analizar).
    val aiReview: AiReview? = null,
)

/** Resultado del último "Analizar con IA" de la cola. */
enum class AiBatchMessage { DONE, NOT_CONFIGURED, FAILED }

// Estado inmutable que la pantalla de inicio del moderador observa y dibuja.
data class ModeratorHomeUiState(
    // Primer nombre del moderador para el saludo.
    val name: String = "",
    // Cantidad de notificaciones sin leer que muestra la campana.
    val notifications: Int = 0,
    // Estadísticas de moderación que alimentan las tarjetas del dashboard.
    val stats: ModerationStats = ModerationStats(),
    // Sin moderador asignado, de mayor a menor prioridad.
    val queue: List<ModerationPostUi> = emptyList(),
    // Tarjeta elegida: filtra la cola por esa prioridad; "Pendientes" (o tocar de nuevo la misma) muestra todas.
    val filter: QueueFilter = QueueFilter.ALL,
    // Texto actual del buscador.
    val query: String = "",
    // Resultados de publicaciones al escribir (sin @).
    val postResults: List<ModerationPostUi> = emptyList(),
    // Resultados de personas al escribir con @ (solo usuarios; los moderadores no se buscan).
    val users: List<UserSummary> = emptyList(),
    // Indica si la búsqueda actual es de usuarios (el texto empieza con @).
    val isSearchingUsers: Boolean = false,
    // Indica si los datos iniciales aún se están cargando.
    val isLoading: Boolean = true,
    // Indica si falló la carga de estadísticas o de la cola.
    val hasError: Boolean = false,
    // "Analizar con IA": progreso (hechas / total) mientras corre, y su resultado.
    val aiProgress: Pair<Int, Int>? = null,
    // Resultado del último análisis por lotes, pendiente de mostrarse en la UI.
    val aiMessage: AiBatchMessage? = null,
) {
    // Indica si hay texto en el buscador (se muestran resultados en vez del dashboard).
    val isSearching: Boolean get() = query.isNotBlank()

    // Publicaciones de la cola que la IA aún no analizó.
    val notAnalyzedCount: Int get() = queue.count { it.aiReview == null }

    /** Cola que se muestra según la tarjeta elegida (las mismas reglas que los números de las tarjetas). */
    val visibleQueue: List<ModerationPostUi>
        // Calcula la lista filtrada cada vez que se consulta.
        get() = when (filter) {
            // Sin filtro: se muestra toda la cola.
            QueueFilter.ALL -> queue
            // Prioridad alta: incluye también las urgentes.
            QueueFilter.HIGH -> queue.filter { it.priority == PriorityLevel.HIGH || it.priority == PriorityLevel.URGENT }
            // Solo las de prioridad media.
            QueueFilter.MEDIUM -> queue.filter { it.priority == PriorityLevel.MEDIUM }
            // Solo las de prioridad baja.
            QueueFilter.LOW -> queue.filter { it.priority == PriorityLevel.LOW }
        }
}

/**
 Inicio del moderador: saludo y campana (las mismas notificaciones que el usuario), buscador,
 dashboard de moderación (general de la app; "revisadas hoy" es del moderador) y la cola de publicaciones
 que ningún moderador ha tomado, por prioridad. Se recarga cada vez que la pantalla vuelve a verse.
 */
// Hilt crea e inyecta este ViewModel con sus casos de uso.
@HiltViewModel
class ModeratorHomeViewModel @Inject constructor(
    // Obtiene el perfil del moderador con sesión iniciada.
    private val getCurrentModerator: GetCurrentModeratorUseCase,
    // Obtiene las estadísticas generales de moderación.
    private val getModerationStats: GetModerationStatsUseCase,
    // Obtiene la cola de publicaciones pendientes de moderar.
    private val getModerationQueue: GetModerationQueueUseCase,
    // Obtiene el feed público de publicaciones (para la búsqueda).
    private val getFeed: GetFeedUseCase,
    // Carga el catálogo (categorías) en memoria.
    private val loadCatalog: LoadCatalogUseCase,
    // Expone las categorías ya cargadas para resolver su nombre.
    private val observeCategories: ObserveCategoriesUseCase,
    // Cuenta las notificaciones sin leer.
    private val countUnread: CountUnreadNotificationsUseCase,
    // Busca usuarios por texto (búsqueda con @).
    private val searchUsers: SearchUsersUseCase,
    // Ejecuta el análisis de IA de una publicación.
    private val analyzePost: AnalyzePostUseCase,
) : ViewModel() {

    // Estado mutable interno de la pantalla.
    private val _uiState = MutableStateFlow(ModeratorHomeUiState())
    // Estado público de solo lectura que observa la UI.
    val uiState: StateFlow<ModeratorHomeUiState> = _uiState.asStateFlow()

    // Publicaciones donde se busca por título: las del feed público y todas las pendientes.
    private var searchable: List<ModerationPostUi> = emptyList()
    // Trabajo de búsqueda de usuarios en curso, para cancelarlo si el texto cambia.
    private var searchJob: Job? = null

    // Recarga todos los datos de la pantalla (saludo, notificaciones, dashboard, cola y búsqueda).
    fun refresh() {
        // Lanza la carga en una corrutina ligada al ciclo de vida del ViewModel.
        viewModelScope.launch {
            // Para mostrar el nombre de la categoría.
            loadCatalog()
            // Consulta el moderador actual y, si responde, actualiza el saludo.
            getCurrentModerator().onSuccess { moderator ->
                // Saludo con el primer nombre ("Hola, Nicolas"); sin nombre, el alias.
                val first = moderator.name.trim().substringBefore(" ").ifBlank { moderator.account.userName }
                // Guarda el nombre en el estado.
                _uiState.update { it.copy(name = first) }
            }
            // Cuenta las notificaciones sin leer y las guarda en el estado.
            countUnread().onSuccess { count -> _uiState.update { it.copy(notifications = count) } }

            // Pide las estadísticas de moderación.
            val stats = getModerationStats()
            // Pide la cola de publicaciones que ningún moderador ha tomado.
            val queue = getModerationQueue(unassignedOnly = true)
            // Si alguna de las dos consultas falla, se marca el error y se detiene la carga.
            if (stats.isFailure || queue.isFailure) {
                // Quita la carga y activa la bandera de error.
                _uiState.update { it.copy(isLoading = false, hasError = true) }
                // Sale de la corrutina sin continuar.
                return@launch
            }
            // Convierte las publicaciones de la cola al modelo de UI.
            val queueUi = queue.getOrThrow().map { it.toUi() }
            // Publica estadísticas y cola en el estado, quitando la carga y el error.
            _uiState.update {
                it.copy(stats = stats.getOrThrow(), queue = queueUi, isLoading = false, hasError = false)
            }

            // Lo que se puede buscar: pendientes (también las en revisión) y el feed público.
            val pending = getModerationQueue().getOrDefault(emptyList())
            // Obtiene el feed público sin filtros (lista vacía si falla).
            val feed = getFeed(PostFilter()).getOrDefault(emptyList())
            // Une ambas listas sin duplicados y las convierte al modelo de UI.
            searchable = (pending + feed).distinctBy { it.id }.map { it.toUi() }
            // Si hay una búsqueda de publicaciones activa, vuelve a aplicarla con los datos nuevos.
            if (!_uiState.value.isSearchingUsers) applyPostFilter(_uiState.value.query)
        }
    }

    // Tocar la tarjeta ya elegida vuelve a "todas".
    fun onFilterClick(filter: QueueFilter) =
        // Alterna entre el filtro elegido y ALL.
        _uiState.update { it.copy(filter = if (it.filter == filter) QueueFilter.ALL else filter) }

    // Maneja cada cambio de texto del buscador.
    fun onQueryChange(value: String) {
        // Determina si se buscan usuarios (texto que empieza con @).
        val searchingUsers = value.trimStart().startsWith("@")
        // Guarda el texto y el modo de búsqueda en el estado.
        _uiState.update { it.copy(query = value, isSearchingUsers = searchingUsers) }
        // Cancela cualquier búsqueda de usuarios anterior.
        searchJob?.cancel()
        // Si se buscan usuarios, se consulta el repositorio con un retardo.
        if (searchingUsers) {
            // Lanza la búsqueda en una corrutina cancelable.
            searchJob = viewModelScope.launch {
                // Espera a que deje de escribir (cuida la cuota de lecturas).
                delay(300)
                // Busca usuarios y guarda los resultados en el estado.
                searchUsers(value).onSuccess { list -> _uiState.update { it.copy(users = list) } }
            }
        } else {
            // En búsqueda de publicaciones se limpian los resultados de usuarios.
            _uiState.update { it.copy(users = emptyList()) }
            // Filtra las publicaciones localmente por título.
            applyPostFilter(value)
        }
    }

    // Filtra por título, ignorando mayúsculas; sin texto no hay resultados (se ve el dashboard).
    private fun applyPostFilter(query: String) {
        // Quita espacios al inicio y al final del texto.
        val q = query.trim()
        // Actualiza los resultados: vacíos si no hay texto o las publicaciones cuyo título lo contiene.
        _uiState.update {
            it.copy(postResults = if (q.isEmpty()) emptyList() else searchable.filter { p -> p.title.contains(q, ignoreCase = true) })
        }
    }

    // Convierte una publicación del dominio al modelo que dibuja la pantalla.
    private fun Post.toUi() = ModerationPostUi(
        // Copia el id.
        id = id,
        // Usa la primera foto como imagen de la tarjeta.
        imageUrl = photoUrls.firstOrNull(),
        // Copia el título.
        title = title,
        // Busca el nombre de la categoría en el catálogo cargado (vacío si no se encuentra).
        categoryName = observeCategories().value.firstOrNull { it.id == categoryId }?.name.orEmpty(),
        // Toma la prioridad calculada por la IA.
        priority = verification.priorityLevel,
        // Toma la explicación de la prioridad.
        priorityReason = verification.priorityReason,
        // Calcula las horas desde la creación (nunca negativas).
        ageHours = ((System.currentTimeMillis() - createdAt) / 3_600_000L).toInt().coerceAtLeast(0),
        // Copia el estado de verificación.
        verificationStatus = verification.status,
        // Copia el análisis de la IA, si existe.
        aiReview = aiReview,
    )

    /**
     Analiza con IA, una por una, las publicaciones de la cola que no tienen análisis
     (las que se publicaron antes de configurar la IA o cuyo análisis falló).
     */
    fun onAnalyzePendingClick() {
        // Reúne los ids de las publicaciones de la cola sin análisis.
        val pending = _uiState.value.queue.filter { it.aiReview == null }.map { it.id }
        // No hace nada si no hay pendientes o si ya hay un análisis en curso.
        if (pending.isEmpty() || _uiState.value.aiProgress != null) return
        // Lanza el análisis por lotes en una corrutina.
        viewModelScope.launch {
            // Resultado por defecto: todo terminó bien.
            var message = AiBatchMessage.DONE
            // Recorre las publicaciones pendientes con su posición.
            for ((index, id) in pending.withIndex()) {
                // Actualiza el progreso y oculta cualquier mensaje anterior.
                _uiState.update { it.copy(aiProgress = index to pending.size, aiMessage = null) }
                // Analiza la publicación y conserva el error, si lo hubo.
                val error = analyzePost(id).exceptionOrNull()
                // Sin llave no tiene caso seguir con las demás.
                if (error === AiException.NotConfigured) {
                    // Marca que la IA no está configurada.
                    message = AiBatchMessage.NOT_CONFIGURED
                    // Detiene el recorrido.
                    break
                }
                // Cualquier otro error marca el lote como fallido, pero sigue con las demás.
                if (error != null) message = AiBatchMessage.FAILED
            }
            // Termina el progreso y publica el resultado del lote.
            _uiState.update { it.copy(aiProgress = null, aiMessage = message) }
            // Recarga la pantalla para mostrar los análisis nuevos.
            refresh()
        }
    }

    // Limpia el mensaje del análisis una vez que la UI lo mostró.
    fun onAiMessageShown() = _uiState.update { it.copy(aiMessage = null) }
}
