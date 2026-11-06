package co.edu.uniquindio.petadoptionapp.domain.usecase.catalog

import co.edu.uniquindio.petadoptionapp.domain.repository.CatalogRepository

/**
Caso de uso que carga los catálogos (categorías y tipos de animal) desde Firestore al abrir la app
(una sola lectura por sesión); el resultado queda en caché en los flujos de CatalogRepository.
Lo usa el arranque de la app / ViewModel principal.
**/
// Declara el caso de uso de carga de catálogos.
class LoadCatalogUseCase(

    // Inyecta el repositorio de catálogos para leerlos.
    private val catalogRepository: CatalogRepository
) {
    // Define la función que pide al repositorio refrescar los catálogos y devuelve un resultado vacío.
    suspend operator fun invoke(): Result<Unit> = catalogRepository.refresh()
}
