package co.edu.uniquindio.petadoptionapp.domain.usecase.catalog

import co.edu.uniquindio.petadoptionapp.domain.error.CatalogException
import co.edu.uniquindio.petadoptionapp.domain.repository.CatalogRepository

/**
Documenta el caso de uso que verifica que la categoría y el tipo de animal de una publicación existan y estén activos.
Lo usan CreatePostUseCase y UpdatePostUseCase antes de guardar.
 */
class CheckPostCatalogUseCase(
    // Inyecta el repositorio de catálogos para leer las categorías y tipos de animal.
    private val catalogRepository: CatalogRepository
) {
    // Define la función que recibe el id de la categoría y el id del tipo de animal.
    suspend operator fun invoke(categoryId: String, animalTypeId: String?): Result<Unit> {

        // La categoría debe existir y estar activa.
        val category = catalogRepository.findCategoryById(categoryId)
            ?: return Result.failure(CatalogException.CategoryNotFound)
        if (!category.active) return Result.failure(CatalogException.CategoryInactive)

        // El tipo de animal es opcional (una jornada veterinaria no tiene mascota); si viene, debe ser válido.
        if (animalTypeId != null) {
            val animalType = catalogRepository.findAnimalTypeById(animalTypeId)
                ?: return Result.failure(CatalogException.AnimalTypeNotFound)
            if (!animalType.active) return Result.failure(CatalogException.AnimalTypeInactive)
        }

        return Result.success(Unit)
    }
}