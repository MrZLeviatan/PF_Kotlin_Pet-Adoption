package co.edu.uniquindio.petadoptionapp.features.user.metrics

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Tipo de insignia. La pantalla decide el icono según el tipo. */
enum class BadgeType { GUARDIAN, PROFILE_COMPLETE, VERIFIED_CONTACT }

data class BadgeUi(
    val type: BadgeType,
    val title: String,
    val description: String,
    val tag: String,
)

/**
Estado de la pantalla. Los valores por defecto son datos QUEMADOS de prueba.
Los textos de las insignias viven aquí y no en strings.xml porque vendrán del backend.
TODO: reemplazar por datos reales cuando existan los casos de uso (usuario, métricas, insignias).
 */
data class MetricsUiState(
    val userName: String = "Nicolás C.S",
    val location: String = "Armenia, Colombia",
    val levelName: String = "Guardián de Huellas",
    val levelNumber: Int = 5,
    val points: Int = 3_240,
    val nextLevelPoints: Int = 4_500,
    val activePosts: Int = 12,
    val reviewPosts: Int = 3,
    val finishedPosts: Int = 8,
    val badges: List<BadgeUi> = listOf(
        BadgeUi(BadgeType.GUARDIAN, "Guardián de Huellas", "Nivel 5 alcanzado", "LVL 5"),
        BadgeUi(BadgeType.PROFILE_COMPLETE, "Perfil Completo", "Datos personales actualizados", "100%"),
        BadgeUi(BadgeType.VERIFIED_CONTACT, "Contacto Verificado", "Teléfono y correo confirmados", "Verificado"),
    ),
)

class MetricsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MetricsUiState())
    val uiState: StateFlow<MetricsUiState> = _uiState.asStateFlow()
}