package co.edu.uniquindio.petadoptionapp.features.user.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.petadoptionapp.domain.model.reputation.UserLevel
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.ObserveCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
Estado de la pantalla de perfil. Solo guarda datos neutros:
los textos (nombre del nivel, etc.) los pone la UI con strings.xml.
 */
data class ProfileUiState(
    val userName: String = "",
    val location: String = "",
    val level: UserLevel = UserLevel.ANIMAL_FRIEND,
    val levelNumber: Int = 1,
    val points: Int = 0,
    // Puntos mínimos del nivel actual: inicio de la barra de progreso.
    val currentLevelPoints: Int = 0,
    // Puntos del siguiente nivel; null cuando ya está en el nivel máximo.
    val nextLevelPoints: Int? = null,
    val photoUrl: String? = null
) {
    val isMaxLevel: Boolean get() = nextLevelPoints == null

    // Fracción entre 0 y 1 dentro del nivel actual; coerceIn evita valores fuera de rango.
    val progress: Float
        get() {
            val next = nextLevelPoints ?: return 1f
            val range = next - currentLevelPoints
            return if (range <= 0) 0f
            else ((points - currentLevelPoints).toFloat() / range).coerceIn(0f, 1f)
        }

    val progressPercent: Int get() = (progress * 100).roundToInt()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUserUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        // Si el perfil cambia (puntos, ubicación), la pantalla se actualiza sola.
        viewModelScope.launch {
            observeCurrentUser().collect { user ->
                if (user == null) return@collect
                val reputation = user.reputation
                val level = reputation.level()
                // El siguiente nivel es el que sigue en el enum; null si ya es el último.
                val next = UserLevel.entries.getOrNull(level.ordinal + 1)
                _uiState.update {
                    it.copy(
                        userName = user.account.userName,
                        location = listOf(user.location.city, user.location.country)
                            .filter { part -> part.isNotBlank() }
                            .joinToString(", "),
                        level = level,
                        levelNumber = reputation.levelNumber(),
                        points = reputation.points,
                        currentLevelPoints = level.minPoints,
                        nextLevelPoints = next?.minPoints,
                        photoUrl = user.photoUrl,
                    )
                }
            }
        }
    }
}