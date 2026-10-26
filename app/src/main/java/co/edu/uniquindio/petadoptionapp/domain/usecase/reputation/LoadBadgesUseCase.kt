package co.edu.uniquindio.petadoptionapp.domain.usecase.reputation

import co.edu.uniquindio.petadoptionapp.domain.repository.ReputationRepository

/**
 Documenta el caso de uso que carga el catálogo de insignias al abrir la app (una sola lectura por sesión).
 */
class LoadBadgesUseCase(
    // Inyecta el repositorio de reputación.
    private val reputationRepository: ReputationRepository
) {
    // Define la función asíncrona principal que actualiza el catálogo de insignias desde el repositorio y devuelve un resultado vacío.
    suspend operator fun invoke(): Result<Unit> = reputationRepository.refreshBadges()
}