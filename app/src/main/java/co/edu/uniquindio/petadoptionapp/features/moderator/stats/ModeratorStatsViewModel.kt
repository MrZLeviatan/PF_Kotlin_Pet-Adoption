package co.edu.uniquindio.petadoptionapp.features.moderator.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.petadoptionapp.domain.model.post.ModeratorPerformance
import co.edu.uniquindio.petadoptionapp.domain.usecase.catalog.LoadCatalogUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.catalog.ObserveCategoriesUseCase
import co.edu.uniquindio.petadoptionapp.domain.usecase.post.GetModeratorPerformanceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ModeratorStatsUiState(
    val performance: ModeratorPerformance = ModeratorPerformance(),
    // Decisiones por categoría con el nombre del catálogo ya resuelto.
    val categories: List<Pair<String, Int>> = emptyList(),
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
)

/**
 Dashboard del moderador (pestaña Estadísticas y tarjeta de su perfil): lo que resolvió, lo que tiene
 pendiente, su tasa de aprobación y cómo se reparte. Se recarga al volver a la pantalla.
 */
@HiltViewModel
class ModeratorStatsViewModel @Inject constructor(
    private val getPerformance: GetModeratorPerformanceUseCase,
    private val observeCategories: ObserveCategoriesUseCase,
    private val loadCatalog: LoadCatalogUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ModeratorStatsUiState())
    val uiState: StateFlow<ModeratorStatsUiState> = _uiState.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            if (observeCategories().value.isEmpty()) loadCatalog()
            getPerformance()
                .onSuccess { performance ->
                    val names = observeCategories().value.associate { it.id to it.name }
                    _uiState.update {
                        it.copy(
                            performance = performance,
                            categories = performance.resolvedByCategory.map { (id, count) -> (names[id] ?: id) to count },
                            isLoading = false,
                            hasError = false,
                        )
                    }
                }
                .onFailure { _uiState.update { it.copy(isLoading = false, hasError = true) } }
        }
    }
}
