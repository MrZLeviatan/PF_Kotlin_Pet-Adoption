package co.edu.uniquindio.petadoptionapp.features.user.profile

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

/**
Estado de la pantalla. Los valores por defecto son datos QUEMADOS de prueba.
TODO: reemplazar por el usuario real cuando exista el caso de uso (GetCurrentUserUseCase).
 */
data class ProfileUiState(
    val userName: String = "Nicolás C.S",
    val location: String = "Armenia, Colombia",
    val levelName: String = "Guardián de Huellas",
    val levelNumber: Int = 5,
    val points: Int = 3_240,
    val nextLevelPoints: Int = 4_500,
) {
    // Fracción entre 0 y 1 para la barra; coerceIn evita valores fuera de rango.
    val progress: Float
        get() = if (nextLevelPoints == 0) 0f
        else (points.toFloat() / nextLevelPoints).coerceIn(0f, 1f)

    val progressPercent: Int
        get() = (progress * 100).roundToInt()
}

class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
}