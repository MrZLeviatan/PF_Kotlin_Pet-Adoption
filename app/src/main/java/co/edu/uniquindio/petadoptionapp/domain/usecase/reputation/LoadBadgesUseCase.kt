package co.edu.uniquindio.petadoptionapp.domain.usecase.reputation

import co.edu.uniquindio.petadoptionapp.domain.repository.ReputationRepository

/**
Caso de uso que carga el catálogo de insignias al abrir la app (una sola lectura por sesión).
El catálogo queda en caché en ReputationRepository.badges. Lo usa el arranque de la app / ViewModel principal.
**/
// Declara la clase del caso de uso con sus dependencias inyectadas.
class LoadBadgesUseCase(
    // Inyecta el repositorio de reputación.
    private val reputationRepository: ReputationRepository
) {
    // Define la función asíncrona principal que actualiza el catálogo de insignias desde el repositorio y devuelve un resultado vacío.
    suspend operator fun invoke(): Result<Unit> = reputationRepository.refreshBadges()
}
