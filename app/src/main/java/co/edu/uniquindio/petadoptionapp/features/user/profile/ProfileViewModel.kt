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
ViewModel de la pestaña "Mi Perfil" del dashboard.
Su única responsabilidad es observar al usuario en sesión mediante ObserveCurrentUserUseCase
y transformar sus datos (nombre, ubicación, reputación y foto) en un ProfileUiState listo
para pintar. No emite eventos de navegación: la pantalla ProfileScreen recibe callbacks
desde AppNavigation y este ViewModel solo expone estado. Como la observación es un Flow,
cualquier cambio del perfil (puntos ganados, nueva ubicación o foto) se refleja sin recargar.
**/

/**
Estado de la pantalla de perfil. Solo guarda datos neutros:
los textos (nombre del nivel, etc.) los pone la UI con strings.xml.
Además de los datos crudos expone propiedades calculadas (isMaxLevel, progress,
progressPercent) para que la UI no tenga que hacer cuentas.
**/
data class ProfileUiState(
    // Nombre de usuario que se muestra en la tarjeta superior.
    val userName: String = "",
    // Ubicación ya formateada como "Ciudad, País" (vacía si no hay datos).
    val location: String = "",
    // Nivel de reputación actual del usuario.
    val level: UserLevel = UserLevel.ANIMAL_FRIEND,
    // Número ordinal del nivel (1, 2, 3...) que se muestra en la tarjeta de progreso.
    val levelNumber: Int = 1,
    // Puntos de reputación acumulados por el usuario.
    val points: Int = 0,
    // Puntos mínimos del nivel actual: inicio de la barra de progreso.
    val currentLevelPoints: Int = 0,
    // Puntos del siguiente nivel; null cuando ya está en el nivel máximo.
    val nextLevelPoints: Int? = null,
    // URL de la foto de perfil; null si el usuario no ha subido una.
    val photoUrl: String? = null
) {
    // Indica si el usuario alcanzó el nivel máximo (no hay siguiente nivel).
    val isMaxLevel: Boolean get() = nextLevelPoints == null

    // Fracción entre 0 y 1 dentro del nivel actual; coerceIn evita valores fuera de rango.
    val progress: Float
        get() {
            // Si no hay siguiente nivel, la barra se muestra completa.
            val next = nextLevelPoints ?: return 1f
            // Calcula cuántos puntos abarca el nivel actual.
            val range = next - currentLevelPoints
            // Evita dividir por cero; si no, calcula el avance relativo dentro del nivel.
            return if (range <= 0) 0f
            else ((points - currentLevelPoints).toFloat() / range).coerceIn(0f, 1f)
        }

    // Progreso expresado como porcentaje entero (0 a 100) para mostrarlo en texto.
    val progressPercent: Int get() = (progress * 100).roundToInt()
}

// Marca la clase para que Hilt la construya e inyecte sus dependencias.
@HiltViewModel
class ProfileViewModel @Inject constructor(
    // Caso de uso que emite el usuario en sesión cada vez que su perfil cambia.
    observeCurrentUser: ObserveCurrentUserUseCase,
) : ViewModel() {

    // Estado mutable interno, solo modificable desde el ViewModel.
    private val _uiState = MutableStateFlow(ProfileUiState())
    // Estado público de solo lectura que observa la pantalla.
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    // Bloque de inicialización: arranca la observación del usuario al crear el ViewModel.
    init {
        // Si el perfil cambia (puntos, ubicación), la pantalla se actualiza sola.
        viewModelScope.launch {
            // Recolecta cada emisión del usuario actual.
            observeCurrentUser().collect { user ->
                // Si no hay sesión (usuario nulo), ignora la emisión y conserva el estado.
                if (user == null) return@collect
                // Toma la reputación del usuario para calcular nivel y puntos.
                val reputation = user.reputation
                // Obtiene el nivel correspondiente a los puntos actuales.
                val level = reputation.level()
                // El siguiente nivel es el que sigue en el enum; null si ya es el último.
                val next = UserLevel.entries.getOrNull(level.ordinal + 1)
                // Actualiza el estado de forma atómica copiando los nuevos valores.
                _uiState.update {
                    it.copy(
                        // Nombre de usuario de la cuenta.
                        userName = user.account.userName,
                        // Une ciudad y país omitiendo las partes vacías, separados por coma.
                        location = listOf(user.location.city, user.location.country)
                            .filter { part -> part.isNotBlank() }
                            .joinToString(", "),
                        // Nivel calculado a partir de la reputación.
                        level = level,
                        // Número del nivel para mostrar en la tarjeta.
                        levelNumber = reputation.levelNumber(),
                        // Puntos acumulados.
                        points = reputation.points,
                        // Puntos mínimos del nivel actual (inicio de la barra).
                        currentLevelPoints = level.minPoints,
                        // Puntos del siguiente nivel o null si ya es el máximo.
                        nextLevelPoints = next?.minPoints,
                        // Foto de perfil actual.
                        photoUrl = user.photoUrl,
                    )
                }
            }
        }
    }
}
