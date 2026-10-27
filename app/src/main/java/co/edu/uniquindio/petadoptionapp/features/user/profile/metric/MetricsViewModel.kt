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

// Insignia ya desbloqueada, lista para dibujar (nombre y descripción vienen del catálogo).
data class BadgeUi(
    val id: String,
    val name: String,
    val description: String,
    val requirementType: BadgeRequirementType,
    val unlockedAt: Long,
)

// Estado de la pantalla: datos neutros; los textos fijos los pone la UI con strings.xml.
data class MetricsUiState(
    val userName: String = "",
    val photoUrl: String? = null,
    val location: String = "",
    val level: UserLevel = UserLevel.ANIMAL_FRIEND,
    val levelNumber: Int = 1,
    val points: Int = 0,
    val nextLevelPoints: Int? = null,   // null en el nivel máximo
    val activePosts: Int = 0,
    val reviewPosts: Int = 0,
    val finishedPosts: Int = 0,
    val badges: List<BadgeUi> = emptyList(),
    val hasError: Boolean = false,
)

@HiltViewModel
class MetricsViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUserUseCase,
    observeBadges: ObserveBadgesUseCase,
    private val loadBadges: LoadBadgesUseCase,
    private val evaluateMyBadges: EvaluateMyBadgesUseCase,
    private val getPostStats: GetPostStatsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MetricsUiState())
    val uiState: StateFlow<MetricsUiState> = _uiState.asStateFlow()

    init {
        // Perfil + catálogo: si cambian puntos o insignias, la pantalla se actualiza sola.
        viewModelScope.launch {
            combine(observeCurrentUser(), observeBadges()) { user, catalog -> user to catalog }
                .collect { (user, catalog) ->
                    if (user == null) return@collect
                    val reputation = user.reputation
                    val level = reputation.level()
                    val next = UserLevel.entries.getOrNull(level.ordinal + 1)

                    // Cruza cada insignia ganada con su ficha del catálogo.
                    val badges = reputation.unlockedBadges.mapNotNull { unlocked ->
                        catalog.firstOrNull { it.id == unlocked.badgeId }?.let { badge ->
                            BadgeUi(badge.id, badge.name, badge.description,
                                badge.requirementType, unlocked.unlockedAt)
                        }
                    }.sortedByDescending { it.unlockedAt }

                    _uiState.update {
                        it.copy(
                            userName = user.account.userName,
                            photoUrl = user.photoUrl,
                            location = listOf(user.location.city, user.location.country)
                                .filter { p -> p.isNotBlank() }.joinToString(", "),
                            level = level,
                            levelNumber = reputation.levelNumber(),
                            points = reputation.points,
                            nextLevelPoints = next?.minPoints,
                            badges = badges,
                        )
                    }
                }
        }

        // Carga el catálogo y revisa si ya cumple alguna insignia nueva.
        viewModelScope.launch {
            loadBadges()
            evaluateMyBadges()
        }
        refresh()
    }

    // Recalcula las publicaciones (se llama también al volver a la pantalla).
    fun refresh() {
        viewModelScope.launch {
            getPostStats()
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
                .onFailure { _uiState.update { s -> s.copy(hasError = true) } }
        }
    }
}