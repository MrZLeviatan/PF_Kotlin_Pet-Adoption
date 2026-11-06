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

/**
ViewModel del dashboard de estadísticas del moderador (ModeratorStatsScreen).
Estado de UI (ModeratorStatsUiState): el desempeño completo del moderador (aprobadas, rechazadas,
en revisión, actividad diaria, pendientes por prioridad), las decisiones por categoría con el nombre
ya resuelto desde el catálogo y las banderas de carga y error.
Evento: refresh(), que la pantalla invoca cada vez que vuelve a primer plano.
Casos de uso: GetModeratorPerformanceUseCase, ObserveCategoriesUseCase y LoadCatalogUseCase.
Navegación: se llega desde la pestaña Estadísticas o tocando el resumen en el perfil del moderador;
el ViewModel no navega.
**/

// Estado inmutable que dibuja el dashboard de estadísticas.
data class ModeratorStatsUiState(
    // Cifras de desempeño del moderador.
    val performance: ModeratorPerformance = ModeratorPerformance(),
    // Decisiones por categoría con el nombre del catálogo ya resuelto.
    val categories: List<Pair<String, Int>> = emptyList(),
    // Indica si los datos aún se están cargando.
    val isLoading: Boolean = true,
    // Indica si falló la carga.
    val hasError: Boolean = false,
)

/**
 Dashboard del moderador (pestaña Estadísticas y tarjeta de su perfil): lo que resolvió, lo que tiene
 pendiente, su tasa de aprobación y cómo se reparte. Se recarga al volver a la pantalla.
 */
// Hilt crea e inyecta este ViewModel.
@HiltViewModel
class ModeratorStatsViewModel @Inject constructor(
    // Obtiene el desempeño del moderador.
    private val getPerformance: GetModeratorPerformanceUseCase,
    // Expone las categorías cargadas.
    private val observeCategories: ObserveCategoriesUseCase,
    // Carga el catálogo de categorías.
    private val loadCatalog: LoadCatalogUseCase,
) : ViewModel() {

    // Estado mutable interno.
    private val _uiState = MutableStateFlow(ModeratorStatsUiState())
    // Estado público de solo lectura que observa la UI.
    val uiState: StateFlow<ModeratorStatsUiState> = _uiState.asStateFlow()

    // Recarga las estadísticas del moderador.
    fun refresh() {
        // Lanza la carga en el ámbito del ViewModel.
        viewModelScope.launch {
            // Carga el catálogo solo si aún no hay categorías.
            if (observeCategories().value.isEmpty()) loadCatalog()
            // Pide el desempeño del moderador.
            getPerformance()
                // Si responde, arma el estado con las cifras y las categorías.
                .onSuccess { performance ->
                    // Mapa id -> nombre de las categorías del catálogo.
                    val names = observeCategories().value.associate { it.id to it.name }
                    // Actualiza el estado.
                    _uiState.update {
                        it.copy(
                            // Cifras completas.
                            performance = performance,
                            // Convierte cada id de categoría en su nombre (o deja el id si no se encuentra).
                            categories = performance.resolvedByCategory.map { (id, count) -> (names[id] ?: id) to count },
                            // Termina la carga.
                            isLoading = false,
                            // Sin error.
                            hasError = false,
                        )
                    }
                }
                // Si falla, quita la carga y marca el error.
                .onFailure { _uiState.update { it.copy(isLoading = false, hasError = true) } }
        }
    }
}
