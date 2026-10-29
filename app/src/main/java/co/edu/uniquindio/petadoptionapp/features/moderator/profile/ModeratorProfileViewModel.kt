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

data class ModeratorProfileUiState(
    val userName: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    // El mismo resumen del dashboard de desempeño.
    val performance: ModeratorPerformance = ModeratorPerformance(),
    val isLoading: Boolean = true,
)

/** Perfil del moderador: sus datos, el resumen de su desempeño y el acceso a Configuración. */
@HiltViewModel
class ModeratorProfileViewModel @Inject constructor(
    private val getCurrentModerator: GetCurrentModeratorUseCase,
    private val getPerformance: GetModeratorPerformanceUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ModeratorProfileUiState())
    val uiState: StateFlow<ModeratorProfileUiState> = _uiState.asStateFlow()

    // Se llama al aparecer: el desempeño cambia cada vez que aprueba o rechaza.
    fun refresh() {
        viewModelScope.launch {
            getCurrentModerator().onSuccess { moderator ->
                _uiState.update {
                    it.copy(
                        userName = moderator.account.userName,
                        name = moderator.name,
                        email = moderator.account.email,
                        phone = moderator.phone,
                    )
                }
            }
            getPerformance().onSuccess { performance -> _uiState.update { it.copy(performance = performance) } }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
