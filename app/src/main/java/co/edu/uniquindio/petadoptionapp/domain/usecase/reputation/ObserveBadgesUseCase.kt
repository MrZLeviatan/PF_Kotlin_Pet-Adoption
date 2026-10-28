package co.edu.uniquindio.petadoptionapp.domain.usecase.reputation

import co.edu.uniquindio.petadoptionapp.domain.model.reputation.Badge
import co.edu.uniquindio.petadoptionapp.domain.repository.ReputationRepository
import kotlinx.coroutines.flow.StateFlow

// Define la clase del caso de uso para observar el catálogo de insignias inyectando el repositorio de reputación.
class ObserveBadgesUseCase(
    // Inyecta el repositorio de reputación.
    private val reputationRepository: ReputationRepository
) {
    // Define la función operadora que expone el flujo de estado reactivo con la lista completa de insignias directamente desde el repositorio.
    operator fun invoke(): StateFlow<List<Badge>> = reputationRepository.badges
}