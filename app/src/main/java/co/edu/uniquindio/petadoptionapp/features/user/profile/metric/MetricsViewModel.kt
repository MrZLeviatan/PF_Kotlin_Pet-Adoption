package co.edu.uniquindio.petadoptionapp.features.user.profile.metric

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.BadgeRequirementType
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.UserLevel
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.GetPostStatsUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.reputation.EvaluateMyBadgesUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.reputation.LoadBadgesUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.reputation.ObserveBadgesUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.ObserveCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
ViewModel de la pantalla "Métricas" (se abre al tocar la tarjeta de nivel en Mi Perfil).
Reúne en un único MetricsUiState tres fuentes de información:
1. El perfil del usuario (ObserveCurrentUserUseCase): nombre, foto, ubicación, nivel y puntos.
2. El catálogo de insignias (ObserveBadgesUseCase), que se cruza con las insignias
   desbloqueadas del usuario para obtener su nombre y descripción.
3. Las estadísticas de publicaciones (GetPostStatsUseCase): activas, en revisión y finalizadas.
Al iniciarse también carga el catálogo (LoadBadgesUseCase) y evalúa si el usuario ganó
alguna insignia nueva (EvaluateMyBadgesUseCase). No emite eventos de navegación: la
pantalla recibe onBack y onShare desde AppNavigation y llama a refresh() al reanudarse.
**/

// Insignia ya desbloqueada, lista para dibujar (nombre y descripción vienen del catálogo).
data class BadgeUi(
    // Identificador de la insignia en el catálogo.
    val id: String,
    // Nombre visible de la insignia.
    val name: String,
    // Descripción del logro que la desbloquea.
    val description: String,
    // Tipo de requisito, usado para elegir el icono.
    val requirementType: BadgeRequirementType,
    // Momento (epoch en milisegundos) en que se desbloqueó.
    val unlockedAt: Long,
)

// Estado de la pantalla: datos neutros; los textos fijos los pone la UI con strings.xml.
data class MetricsUiState(
    // Nombre de usuario.
    val userName: String = "",
    // URL de la foto de perfil (null si no tiene).
    val photoUrl: String? = null,
    // Ubicación formateada "Ciudad, País".
    val location: String = "",
    // Nivel de reputación actual.
    val level: UserLevel = UserLevel.ANIMAL_FRIEND,
    // Número del nivel actual.
    val levelNumber: Int = 1,
    // Puntos de reputación acumulados.
    val points: Int = 0,
    // Puntos necesarios para el siguiente nivel; null en el nivel máximo.
    val nextLevelPoints: Int? = null,
    // Cantidad de publicaciones activas.
    val activePosts: Int = 0,
    // Cantidad de publicaciones pendientes de verificación.
    val reviewPosts: Int = 0,
    // Cantidad de publicaciones finalizadas.
    val finishedPosts: Int = 0,
    // Insignias desbloqueadas, de la más reciente a la más antigua.
    val badges: List<BadgeUi> = emptyList(),
    // Indica si falló la carga de estadísticas de publicaciones.
    val hasError: Boolean = false,
)

// Marca la clase para que Hilt la construya e inyecte sus dependencias.
@HiltViewModel
class MetricsViewModel @Inject constructor(
    // Emite el usuario en sesión cada vez que cambia.
    observeCurrentUser: ObserveCurrentUserUseCase,
    // Emite el catálogo de insignias disponible.
    observeBadges: ObserveBadgesUseCase,
    // Descarga o refresca el catálogo de insignias.
    private val loadBadges: LoadBadgesUseCase,
    // Revisa si el usuario cumple requisitos de insignias nuevas y las otorga.
    private val evaluateMyBadges: EvaluateMyBadgesUseCase,
    // Obtiene el conteo de publicaciones del usuario por estado.
    private val getPostStats: GetPostStatsUseCase,
) : ViewModel() {

    // Estado mutable interno.
    private val _uiState = MutableStateFlow(MetricsUiState())
    // Estado público de solo lectura que observa la pantalla.
    val uiState: StateFlow<MetricsUiState> = _uiState.asStateFlow()

    // Bloque de inicialización: arranca observaciones y cargas al crear el ViewModel.
    init {
        // Perfil + catálogo: si cambian puntos o insignias, la pantalla se actualiza sola.
        viewModelScope.launch {
            // Combina ambos flujos en un par (usuario, catálogo) cada vez que alguno emite.
            combine(observeCurrentUser(), observeBadges()) { user, catalog -> user to catalog }
                // Procesa cada combinación.
                .collect { (user, catalog) ->
                    // Sin sesión no hay nada que mostrar.
                    if (user == null) return@collect
                    // Reputación del usuario.
                    val reputation = user.reputation
                    // Nivel actual según los puntos.
                    val level = reputation.level()
                    // Siguiente nivel en el enum; null si ya es el último.
                    val next = UserLevel.entries.getOrNull(level.ordinal + 1)

                    // Cruza cada insignia ganada con su ficha del catálogo.
                    val badges = reputation.unlockedBadges.mapNotNull { unlocked ->
                        // Busca la ficha; si no existe en el catálogo, la insignia se omite.
                        catalog.firstOrNull { it.id == unlocked.badgeId }?.let { badge ->
                            // Construye el modelo de UI con datos del catálogo y fecha de desbloqueo.
                            BadgeUi(badge.id, badge.name, badge.description,
                                badge.requirementType, unlocked.unlockedAt)
                        }
                    // Ordena de la más reciente a la más antigua.
                    }.sortedByDescending { it.unlockedAt }

                    // Actualiza el estado con perfil, nivel e insignias.
                    _uiState.update {
                        it.copy(
                            userName = user.account.userName,
                            photoUrl = user.photoUrl,
                            // Une ciudad y país omitiendo las partes vacías.
                            location = listOf(user.location.city, user.location.country)
                                .filter { p -> p.isNotBlank() }.joinToString(", "),
                            level = level,
                            levelNumber = reputation.levelNumber(),
                            points = reputation.points,
                            // Puntos del siguiente nivel o null si es el máximo.
                            nextLevelPoints = next?.minPoints,
                            badges = badges,
                        )
                    }
                }
        }

        // Carga el catálogo y revisa si ya cumple alguna insignia nueva.
        viewModelScope.launch {
            // Primero asegura que el catálogo esté disponible.
            loadBadges()
            // Luego evalúa si el usuario desbloqueó insignias nuevas.
            evaluateMyBadges()
        }
        // Carga inicial de las estadísticas de publicaciones.
        refresh()
    }

    // Recalcula las publicaciones (se llama también al volver a la pantalla).
    fun refresh() {
        // Lanza la consulta en una corrutina ligada al ViewModel.
        viewModelScope.launch {
            // Pide el conteo de publicaciones por estado.
            getPostStats()
                // Si sale bien, actualiza los contadores y limpia el error.
                .onSuccess { stats ->
                    _uiState.update {
                        it.copy(
                            activePosts = stats.active,
                            reviewPosts = stats.pendingVerification,
                            finishedPosts = stats.finished,
                            hasError = false,
                        )
                    }
                }
                // Si falla, marca el error para que la UI muestre el aviso.
                .onFailure { _uiState.update { s -> s.copy(hasError = true) } }
        }
    }
}
