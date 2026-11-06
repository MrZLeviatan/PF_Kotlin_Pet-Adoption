package co.edu.uniquindio.petadoptionapp.features.moderator.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.petadoptionapp.domain.model.post.ModeratorPerformance
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.GetModeratorPerformanceUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.user.GetCurrentModeratorUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
ViewModel de la pantalla de perfil del moderador (ModeratorProfileScreen).
Estado de UI (ModeratorProfileUiState): alias, nombre, correo, teléfono, el resumen de desempeño
(aprobadas, rechazadas y en revisión) y la bandera de carga.
Evento: refresh(), que la pantalla invoca cada vez que vuelve a primer plano.
Casos de uso: GetCurrentModeratorUseCase (datos personales) y GetModeratorPerformanceUseCase (desempeño).
Navegación: el ViewModel no navega; la pantalla expone callbacks para ir al dashboard de
estadísticas y a Configuración.
**/

// Estado inmutable que dibuja la pantalla de perfil del moderador.
data class ModeratorProfileUiState(
    // Alias (nombre de usuario) de la cuenta.
    val userName: String = "",
    // Nombre completo del moderador.
    val name: String = "",
    // Correo de la cuenta.
    val email: String = "",
    // Teléfono del moderador.
    val phone: String = "",
    // El mismo resumen del dashboard de desempeño.
    val performance: ModeratorPerformance = ModeratorPerformance(),
    // Indica si los datos aún se están cargando.
    val isLoading: Boolean = true,
)

/** Perfil del moderador: sus datos, el resumen de su desempeño y el acceso a Configuración. */
// Hilt crea e inyecta este ViewModel.
@HiltViewModel
class ModeratorProfileViewModel @Inject constructor(
    // Obtiene el perfil del moderador con sesión.
    private val getCurrentModerator: GetCurrentModeratorUseCase,
    // Obtiene las cifras de desempeño del moderador.
    private val getPerformance: GetModeratorPerformanceUseCase,
) : ViewModel() {

    // Estado mutable interno.
    private val _uiState = MutableStateFlow(ModeratorProfileUiState())
    // Estado público de solo lectura que observa la UI.
    val uiState: StateFlow<ModeratorProfileUiState> = _uiState.asStateFlow()

    // Se llama al aparecer: el desempeño cambia cada vez que aprueba o rechaza.
    fun refresh() {
        // Lanza la carga en el ámbito del ViewModel.
        viewModelScope.launch {
            // Consulta el moderador actual y, si responde, copia sus datos al estado.
            getCurrentModerator().onSuccess { moderator ->
                // Actualiza los datos personales en el estado.
                _uiState.update {
                    it.copy(
                        // Alias de la cuenta.
                        userName = moderator.account.userName,
                        // Nombre completo.
                        name = moderator.name,
                        // Correo de la cuenta.
                        email = moderator.account.email,
                        // Teléfono.
                        phone = moderator.phone,
                    )
                }
            }
            // Consulta el desempeño y, si responde, lo guarda en el estado.
            getPerformance().onSuccess { performance -> _uiState.update { it.copy(performance = performance) } }
            // Marca la carga como terminada (aunque alguna consulta haya fallado).
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
