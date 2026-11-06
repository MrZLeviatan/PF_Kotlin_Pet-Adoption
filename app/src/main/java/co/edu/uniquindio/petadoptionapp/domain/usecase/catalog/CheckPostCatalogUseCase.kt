package co.edu.uniquindio.petadoptionapp.domain.usecase.catalog

import co.edu.uniquindio.petadoptionapp.domain.error.CatalogException
import co.edu.uniquindio.petadoptionapp.domain.repository.CatalogRepository

/**
Caso de uso que verifica que la categoría y el tipo de animal de una publicación existan y estén activos.
Consulta los catálogos a través de CatalogRepository y devuelve el CatalogException correspondiente si algo no cuadra.
Lo usan CreatePostUseCase y UpdatePostUseCase antes de guardar.
**/
// Declara el caso de uso de verificación de catálogo de una publicación.
class CheckPostCatalogUseCase(
    // Inyecta el repositorio de catálogos para leer las categorías y tipos de animal.
    private val catalogRepository: CatalogRepository
) {
    // Define la función que recibe el id de la categoría y el id del tipo de animal.
    suspend operator fun invoke(categoryId: String, animalTypeId: String?): Result<Unit> {

        // La categoría debe existir y estar activa.
        val category = catalogRepository.findCategoryById(categoryId)
            // Si no existe, falla con CategoryNotFound.
            ?: return Result.failure(CatalogException.CategoryNotFound)
        // Si existe pero está desactivada, falla con CategoryInactive.
        if (!category.active) return Result.failure(CatalogException.CategoryInactive)

        // El tipo de animal es opcional (una jornada veterinaria no tiene mascota); si viene, debe ser válido.
        if (animalTypeId != null) {
            // Busca el tipo de animal en el catálogo.
            val animalType = catalogRepository.findAnimalTypeById(animalTypeId)
                // Si no existe, falla con AnimalTypeNotFound.
                ?: return Result.failure(CatalogException.AnimalTypeNotFound)
            // Si existe pero está desactivado, falla con AnimalTypeInactive.
            if (!animalType.active) return Result.failure(CatalogException.AnimalTypeInactive)
        }

        // Todo es válido: devuelve éxito sin datos.
        return Result.success(Unit)
    }
}
