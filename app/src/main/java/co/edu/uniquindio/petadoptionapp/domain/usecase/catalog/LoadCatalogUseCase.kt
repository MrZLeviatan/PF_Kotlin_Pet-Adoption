package co.edu.uniquindio.petadoptionapp.domain.usecase.catalog

import co.edu.uniquindio.petadoptionapp.domain.repository.CatalogRepository

/**
 Documenta el caso de uso que carga los catálogos al abrir la app (una sola lectura por sesión).
 */
class LoadCatalogUseCase(

    // Inyecta el repositorio de catálogos para leerlos.
    private val catalogRepository: CatalogRepository
) {
    // Define la función que devuelve un resultado vacío.
    suspend operator fun invoke(): Result<Unit> = catalogRepository.refresh()
}